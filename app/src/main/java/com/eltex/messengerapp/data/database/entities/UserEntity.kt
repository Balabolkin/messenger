package com.eltex.messengerapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eltex.messengerapp.feature.user.domain.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val _id: String,
    val name: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null
) {
    fun toDomain(): User {
        return User(
            id = _id,
            firstName = firstName ?: "",
            lastName = lastName ?: name ?: "",
            avatarUrl = avatarUrl
        )
    }
}