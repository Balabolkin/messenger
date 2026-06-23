package com.eltex.messengerapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
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

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun logout() {
        clear()
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}