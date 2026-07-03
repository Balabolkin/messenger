package com.eltex.messengerapp.feature.user.domain

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val patronymic: String? = null,
    val username: String,
    val avatarUrl: String? = null
) {
    fun getFullName(): String {
        return buildString {
            append(lastName)
            append(" ")
            append(firstName)
            patronymic?.let {
                append(" ")
                append(patronymic)
            }
        }
    }

    fun getInitials(): String {
        return buildString {
            append(lastName.firstOrNull()?.uppercaseChar() ?: "")
            append(" ")
            append(firstName.firstOrNull()?.uppercaseChar() ?: "")
        }
    }
}