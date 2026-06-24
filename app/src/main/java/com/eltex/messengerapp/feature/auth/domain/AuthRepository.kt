package com.eltex.messengerapp.feature.auth.domain

data class AuthResult(
    val authToken: String,
    val userId: String
)
interface AuthRepository {
    suspend fun login(login: String, password: String): Result<AuthResult>
}