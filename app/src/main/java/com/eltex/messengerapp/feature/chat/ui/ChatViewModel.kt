package com.eltex.messengerapp.feature.chat.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eltex.messengerapp.NavDestinations
import com.eltex.messengerapp.datastore.AuthDataStore
import com.eltex.messengerapp.feature.chat.domain.ChatRepository
import com.eltex.messengerapp.feature.chats.data.MessageDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authDataStore: AuthDataStore,
    private val okHttpClient: OkHttpClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatRoute = savedStateHandle.toRoute<NavDestinations.Chat>()
    val roomId = chatRoute.roomId
    val roomName = chatRoute.roomName
    val roomType = chatRoute.roomType

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUserId.value = authDataStore.getUserId().first()
            loadMessages()
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                chatRepository.loadHistory(roomId, roomType)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }

        viewModelScope.launch {
            chatRepository.getMessagesFlow(roomId, roomType).collect { msgs ->
                _state.update { it.copy(messages = msgs) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun downloadAndOpenFile(
        context: Context,
        url: String,
        fileName: String,
        onComplete: (File) -> Unit
    ) {
        if (_state.value.downloadingFiles.containsKey(url)) return

        val cacheDir = context.cacheDir
        val localFile = File(cacheDir, fileName)
        if (localFile.exists()) {
            onComplete(localFile)
            return
        }

        _state.update { it.copy(downloadingFiles = it.downloadingFiles + (url to 0f)) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fullUrl = if (url.startsWith("http")) url else "https://study-chat.eltex-co.ru$url"
                val request = okhttp3.Request.Builder().url(fullUrl).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Ошибка сервера: ${response.code}")
                    val body = response.body ?: throw Exception("Тело ответа пустое")
                    val contentLength = body.contentLength()

                    body.byteStream().use { inputStream ->
                        localFile.outputStream().use { outputStream ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                if (contentLength > 0) {
                                    val progress = totalBytesRead.toFloat() / contentLength
                                    _state.update { it.copy(downloadingFiles = it.downloadingFiles + (url to progress)) }
                                }
                              }
                        }
                    }

                    _state.update { it.copy(downloadingFiles = it.downloadingFiles - url) }
                    withContext(Dispatchers.Main) {
                        onComplete(localFile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        downloadingFiles = it.downloadingFiles - url,
                        error = "Ошибка при загрузке файла: ${e.message}"
                    )
                }
            }
        }
    }
}
