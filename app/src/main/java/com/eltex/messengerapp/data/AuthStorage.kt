package com.eltex.messengerapp.data

import kotlinx.coroutines.flow.Flow

interface AuthStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    fun observeToken(): Flow<String?>
}