package com.eltex.messengerapp.feature.chat.ui

import com.eltex.messengerapp.feature.chats.data.MessageDto

import android.net.Uri

data class ChatState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloadingFiles: Map<String, Float?> = emptyMap(), // Map of URL to progress (null if indeterminate loader)
    val isSending: Boolean = false,
    val isAttachmentPanelOpen: Boolean = false,
    val activeTab: Int = 0, // 0 for Photos, 1 for Videos, 2 for Documents
    val selectedMedia: List<Uri> = emptyList(),
    val localMediaList: List<Uri> = emptyList(),
    val hasMediaPermission: Boolean = false,
    val membersCount: Int? = null
)
