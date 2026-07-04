package com.eltex.messengerapp.feature.chats.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltex.messengerapp.datastore.AuthDataStore
import com.eltex.messengerapp.feature.chats.data.ChatsDateParser
import com.eltex.messengerapp.feature.chats.data.SubscriptionDto
import com.eltex.messengerapp.feature.chats.domain.ChatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: ChatsRepository,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    var state by mutableStateOf(ChatsState())
        private set

    private var searchJob: Job? = null
    private var searchResults by mutableStateOf<List<SubscriptionDto>>(emptyList())

    private val ChatsComparator = Comparator<SubscriptionDto> { o1, o2 ->
        val t1 = ChatsDateParser.parse(o1.lastMessage?.ts) ?: ChatsDateParser.parse(o1.ls) ?: ChatsDateParser.parse(o1.ts) ?: 0L
        val t2 = ChatsDateParser.parse(o2.lastMessage?.ts) ?: ChatsDateParser.parse(o2.ls) ?: ChatsDateParser.parse(o2.ts) ?: 0L
        t2.compareTo(t1) // Descending (latest first)
    }

    init {
        viewModelScope.launch {
            val userId = authDataStore.getUserId().first()
            state = state.copy(currentUserId = userId)
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)
            repository.chatsFlow.collectLatest { list ->
                state = state.copy(
                    chats = list,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            try {
                repository.refresh()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun onForeground() {
        viewModelScope.launch {
            try {
                repository.refresh()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun onBackground() {
    }

    fun loadNextPage() {
        if (state.searchQuery.length >= 2) return
        if (state.isPaginating) return
        state = state.copy(isPaginating = true)
        viewModelScope.launch {
            try {
                repository.loadNextPage()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            } finally {
                state = state.copy(isPaginating = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.length < 2) {
            searchResults = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            state = state.copy(isLoading = true)
            try {
                val results = repository.searchChats(query)
                searchResults = results
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun getDisplayedChats(): List<SubscriptionDto> {
        val query = state.searchQuery
        if (query.length < 2) {
            return state.chats
        }
        val localFiltered = state.chats.filter {
            it.fname?.contains(query, ignoreCase = true) == true ||
            it.name?.contains(query, ignoreCase = true) == true
        }
        return (localFiltered + searchResults)
            .distinctBy { it._id }
            .sortedWith(ChatsComparator)
    }

    fun clearError() {
        state = state.copy(error = null)
    }



    override fun onCleared() {
        super.onCleared()
    }
}
