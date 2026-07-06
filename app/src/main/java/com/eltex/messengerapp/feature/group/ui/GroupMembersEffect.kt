package com.eltex.messengerapp.feature.group.ui

sealed interface GroupMembersEffect {
    data object NavigateBack : GroupMembersEffect
    data class ShowError(val message: String) : GroupMembersEffect
    data class NavigateToProfile(val userId: String) : GroupMembersEffect
}