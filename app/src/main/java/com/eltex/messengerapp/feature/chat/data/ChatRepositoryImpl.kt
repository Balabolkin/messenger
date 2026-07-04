package com.eltex.messengerapp.feature.chat.data

import com.eltex.messengerapp.BuildConfig
import com.eltex.messengerapp.data.database.dao.ChatDao
import com.eltex.messengerapp.data.database.dao.MessageDao
import com.eltex.messengerapp.feature.chat.domain.ChatRepository
import com.eltex.messengerapp.feature.chats.data.HistoryResponseDto
import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.domain.ChatsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri
import android.content.Context
import kotlinx.serialization.Serializable
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class PostMessageRequest(
    val roomId: String,
    val text: String
)

private const val HISTORY_LOAD_COUNT = 100
private const val MAX_FILE_SIZE_BYTES = 5242880 // 5 MB

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val okHttpClient: OkHttpClient,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val chatsRepository: ChatsRepository
) : ChatRepository {

    private val _messagesState = MutableStateFlow<Map<String, List<MessageDto>>>(emptyMap())
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getMessagesFlow(roomId: String, roomType: String): Flow<List<MessageDto>> {
        repositoryScope.launch {
            messageDao.getMessagesForRoom(roomId).collect { entities ->
                val messages = entities.map { it.toMessageDto() }
                _messagesState.update { current ->
                    current + (roomId to messages)
                }
            }
        }

        return channelFlow {
            chatsRepository.subscribeToRoomMessages(roomId) { newMessage ->
                _messagesState.update { current ->
                    val roomMsgs = current[roomId] ?: emptyList()
                    val index = roomMsgs.indexOfFirst { it._id == newMessage._id }
                    val updated = if (index != -1) {
                        roomMsgs.toMutableList().apply { this[index] = newMessage }
                    } else {
                        listOf(newMessage) + roomMsgs
                    }
                    current + (roomId to updated)
                }
                repositoryScope.launch {
                    try {
                        markAsRead(roomId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            _messagesState.collect { map ->
                send(map[roomId] ?: emptyList())
            }
        }.onCompletion {
            chatsRepository.unsubscribeFromRoomMessages(roomId)
        }
    }

    override suspend fun loadHistory(roomId: String, roomType: String) {
        val endpoint = when (roomType) {
            "d" -> "api/v1/im.history"
            "c" -> "api/v1/channels.history"
            "p" -> "api/v1/groups.history"
            else -> "api/v1/channels.history"
        }

        try {
            val response: HttpResponse = client.get(endpoint) {
                parameter("roomId", roomId)
                parameter("count", HISTORY_LOAD_COUNT)
            }

            if (response.status.value == 200) {
                val historyResponse: HistoryResponseDto = response.body()
                if (historyResponse.success) {
                    val messages = historyResponse.messages

                    messageDao.insertMessages(messages.map { it.toEntity(roomId) })

                    _messagesState.update { current ->
                        current + (roomId to messages)
                    }
                } else {
                    throw Exception("Failed to load chat history")
                }
            } else {
                val errorBody = response.body<String>()
                throw Exception("Failed to load chat history: $errorBody")
            }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

    override suspend fun sendMessage(roomId: String, text: String) {
        val response: HttpResponse = client.post("api/v1/chat.postMessage") {
            contentType(ContentType.Application.Json)
            setBody(PostMessageRequest(roomId, text))
        }
        if (response.status.value != 200) {
            throw Exception("Failed to send message: ${response.status}")
        }
    }

    override suspend fun uploadFile(
        roomId: String,
        fileUri: Uri,
        context: Context,
        msg: String?,
        description: String?
    ) {
        val contentResolver = context.contentResolver
        var fileName = "file"
        val cursor = contentResolver.query(fileUri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            cursor.close()
        }
        val originalMimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val originalBytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            ?: throw Exception("Cannot read file content")

        val (bytes, mimeType) = compressImageIfNeeded(originalBytes, originalMimeType, context)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                fileName,
                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            .apply {
                if (!msg.isNullOrEmpty()) addFormDataPart("msg", msg)
                if (!description.isNullOrEmpty()) addFormDataPart("description", description)
            }
            .build()

        val request = Request.Builder()
            .url("https://${BuildConfig.BASE_HOST}/api/v1/rooms.media/$roomId")
            .post(multipartBody)
            .build()

        val responseBody = withContext(Dispatchers.IO) {
            val response = okHttpClient.newCall(request).execute()
            response.use {
                val bodyStr = it.body.string()
                if (!it.isSuccessful) {
                    throw Exception("Failed to upload file: ${it.code} $bodyStr")
                }
                bodyStr
            }
        }

        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
        val fileObj = jsonResponse["file"]?.jsonObject ?: throw Exception("Invalid upload response: $responseBody")
        val fileUrl = fileObj["url"]?.jsonPrimitive?.content ?: throw Exception("Missing file URL in response: $responseBody")

        val attachment = buildJsonObject {
            put("title", fileName)
            put("title_link", fileUrl)
            put("title_link_download", JsonPrimitive(true))
            if (mimeType.startsWith("image/")) {
                put("image_url", fileUrl)
            } else if (mimeType.startsWith("video/")) {
                put("video_url", fileUrl)
            } else if (mimeType.startsWith("audio/")) {
                put("audio_url", fileUrl)
            }
            put("type", "file")
            if (!description.isNullOrEmpty()) {
                put("description", description)
            }
        }

        val requestBody = buildJsonObject {
            put("roomId", roomId)
            put("text", msg ?: "")
            putJsonArray("attachments") {
                add(attachment)
            }
        }

        val postResponse: HttpResponse = client.post("api/v1/chat.postMessage") {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        if (postResponse.status.value != 200) {
            val errorBody = postResponse.body<String>()
            throw Exception("Failed to post attachment message: ${postResponse.status} $errorBody")
        }
    }

    private fun compressImageIfNeeded(bytes: ByteArray, mimeType: String, context: Context): Pair<ByteArray, String> {
        if (!mimeType.startsWith("image/") || mimeType.contains("svg")) {
            return Pair(bytes, mimeType)
        }
        if (bytes.size <= MAX_FILE_SIZE_BYTES) {
            return Pair(bytes, mimeType)
        }
        try {
            val options = android.graphics.BitmapFactory.Options()
            var bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                ?: return Pair(bytes, mimeType)

            var quality = 70
            var scale = 0.8
            var currentBytes = bytes
            
            while (currentBytes.size > MAX_FILE_SIZE_BYTES && (bitmap.width > 16 || bitmap.height > 16)) {
                val width = (bitmap.width * scale).toInt()
                val height = (bitmap.height * scale).toInt()
                if (width <= 0 || height <= 0) break
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, stream)
                currentBytes = stream.toByteArray()
                
                if (quality > 15) {
                    quality -= 15
                } else {
                    scale = 0.5
                }
            }
            bitmap.recycle()
            if (currentBytes.size <= MAX_FILE_SIZE_BYTES) {
                return Pair(currentBytes, "image/jpeg")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(bytes, mimeType)
    }

    suspend fun clearMessagesForRoom(roomId: String) {
        messageDao.clearMessagesForRoom(roomId)
        _messagesState.update { current ->
            current - roomId
        }
    }

    override suspend fun markAsRead(roomId: String) {
        try {
            // Update local Room database immediately for instant UI update
            chatDao.markChatAsRead(roomId)

            // Notify backend that we read this room
            val response: HttpResponse = client.post("api/v1/subscriptions.read") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rid" to roomId))
            }
            if (response.status.value != 200) {
                android.util.Log.e("ChatRepositoryImpl", "Failed to mark as read on backend: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getMembersCount(roomId: String): Int {
        val response: HttpResponse = client.get("api/v1/rooms.info") {
            parameter("roomId", roomId)
        }
        if (response.status.value == 200) {
            val roomsInfo: RoomInfoResponse = response.body()
            if (roomsInfo.success) {
                return roomsInfo.room.usersCount ?: 0
            }
        }
        throw Exception("Failed to load room info: ${response.status}")
    }
}

@Serializable
data class RoomInfoResponse(
    val room: RoomInfoDetail,
    val success: Boolean
)

@Serializable
data class RoomInfoDetail(
    val _id: String,
    val usersCount: Int? = null
)