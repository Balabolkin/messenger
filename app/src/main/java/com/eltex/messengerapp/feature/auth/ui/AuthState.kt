package com.eltex.messengerapp.feature.auth.ui

import com.eltex.messengerapp.feature.auth.domain.LoginError
import com.eltex.messengerapp.feature.auth.domain.PasswordError

data class AuthState(
    val login: String = "",
    val password: String = "",
    val loginError: LoginError? = null,
    val passwordError: PasswordError? = null,
)