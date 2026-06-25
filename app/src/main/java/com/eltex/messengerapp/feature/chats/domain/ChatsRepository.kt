package com.eltex.messengerapp.feature.chats.domain

import com.eltex.messengerapp.feature.chats.data.SubscriptionDto
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
    val chatsFlow: Flow<List<SubscriptionDto>>
    suspend fun loadNextPage()
    suspend fun refresh()
    fun startRealtimeUpdates()
    fun stopRealtimeUpdates()
    suspend fun searchChats(query: String): List<SubscriptionDto>
}
