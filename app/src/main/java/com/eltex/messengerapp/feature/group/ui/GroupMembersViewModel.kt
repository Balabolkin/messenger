package com.eltex.messengerapp.feature.group.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eltex.messengerapp.NavDestinations
import com.eltex.messengerapp.R
import com.eltex.messengerapp.feature.group.domain.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupMembersViewModel @Inject constructor(
    private val repository: GroupRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId = savedStateHandle.toRoute<NavDestinations.GroupMembers>().roomId
    private val roomType = savedStateHandle.toRoute<NavDestinations.GroupMembers>().roomType
    private val _state = MutableStateFlow(GroupMembersState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<GroupMembersEffect>()
    val effects = _effects.asSharedFlow()

    init {
        loadGroupData()
    }

    private fun loadGroupData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val groupInfo = repository.getGroupInfo(roomId, roomType)
                val members = repository.getGroupMembers(roomId, roomType)

                _state.update {
                    it.copy(
                        isLoading = false,
                        groupInfo = groupInfo,
                        members = members
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    "LOAD_GROUP_INFO_ERROR" -> context.getString(R.string.error_load_group_info)
                    "LOAD_GROUP_MEMBERS_ERROR" -> context.getString(R.string.error_load_group_members)
                    else -> e.message ?: context.getString(R.string.error_loading)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                _effects.emit(GroupMembersEffect.ShowError(errorMessage))
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _effects.emit(GroupMembersEffect.NavigateBack)
        }
    }

    fun onMemberClicked(memberId: String) {
        viewModelScope.launch {
            _effects.emit(GroupMembersEffect.NavigateToProfile(memberId))
        }
    }
}