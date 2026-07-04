package com.eltex.messengerapp.feature.chats.creation.dm.ui

sealed interface DmCreationEffect {
    data class OpenChat(
        val chatId: String,
        val chatName: String,
        val avatarName: String
    ) : DmCreationEffect

    data class ShowError(val message: String) : DmCreationEffect
    object CloseScreen : DmCreationEffect
}