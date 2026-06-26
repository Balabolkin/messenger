package com.eltex.messengerapp.feature.profile.ui

sealed interface ProfileEffect {
    object NavigateToLogin : ProfileEffect
//    data class ShowError(val message: String): ProfileEffect
}