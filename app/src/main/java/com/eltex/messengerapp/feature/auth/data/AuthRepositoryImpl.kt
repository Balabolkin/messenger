package com.eltex.messengerapp.feature.auth.data

import com.eltex.messengerapp.feature.auth.domain.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: HttpClient,
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.post("login")
            {
                setBody(mapOf(
                    "user" to login,
                    "password" to password
                ))
            }
            if (response.status.value == 200) {
                val token = response.headers["Authorization"] ?: response.body<String>()
                Result.success(Unit)
            } else {
                val error = response.body<String>()
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
