package com.eltex.messengerapp.feature.room.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateDmResponse(
    val room: RoomDto? = null,
    val success: Boolean,
    val error: String? = null
)

@Serializable
data class RoomDto(
    val _id: String,
    val t: String,
    val name: String? = null,
    val msgs: Int? = null,
    val ts: String? = null
)