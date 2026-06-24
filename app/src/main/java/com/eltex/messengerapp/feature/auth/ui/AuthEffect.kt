package com.eltex.messengerapp.feature.auth.ui

sealed interface AuthEffect {
    data object ShowSuccess : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}