package com.eltex.messengerapp.feature.chats.ui

import com.eltex.messengerapp.feature.chats.data.SubscriptionDto

data class ChatsState(
    val chats: List<SubscriptionDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null
)
