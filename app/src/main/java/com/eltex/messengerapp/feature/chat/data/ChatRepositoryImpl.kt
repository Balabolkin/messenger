package com.eltex.messengerapp.feature.chat.data

import com.eltex.messengerapp.feature.chat.domain.ChatRepository
import com.eltex.messengerapp.feature.chats.data.HistoryResponseDto
import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.domain.ChatsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val chatsRepository: ChatsRepository
) : ChatRepository {

    private val _messagesState = MutableStateFlow<Map<String, List<MessageDto>>>(emptyMap())

    override fun getMessagesFlow(roomId: String, roomType: String): Flow<List<MessageDto>> {
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

        val response: HttpResponse = client.get(endpoint) {
            parameter("roomId", roomId)
            parameter("count", 100)
        }

        if (response.status.value == 200) {
            val historyResponse: HistoryResponseDto = response.body()
            if (historyResponse.success) {
                val messages = historyResponse.messages
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
    }
}
