package com.eltex.messengerapp.feature.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltex.messengerapp.feature.auth.domain.AuthRepository
import com.eltex.messengerapp.feature.auth.domain.validateLogin
import com.eltex.messengerapp.feature.auth.domain.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.eltex.messengerapp.datastore.AuthDataStore

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    var state by mutableStateOf(AuthState())
        private set

    private val _effects = MutableSharedFlow<AuthEffect>(
        extraBufferCapacity = 1
    )
    val effects = _effects.asSharedFlow()

    fun accept(message: AuthMessage) {
        when (message) {
            is AuthMessage.LoginChanged -> {
                state = state.copy(login = message.value, loginError = null)
            }

            is AuthMessage.PasswordChanged -> {
                state = state.copy(password = message.value, passwordError = null)
            }

            AuthMessage.Submit -> {
                val loginError = validateLogin(state.login)
                val passwordError = validatePassword(state.password)

                state = state.copy(
                    loginError = loginError,
                    passwordError = passwordError
                )

                if (loginError == null && passwordError == null) {
                    viewModelScope.launch {
                        val result = repository.login(state.login, state.password)
                        result.fold(
                            onSuccess = { authResult ->
                                authDataStore.saveAuthData(
                                    token = authResult.authToken,
                                    userId = authResult.userId
                                )
                                _effects.tryEmit(AuthEffect.ShowSuccess)
                            },
                            onFailure = { error ->
                                val message = handleError(error)
                                _effects.tryEmit(AuthEffect.ShowError(message))
                            }
                        )
                    }
                }
            }
        }
    }
    private fun handleError(error: Throwable): String {
        return when (error) {
            is java.net.UnknownHostException,
            is java.net.ConnectException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> {
                "Отсутствует соединение с сервером, проверьте ваше интернет соединение и повторите позднее"
            }

            else -> {
                if (error.message?.contains("401") == true ||
                    error.message?.contains("403") == true
                ) {
                    "Неправильный логин или пароль"
                } else {
                    error.message ?: "Неизвестная ошибка"
                }
            }
        }
    }
}
