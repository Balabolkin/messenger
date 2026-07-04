package com.eltex.messengerapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.data.SubscriptionDto

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val _id: String,
    val rid: String,                         // ← ДОБАВЛЯЕМ!
    val name: String? = null,
    val fname: String? = null,
    val t: String? = null,
    val unread: Int? = 0,
    val alert: Boolean? = false,
    val ts: String? = null,
    val ls: String? = null,
    val lastMessageText: String? = null,
    val lastMessageTs: String? = null,
    val lastMessageUserId: String? = null,
    val lastMessageUsername: String? = null
) {
    fun toSubscriptionDto(): SubscriptionDto {
        val lastMessage = if (lastMessageText != null) {
            MessageDto(
                _id = "temp",
                rid = rid,
                msg = lastMessageText,
                ts = lastMessageTs?.let { kotlinx.serialization.json.JsonPrimitive(it) },
                u = if (lastMessageUserId != null) {
                    com.eltex.messengerapp.feature.chats.data.UserDto(
                        _id = lastMessageUserId,
                        username = lastMessageUsername
                    )
                } else null,
                attachments = null,
                unread = null
            )
        } else null

        return SubscriptionDto(
            _id = _id,
            rid = rid,
            name = name,
            fname = fname,
            t = t ?: "c",
            unread = unread ?: 0,
            alert = alert ?: false,
            ts = ts?.let { kotlinx.serialization.json.JsonPrimitive(it) },
            ls = ls?.let { kotlinx.serialization.json.JsonPrimitive(it) },
            lastMessage = lastMessage
        )
    }
}