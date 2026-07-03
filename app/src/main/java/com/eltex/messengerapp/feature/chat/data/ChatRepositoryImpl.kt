package com.eltex.messengerapp.feature.chat.data

import android.util.Log.e
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
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri
import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
data class PostMessageRequest(
    val roomId: String,
    val text: String
)

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val messageDao: MessageDao,
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
                parameter("count", 100)
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

        val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val bytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            ?: throw Exception("Cannot read file content")

        val response: HttpResponse = client.post("api/v1/rooms.upload/$roomId") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                    if (msg != null) {
                        append("msg", msg)
                    }
                    if (description != null) {
                        append("description", description)
                    }
                }
            ))
        }

        if (response.status.value != 200) {
            throw Exception("Failed to upload file: ${response.status}")
        }
    }

    suspend fun clearMessagesForRoom(roomId: String) {
        messageDao.clearMessagesForRoom(roomId)
        _messagesState.update { current ->
            current - roomId
        }
    }
}