package com.eltex.messengerapp.feature.chats.domain

import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.data.SubscriptionDto
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
    val chatsFlow: Flow<List<SubscriptionDto>>
    suspend fun loadNextPage()
    suspend fun refresh()
    fun startRealtimeUpdates()
    fun stopRealtimeUpdates()
    suspend fun searchChats(query: String): List<SubscriptionDto>
    fun subscribeToRoomMessages(roomId: String, callback: (MessageDto) -> Unit)
    fun unsubscribeFromRoomMessages(roomId: String)
    suspend fun createRoom(name: String, type: String)
    suspend fun searchUsers(query: String): List<UserDto>
    suspend fun createDirectMessage(username: String)
}

@kotlinx.serialization.Serializable
data class UserDto(
    val _id: String,
    val username: String? = null,
    val name: String? = null,
    val status: String? = null
)
