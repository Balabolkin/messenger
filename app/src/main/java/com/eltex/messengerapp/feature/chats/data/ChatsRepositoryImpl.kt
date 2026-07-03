package com.eltex.messengerapp.feature.chats.data

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

@Singleton
class ChatsRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val okHttpClient: OkHttpClient,
    private val authDataStore: AuthDataStore
) : ChatsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _chatsFlow = MutableStateFlow<List<SubscriptionDto>>(emptyList())
    override val chatsFlow: Flow<List<SubscriptionDto>> = _chatsFlow.asStateFlow()

    private val roomListeners = java.util.concurrent.ConcurrentHashMap<String, (MessageDto) -> Unit>()

    private var allSubscriptions = emptyList<SubscriptionDto>()
    private var currentPage = 0
    private var isPaginating = false
    private var isRefreshing = false
    private var hasMore = true

    private fun updateChatsFlow() {
        _chatsFlow.value = allSubscriptions.take(currentPage * 20)
        hasMore = allSubscriptions.size > currentPage * 20
    }

    private var webSocket: WebSocket? = null
    private var shouldReconnect = false
    private val reconnectDelayMs = 3000L

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
                val updatedSubs = subsResponse.update ?: emptyList()
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
            .url("wss://study-chat.eltex-co.ru/websocket")
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
                        val current = allSubscriptions.toMutableList()
                        val index = current.indexOfFirst { it._id == updatedSub._id }
                        if (index != -1) {
                            val existing = current[index]
                            val merged = existing.copy(
                                name = updatedSub.name ?: existing.name,
                                fname = updatedSub.fname ?: existing.fname,
                                t = updatedSub.t,
                                unread = updatedSub.unread,
                                alert = updatedSub.alert,
                                ts = updatedSub.ts ?: existing.ts,
                                ls = updatedSub.ls ?: existing.ls,
                                lastMessage = updatedSub.lastMessage ?: existing.lastMessage
                            )
                            current[index] = merged
                        } else {
                            current.add(updatedSub)
                        }
                        allSubscriptions = current.sortedWith(ChatsComparator)
                        updateChatsFlow()
                    }
                    "removed" -> {
                        val subId = if (data is JsonObject) {
                            data["_id"]?.let { (it as? JsonPrimitive)?.content }
                        } else null
                        if (subId != null) {
                            allSubscriptions = allSubscriptions.filter { it._id != subId }
                            updateChatsFlow()
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
                    
                    val current = allSubscriptions.toMutableList()
                    var changed = false
                    for (i in current.indices) {
                        if (current[i].rid == roomId) {
                            var sub = current[i]
                            if (roomName != null) sub = sub.copy(name = roomName)
                            if (roomFname != null) sub = sub.copy(fname = roomFname)
                            if (lastMsgEl != null) {
                                val lastMsg = try {
                                    json.decodeFromJsonElement<MessageDto>(lastMsgEl)
                                } catch (e: Exception) {
                                    null
                                }
                                if (lastMsg != null) sub = sub.copy(lastMessage = lastMsg)
                            }
                            current[i] = sub
                            changed = true
                        }
                    }
                    if (changed) {
                        allSubscriptions = current.sortedWith(ChatsComparator)
                        updateChatsFlow()
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

    override suspend fun createRoom(name: String, type: String) {
        val endpoint = when (type) {
            "c" -> "api/v1/channels.create"
            "p" -> "api/v1/groups.create"
            "d" -> "api/v1/im.create"
            else -> "api/v1/channels.create"
        }
        val response: HttpResponse = if (type == "d") {
            client.post(endpoint) {
                setBody(CreateImRequest(username = name))
            }
        } else {
            client.post(endpoint) {
                setBody(CreateRoomRequest(name = name))
            }
        }
        if (response.status.value == 200 || response.status.value == 201) {
            refresh()
        } else {
            val errorBody = response.body<String>()
            throw Exception("Failed to create chat: $errorBody")
        }
    }

    override suspend fun searchUsers(query: String): List<com.eltex.messengerapp.feature.chats.domain.UserDto> {
        val q = if (query.isBlank()) "{}" else """{"${"$"}or":[{"username":{"${"$"}regex":"$query","${"$"}options":"i"}},{"name":{"${"$"}regex":"$query","${"$"}options":"i"}}]}"""
        val response: HttpResponse = client.get("api/v1/users.list") {
            parameter("query", q)
            parameter("count", 50)
        }
        if (response.status.value == 200) {
            val res: UsersListResponse = response.body()
            return res.users ?: emptyList()
        }
        return emptyList()
    }

    override suspend fun createDirectMessage(username: String) {
        val response: HttpResponse = client.post("api/v1/im.create") {
            setBody(CreateImRequest(username = username))
        }
        if (response.status.value == 200 || response.status.value == 201) {
            refresh()
        } else {
            val errorBody = response.body<String>()
            throw Exception("Failed to create direct message: $errorBody")
        }
    }
}

@Serializable
data class CreateRoomRequest(
    val name: String
)

@Serializable
data class CreateImRequest(
    val username: String
)

@Serializable
data class UsersListResponse(
    val users: List<com.eltex.messengerapp.feature.chats.domain.UserDto>? = null,
    val success: Boolean
)
