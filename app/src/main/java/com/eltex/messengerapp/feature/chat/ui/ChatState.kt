package com.eltex.messengerapp.feature.chat.ui

import com.eltex.messengerapp.feature.chats.data.MessageDto

data class ChatState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloadingFiles: Map<String, Float?> = emptyMap() // Map of URL to progress (null if indeterminate loader)
)
