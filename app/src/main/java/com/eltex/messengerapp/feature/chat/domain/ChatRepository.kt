package com.eltex.messengerapp.feature.chat.domain

import com.eltex.messengerapp.feature.chats.data.MessageDto
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessagesFlow(roomId: String, roomType: String): Flow<List<MessageDto>>
    suspend fun loadHistory(roomId: String, roomType: String)
}
