package com.eltex.messengerapp.feature.chat.domain

import com.eltex.messengerapp.feature.chats.data.MessageDto
import kotlinx.coroutines.flow.Flow
import android.net.Uri
import android.content.Context

interface ChatRepository {
    fun getMessagesFlow(roomId: String, roomType: String): Flow<List<MessageDto>>
    suspend fun loadHistory(roomId: String, roomType: String)
    suspend fun sendMessage(roomId: String, text: String)
    suspend fun uploadFile(roomId: String, fileUri: Uri, context: Context, msg: String? = null, description: String? = null)
}
