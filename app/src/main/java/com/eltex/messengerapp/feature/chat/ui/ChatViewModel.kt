package com.eltex.messengerapp.feature.chat.ui

import android.content.Context
import android.net.Uri
import android.content.ContentUris
import android.provider.MediaStore
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
import com.eltex.messengerapp.util.toSecureUrl

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
            if (roomType != "d") {
                loadMembersCount()
            }
        }
    }

    private fun loadMembersCount() {
        viewModelScope.launch {
            try {
                val count = chatRepository.getMembersCount(roomId)
                _state.update { it.copy(membersCount = count) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            try {
                chatRepository.markAsRead(roomId)
            } catch (e: Exception) {
                e.printStackTrace()
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
                val fullUrl = toSecureUrl(url) ?: ""
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

    fun setAttachmentPanelOpen(open: Boolean) {
        _state.update { it.copy(isAttachmentPanelOpen = open) }
        if (!open) {
            clearSelection()
        }
    }

    fun setActiveTab(tab: Int, context: Context) {
        _state.update { it.copy(activeTab = tab, selectedMedia = emptyList()) }
        if (_state.value.hasMediaPermission) {
            loadLocalMedia(context)
        }
    }

    fun toggleMediaSelection(uri: Uri) {
        _state.update { current ->
            val list = current.selectedMedia.toMutableList()
            if (list.contains(uri)) {
                list.remove(uri)
            } else {
                list.add(uri)
            }
            current.copy(selectedMedia = list)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedMedia = emptyList()) }
    }

    fun updatePermissionState(granted: Boolean, context: Context) {
        _state.update { it.copy(hasMediaPermission = granted) }
        if (granted) {
            loadLocalMedia(context)
        }
    }

    fun loadLocalMedia(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mediaList = mutableListOf<Uri>()
                val activeTab = _state.value.activeTab
                
                if (activeTab == 0) { // Photos
                    val projection = arrayOf(MediaStore.Images.Media._ID)
                    val cursor = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        "${MediaStore.Images.Media.DATE_ADDED} DESC"
                    )
                    cursor?.use {
                        val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        while (it.moveToNext()) {
                            val id = it.getLong(idCol)
                            val uri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                            mediaList.add(uri)
                        }
                    }
                } else if (activeTab == 1) { // Videos
                    val projection = arrayOf(MediaStore.Video.Media._ID)
                    val cursor = context.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        "${MediaStore.Video.Media.DATE_ADDED} DESC"
                    )
                    cursor?.use {
                        val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                        while (it.moveToNext()) {
                            val id = it.getLong(idCol)
                            val uri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                            mediaList.add(uri)
                        }
                    }
                }
                
                _state.update { it.copy(localMediaList = mediaList) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendTextOrMedia(context: Context, text: String, onSentSuccess: () -> Unit) {
        val trimmed = text.trim()
        val mediaList = _state.value.selectedMedia

        if (trimmed.isEmpty() && mediaList.isEmpty()) return

        _state.update { it.copy(isSending = true, error = null) }

        viewModelScope.launch {
            try {
                if (mediaList.isEmpty()) {
                    chatRepository.sendMessage(roomId, text)
                } else {
                    for (i in mediaList.indices) {
                        val mediaUri = mediaList[i]
                        val isLast = i == mediaList.size - 1
                        val msgText = if (isLast && trimmed.isNotEmpty()) text else null
                        chatRepository.uploadFile(roomId, mediaUri, context, msgText, null)
                    }
                }
                clearSelection()
                _state.update { it.copy(isAttachmentPanelOpen = false) }
                withContext(Dispatchers.Main) {
                    onSentSuccess()
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Ошибка при отправке: ${e.message}") }
            } finally {
                _state.update { it.copy(isSending = false) }
            }
        }
    }

    fun sendDocument(context: Context, uri: Uri) {
        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            try {
                chatRepository.uploadFile(roomId, uri, context, null, null)
                _state.update { it.copy(isAttachmentPanelOpen = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Ошибка при отправке документа: ${e.message}") }
            } finally {
                _state.update { it.copy(isSending = false) }
            }
        }
    }
}
