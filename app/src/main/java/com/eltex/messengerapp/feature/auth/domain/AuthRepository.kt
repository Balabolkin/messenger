package com.eltex.messengerapp.feature.auth.domain

interface AuthRepository {
    suspend fun login(login: String, password: String): Result<String>
}