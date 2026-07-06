package com.eltex.messengerapp.feature.chats.data

import com.eltex.messengerapp.data.database.dao.ChatDao
import com.eltex.messengerapp.data.database.dao.MessageDao
import com.eltex.messengerapp.BuildConfig
import com.eltex.messengerapp.datastore.AuthDataStore
import com.eltex.messengerapp.feature.chats.domain.ChatsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

private const val CHATS_PAGE_SIZE = 20
private const val RECONNECT_DELAY_MS = 3000L

@Singleton
class ChatsRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val okHttpClient: OkHttpClient,
    private val authDataStore: AuthDataStore,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
) : ChatsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _chatsFlow = MutableStateFlow<List<SubscriptionDto>>(emptyList())
    override val chatsFlow: Flow<List<SubscriptionDto>> = _chatsFlow.asStateFlow()

    init {
        repositoryScope.launch {
            chatDao.getAllChats().collect { entities ->
                val subs = entities.map { it.toSubscriptionDto() }
                _chatsFlow.value = subs
                allSubscriptions = subs
                updateChatsFlow()
            }
        }
        repositoryScope.launch {
            authDataStore.getToken().collect { token ->
                val userId = authDataStore.getUserId().first()
                if (webSocket != null && !token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                    val loginMsg = """{"msg":"method","method":"login","id":"login-id","params":[{"resume":"$token"}]}"""
                    webSocket?.send(loginMsg)
                }
            }
        }
    }
    private val roomListeners = java.util.concurrent.ConcurrentHashMap<String, (MessageDto) -> Unit>()

    private var allSubscriptions = emptyList<SubscriptionDto>()
    private var currentPage = 1
    private var isPaginating = false
    private var isRefreshing = false
    private var hasMore = true

    private fun updateChatsFlow() {
        _chatsFlow.value = allSubscriptions.take(currentPage * CHATS_PAGE_SIZE)
        hasMore = allSubscriptions.size > currentPage * CHATS_PAGE_SIZE
    }

    private var webSocket: WebSocket? = null
    private var shouldReconnect = false
    private val reconnectDelayMs = RECONNECT_DELAY_MS

    private val ChatsComparator = Comparator<SubscriptionDto> { o1, o2 ->
        val t1 = ChatsDateParser.parse(o1.lastMessage?.ts) ?: ChatsDateParser.parse(o1.ls) ?: ChatsDateParser.parse(o1.ts) ?: 0L
        val t2 = ChatsDateParser.parse(o2.lastMessage?.ts) ?: ChatsDateParser.parse(o2.ls) ?: ChatsDateParser.parse(o2.ts) ?: 0L
        t2.compareTo(t1) // Descending (latest first)
    }

    override suspend fun loadNextPage() {
        if (isPaginating || !hasMore) return
        isPaginating = true
        try {
            currentPage++
            updateChatsFlow()
        } finally {
            isPaginating = false
        }
    }

    override suspend fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        try {
            val response: HttpResponse = client.get("api/v1/subscriptions.get")
            if (response.status.value == 200) {
                val subsResponse: SubscriptionsResponse = response.body()
                val rawSubs = subsResponse.update ?: emptyList()

                // Fetch rooms info to get last messages
                var roomsMap = emptyMap<String, RoomDto>()
                try {
                    val roomsResponse: HttpResponse = client.get("api/v1/rooms.get")
                    if (roomsResponse.status.value == 200) {
                        val rResponse: RoomsResponse = roomsResponse.body()
                        roomsMap = rResponse.update?.associateBy { it._id } ?: emptyMap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Merge last message into subscriptions
                val updatedSubs = rawSubs.map { sub ->
                    val roomInfo = roomsMap[sub.rid]
                    if (roomInfo?.lastMessage != null) {
                        sub.copy(lastMessage = roomInfo.lastMessage)
                    } else {
                        sub
                    }
                }

                chatDao.insertChats(updatedSubs.map { it.toEntity() })

                allSubscriptions = updatedSubs.sortedWith(ChatsComparator)
                currentPage = 1
                updateChatsFlow()
            } else {
                val errorBody = response.body<String>()
                throw Exception("Failed to refresh chats: $errorBody")
            }
        } finally {
            isRefreshing = false
        }
    }

    override fun startRealtimeUpdates() {
        if (webSocket != null) return
        shouldReconnect = true
        connectWebSocket()
    }

    override fun stopRealtimeUpdates() {
        shouldReconnect = false
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }

    override suspend fun searchChats(query: String): List<SubscriptionDto> {
        return try {
            val jsonQuery = """{"${"$"}or":[{"name":{"${"$"}regex":"$query","${"$"}options":"i"}},{"fname":{"${"$"}regex":"$query","${"$"}options":"i"}}]}"""
            val response: HttpResponse = client.get("api/v1/subscriptions.get") {
                parameter("query", jsonQuery)
            }
            if (response.status.value == 200) {
                val subsResponse: SubscriptionsResponse = response.body()
                subsResponse.update ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun connectWebSocket() {
        if (webSocket != null) return
        val request = Request.Builder()
            .url("wss://${BuildConfig.BASE_HOST}/websocket")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("""{"msg":"connect","version":"1","support":["1"]}""")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@ChatsRepositoryImpl.webSocket = null
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@ChatsRepositoryImpl.webSocket = null
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        repositoryScope.launch {
            delay(reconnectDelayMs)
            if (shouldReconnect) {
                connectWebSocket()
            }
        }
    }

    private fun handleWebSocketMessage(text: String) {
        try {
            val ddpMsg = json.decodeFromString<DdpMessage>(text)
            when (ddpMsg.msg) {
                "ping" -> {
                    webSocket?.send("""{"msg":"pong"}""")
                }
                "connected" -> {
                    repositoryScope.launch {
                        val token = authDataStore.getToken().first()
                        val userId = authDataStore.getUserId().first()
                        if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                            val loginMsg = """{"msg":"method","method":"login","id":"login-id","params":[{"resume":"$token"}]}"""
                            webSocket?.send(loginMsg)
                        }
                    }
                }
                "result" -> {
                    if (ddpMsg.id == "login-id") {
                        if (ddpMsg.error == null) {
                            repositoryScope.launch {
                                val userId = authDataStore.getUserId().first()
                                if (!userId.isNullOrEmpty()) {
                                    subscribeToEvents(userId)
                                }
                            }
                        }
                    }
                }
                "changed" -> {
                    if (ddpMsg.collection == "stream-notify-user") {
                        val eventName = ddpMsg.fields?.eventName
                        val args = ddpMsg.fields?.args
                        if (eventName != null && args != null && args.size >= 2) {
                            val action = (args[0] as? JsonPrimitive)?.content
                            val data = args[1]
                            
                            if (eventName.contains("/subscriptions-changed")) {
                                handleSubscriptionChange(action, data)
                            } else if (eventName.contains("/rooms-changed")) {
                                handleRoomChange(action, data)
                            }
                        }
                    } else if (ddpMsg.collection == "stream-room-messages") {
                        val args = ddpMsg.fields?.args
                        if (args != null && args.isNotEmpty()) {
                            val messageDto = json.decodeFromJsonElement<MessageDto>(args[0])
                            val roomId = messageDto.rid
                            if (roomId != null) {
                                repositoryScope.launch {
                                    try {
                                        messageDao.insertMessages(listOf(messageDto.toEntity(roomId)))
                                        chatDao.getChatByRid(roomId)?.let { chat ->
                                            val updatedChat = chat.copy(
                                                lastMessageText = messageDto.msg,
                                                lastMessageTs = ChatsDateParser.parse(messageDto.ts)?.toString(),
                                                lastMessageUserId = messageDto.u?._id,
                                                lastMessageUsername = messageDto.u?.username
                                            )
                                            chatDao.insertChat(updatedChat)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                roomListeners[roomId]?.invoke(messageDto)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToEvents(userId: String) {
        val subSubs = """{"msg":"sub","id":"sub-subs","name":"stream-notify-user","params":["$userId/subscriptions-changed",false]}"""
        webSocket?.send(subSubs)
        val subRooms = """{"msg":"sub","id":"sub-rooms","name":"stream-notify-user","params":["$userId/rooms-changed",false]}"""
        webSocket?.send(subRooms)
        
        // Re-subscribe to active room listeners on reconnection
        roomListeners.keys.forEach { roomId ->
            val subMsg = """{"msg":"sub","id":"sub-room-messages-$roomId","name":"stream-room-messages","params":["$roomId",false]}"""
            webSocket?.send(subMsg)
        }
    }

    private fun handleSubscriptionChange(action: String?, data: JsonElement) {
        repositoryScope.launch {
            try {
                when (action) {
                    "inserted", "updated" -> {
                        val updatedSub = json.decodeFromJsonElement<SubscriptionDto>(data)
                        val existing = chatDao.getChatByRid(updatedSub.rid)
                        val merged = if (existing != null) {
                            existing.copy(
                                name = updatedSub.name ?: existing.name,
                                fname = updatedSub.fname ?: existing.fname,
                                t = updatedSub.t,
                                unread = updatedSub.unread,
                                alert = updatedSub.alert,
                                ts = ChatsDateParser.parse(updatedSub.ts)?.toString() ?: existing.ts,
                                ls = ChatsDateParser.parse(updatedSub.ls)?.toString() ?: existing.ls,
                                lastMessageText = updatedSub.lastMessage?.msg ?: existing.lastMessageText,
                                lastMessageTs = ChatsDateParser.parse(updatedSub.lastMessage?.ts)?.toString() ?: existing.lastMessageTs,
                                lastMessageUserId = updatedSub.lastMessage?.u?._id ?: existing.lastMessageUserId,
                                lastMessageUsername = updatedSub.lastMessage?.u?.username ?: existing.lastMessageUsername
                            )
                        } else {
                            updatedSub.toEntity()
                        }
                        chatDao.insertChat(merged)
                    }
                    "removed" -> {
                        val subId = if (data is JsonObject) {
                            data["_id"]?.let { (it as? JsonPrimitive)?.content }
                        } else null
                        if (subId != null) {
                            chatDao.deleteChatById(subId)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleRoomChange(action: String?, data: JsonElement) {
        repositoryScope.launch {
            try {
                if (action == "updated" || action == "inserted") {
                    val roomObj = data as? JsonObject ?: return@launch
                    val roomId = roomObj["_id"]?.let { (it as? JsonPrimitive)?.content } ?: return@launch
                    val roomName = roomObj["name"]?.let { (it as? JsonPrimitive)?.content }
                    val roomFname = roomObj["fname"]?.let { (it as? JsonPrimitive)?.content }
                    val lastMsgEl = roomObj["lastMessage"]
                    
                    val existing = chatDao.getChatByRid(roomId)
                    if (existing != null) {
                        var updated = existing
                        if (roomName != null) updated = updated.copy(name = roomName)
                        if (roomFname != null) updated = updated.copy(fname = roomFname)
                        if (lastMsgEl != null) {
                            val lastMsg = try {
                                json.decodeFromJsonElement<MessageDto>(lastMsgEl)
                            } catch (e: Exception) {
                                null
                            }
                            if (lastMsg != null) {
                                updated = updated.copy(
                                    lastMessageText = lastMsg.msg,
                                    lastMessageTs = ChatsDateParser.parse(lastMsg.ts)?.toString(),
                                    lastMessageUserId = lastMsg.u?._id,
                                    lastMessageUsername = lastMsg.u?.username
                                )
                            }
                        }
                        chatDao.insertChat(updated)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun subscribeToRoomMessages(roomId: String, callback: (MessageDto) -> Unit) {
        roomListeners[roomId] = callback
        val subMsg = """{"msg":"sub","id":"sub-room-messages-$roomId","name":"stream-room-messages","params":["$roomId",false]}"""
        webSocket?.send(subMsg)
    }

    override fun unsubscribeFromRoomMessages(roomId: String) {
        roomListeners.remove(roomId)
        val unsubMsg = """{"msg":"unsub","id":"sub-room-messages-$roomId"}"""
        webSocket?.send(unsubMsg)
    }

    suspend fun clearAllData() {
        chatDao.clearChats()
    }
}
