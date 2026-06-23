package com.eltex.messengerapp.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltex.messengerapp.NavDestinations
import com.eltex.messengerapp.datastore.AuthDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _startDestination = MutableStateFlow<Any?>(null)
    val startDestination = _startDestination.asStateFlow()

    fun checkAuth() {
        viewModelScope.launch {
            val isLoggedIn = authDataStore.isLoggedIn().first()

            _startDestination.value = if (isLoggedIn) {
                NavDestinations.Main
            } else {
                NavDestinations.Auth
            }

            _isReady.value = true
        }
    }
}