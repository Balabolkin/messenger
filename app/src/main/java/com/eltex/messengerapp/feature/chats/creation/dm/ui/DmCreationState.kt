package com.eltex.messengerapp.feature.chats.creation.dm.ui

import com.eltex.messengerapp.feature.user.domain.User

data class DmCreationState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
    val isCreating: Boolean = false
)