package com.eltex.messengerapp.feature.auth.data

import com.eltex.messengerapp.feature.auth.domain.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class AuthResponse(
    val status: String,
    val data: AuthData
)

@Serializable
data class AuthData(
    val authToken: String
)
class AuthRepositoryImpl @Inject constructor(
    private val client: HttpClient,
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<String> {
        return try {
            val response: HttpResponse = client.post("login")
            {
                setBody(mapOf(
                    "user" to login,
                    "password" to password
                ))
            }

            if (response.status.value == 200) {
                val authResponse: AuthResponse = response.body()
                val token = authResponse.data.authToken
                Result.success(token)
            } else {
                val error = response.body<String>()
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
