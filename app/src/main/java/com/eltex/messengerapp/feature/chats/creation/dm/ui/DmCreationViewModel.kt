package com.eltex.messengerapp.feature.chats.creation.dm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltex.messengerapp.feature.chats.creation.dm.domain.DmCreationRepository
import com.eltex.messengerapp.feature.user.domain.User
import com.eltex.messengerapp.feature.user.domain.UsersRepository
import com.eltex.messengerapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class DmCreationViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val dmCreationRepository: DmCreationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DmCreationState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DmCreationEffect>()
    val effect = _effect.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadUsers()
    }

    fun messageHandler(message: DmCreationMessage) {
        when (message) {
            DmCreationMessage.DismissError -> dismissError()
            DmCreationMessage.LoadUsers -> loadUsers()
            is DmCreationMessage.SearchQueryChanged -> onSearchQueryChanged(message.query)
            is DmCreationMessage.SelectUser -> selectUser(message.user)
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = usersRepository.getUsers()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            users = result.data,
                            isLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось загрузить пользователей"
                        )
                    }
                }

                Result.Loading -> {}
            }
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300.milliseconds)
            if (query.length >= 2) {
                when (val result = usersRepository.searchUsers(query)) {
                    is Result.Success -> {
                        _state.update { it.copy(users = result.data) }
                    }

                    is Result.Error -> {
                        _state.update { it.copy(error = "Ошибка поиска") }
                    }

                    Result.Loading -> {}
                }
            } else {
                loadUsers()
            }
        }
    }

    private fun selectUser(user: User) {
        viewModelScope.launch {
            val username = user.username

            if (username.isEmpty()) {
                _effect.emit(DmCreationEffect.ShowError("Не удалось создать чат: отсутствует username"))
                return@launch
            }

            _state.update { it.copy(isCreating = true) }

            when (val result = dmCreationRepository.createDm(username)) {
                is Result.Success -> {
                    _effect.emit(DmCreationEffect.OpenChat(result.data.rid, user.getFullName(), user.username))
                }

                is Result.Error -> {
                    _effect.emit(DmCreationEffect.ShowError(result.message))
                }

                Result.Loading -> {}
            }

            _state.update { it.copy(isCreating = false) }
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

}