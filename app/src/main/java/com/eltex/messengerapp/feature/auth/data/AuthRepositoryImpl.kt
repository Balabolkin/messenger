package com.eltex.messengerapp.feature.auth.data

import com.eltex.messengerapp.feature.auth.domain.AuthRepository
import com.eltex.messengerapp.feature.auth.domain.AuthResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class AuthResponse(
    val status: String,
    val data: AuthData
)

@Serializable
data class AuthData(
    val authToken: String,
    val userId: String
)
class AuthRepositoryImpl @Inject constructor(
    private val client: HttpClient,
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<AuthResult> {
        return try {
            val response: HttpResponse = client.post("api/v1/login")
            {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "user" to login,
                    "password" to password
                ))
            }

            if (response.status.value == 200) {
                val authResponse: AuthResponse = response.body()
                Result.success(AuthResult(
                    authToken = authResponse.data.authToken,
                    userId = authResponse.data.userId
                )
                )
            } else {
                val error = response.body<String>()
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
