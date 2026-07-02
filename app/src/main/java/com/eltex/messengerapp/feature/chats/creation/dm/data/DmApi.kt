package com.eltex.messengerapp.feature.chats.creation.dm.data

import com.eltex.messengerapp.feature.room.data.CreateDmResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DmApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun createDm(username: String): CreateDmResponse {
        return client.post {
            url("api/v1/dm.create")
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username"}""")
        }.body()
    }
}