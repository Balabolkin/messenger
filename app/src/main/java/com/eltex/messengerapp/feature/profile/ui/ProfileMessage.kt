package com.eltex.messengerapp.feature.profile.ui

sealed interface ProfileMessage {
    object LoadUser: ProfileMessage
    object ShowLogoutDialog: ProfileMessage
    object DismissLogoutDialog: ProfileMessage
    object Logout: ProfileMessage
}