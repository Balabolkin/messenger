package com.eltex.messengerapp.feature.user.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApi @Inject constructor(
    private val client: HttpClient,
) {

    suspend fun getUserInfo(
        userId: String,
    ): UserInfoResponseDto {
        return client.get {
            url("api/v1/users.info")
            parameter("userId", userId)
        }.body()
    }
}