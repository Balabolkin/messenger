package com.eltex.messengerapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eltex.messengerapp.data.database.dao.ChatDao
import com.eltex.messengerapp.data.database.dao.MessageDao
import com.eltex.messengerapp.data.database.dao.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var cachedUserId: String? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            dataStore.data.collect { prefs ->
                cachedToken = prefs[TOKEN_KEY]
                cachedUserId = prefs[USER_ID_KEY]
            }
        }
    }

    fun getCachedToken(): String? = cachedToken
    fun getCachedUserId(): String? = cachedUserId

    suspend fun saveAuthData(token: String, userId: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
        }
        cachedToken = token
        cachedUserId = userId
    }
    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
        cachedToken = token
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
        cachedUserId = userId
    }

    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            !prefs[TOKEN_KEY].isNullOrEmpty()
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    fun getUserId(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
        }
        cachedToken = null
        cachedUserId = null
    }

    suspend fun logout() {
        clear()
    }

    suspend fun clearAllData(
        chatDao: ChatDao,
        messageDao: MessageDao,
        userDao: UserDao
    ) {
        clear()

        chatDao.clearChats()
        messageDao.clearAllMessages()
        userDao.clearUsers()
    }


    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }
}