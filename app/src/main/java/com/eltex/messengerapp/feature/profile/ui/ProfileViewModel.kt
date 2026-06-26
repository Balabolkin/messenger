package com.eltex.messengerapp.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltex.messengerapp.feature.profile.domain.ProfileRepository
import com.eltex.messengerapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadUser()
    }

    fun messageHandler(message: ProfileMessage) {
        when (message) {
            ProfileMessage.LoadUser -> loadUser()
            ProfileMessage.Logout -> logout()
            ProfileMessage.DismissLogoutDialog -> dismissLogoutDialog()
            ProfileMessage.ShowLogoutDialog -> showLogoutDialog()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            profileRepository.getUser().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                user = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is Result.Loading -> {}
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
//                        _effect.emit(ProfileEffect.ShowError(result.message))
                    }
                }
            }
        }
    }

    private fun showLogoutDialog() {
        _state.update { it.copy(isLogoutDialogVisible = true) }
    }

    private fun dismissLogoutDialog() {
        _state.update { it.copy(isLogoutDialogVisible = false) }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isLogoutDialogVisible = false) }

            try {
                profileRepository.logout()
                _effect.emit(ProfileEffect.NavigateToLogin)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка выхода: ${e.message}"
                    )
                }
            }
        }
    }

}