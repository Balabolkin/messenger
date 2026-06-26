package com.eltex.messengerapp.feature.profile.ui

import com.eltex.messengerapp.feature.user.domain.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isLogoutDialogVisible: Boolean = false,
    val error: String? = null
)