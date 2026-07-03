package com.eltex.messengerapp.feature.user.data

import com.eltex.messengerapp.feature.user.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponseDto(
    val user: UserDto,
    val success: Boolean
)

@Serializable
data class UserDto(
    val _id: String,
    val name: String?,
    val username: String,
    val avatarETag: String? = null,
    val emails: List<EmailDto>? = null
) {
    fun toDomain(): User {
        val nameParts = name?.split(" ") ?: emptyList()
        val lastName = nameParts.getOrNull(0) ?: ""
        val firstName = nameParts.getOrNull(1) ?: ""
        val patronymic = nameParts.getOrNull(2)

        return User(
            id = _id,
            firstName = firstName,
            lastName = lastName,
            patronymic = patronymic,
            username = username,
            avatarUrl = null
        )
    }
}

@Serializable
data class EmailDto(
    val address: String,
    val verified: Boolean = false
)