package com.eltex.messengerapp.feature.chats.creation.dm.ui

import com.eltex.messengerapp.feature.user.domain.User

sealed interface DmCreationMessage {
    object LoadUsers : DmCreationMessage
    data class SearchQueryChanged(val query: String) : DmCreationMessage
    data class SelectUser(val user: User) : DmCreationMessage
    object DismissError : DmCreationMessage
}