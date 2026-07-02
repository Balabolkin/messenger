package com.eltex.messengerapp.feature.user.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UsersAutocompleteResponse(
    val items: List<UserDto>,
    val success: Boolean
)

@Serializable
data class UsersListResponse(
    val users: List<UserDto>,
    val count: Int,
    val offset: Int,
    val total: Int,
    val success: Boolean
)

@Singleton
class UsersApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun searchUsers(query: String): UsersAutocompleteResponse {
        return client.get {
            url("api/v1/users.autocomplete")
            parameter("selector", """{"term":"$query"}""")
        }.body()
    }

    suspend fun getUsers(): UsersListResponse {
        val response = client.get {
            url("api/v1/users.list")
            parameter("count", 20)
        }
        return response.body()
    }
}
