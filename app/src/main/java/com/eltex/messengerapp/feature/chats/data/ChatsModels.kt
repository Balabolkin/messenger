package com.eltex.messengerapp.feature.chats.data

import com.eltex.messengerapp.data.database.entities.ChatEntity
import com.eltex.messengerapp.data.database.entities.MessageEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant

@Serializable
data class UserDto(
    val _id: String? = null,
    val username: String? = null,
    val name: String? = null
)

@Serializable
data class AttachmentDto(
    val title: String? = null,
    val image_url: String? = null,
    val video_url: String? = null,
    val audio_url: String? = null,
    val description: String? = null,
    val title_link: String? = null
)

@Serializable
data class MessageDto(
    val _id: String,
    val rid: String? = null,
    val msg: String? = null,
    val ts: JsonElement? = null,
    val u: UserDto? = null,
    val attachments: List<AttachmentDto>? = null,
    val unread: Boolean? = null
) {
    fun toEntity(roomId: String): MessageEntity {
        return MessageEntity(
            _id = _id,
            rid = roomId,
            msg = msg,
            ts = ChatsDateParser.parse(ts)?.toString(),
            userId = u?._id,
            username = u?.username,
            fileUrl = attachments?.firstOrNull()?.image_url
                ?: attachments?.firstOrNull()?.video_url,
            fileName = attachments?.firstOrNull()?.title,
            fileType = when {
                attachments?.firstOrNull()?.image_url != null -> "image"
                attachments?.firstOrNull()?.video_url != null -> "video"
                else -> null
            },
            pinned = false
        )
    }
}


@Serializable
data class SubscriptionDto(
    val _id: String,
    val rid: String,
    val name: String? = null,
    val fname: String? = null,
    val t: String, // "d" (direct), "c" (channel), "p" (group)
    val unread: Int = 0,
    val alert: Boolean = false,
    val ts: JsonElement? = null,
    val ls: JsonElement? = null,
    val lastMessage: MessageDto? = null
){
    fun toEntity(): ChatEntity {
        return ChatEntity(
            _id = _id,
            rid = rid,
            name = name,
            fname = fname,
            t = t,
            unread = unread,
            alert = alert,
            ts = ChatsDateParser.parse(ts)?.toString(),
            ls = ChatsDateParser.parse(ls)?.toString(),
            lastMessageText = lastMessage?.msg,
            lastMessageTs = ChatsDateParser.parse(lastMessage?.ts)?.toString(),
            lastMessageUserId = lastMessage?.u?._id,
            lastMessageUsername = lastMessage?.u?.username
        )
    }
}

@Serializable
data class SubscriptionsResponse(
    val update: List<SubscriptionDto>? = null,
    val remove: List<SubscriptionDto>? = null,
    val success: Boolean
)

@Serializable
data class RoomDto(
    val _id: String,
    val lastMessage: MessageDto? = null
)

@Serializable
data class RoomsResponse(
    val update: List<RoomDto>? = null,
    val remove: List<RoomDto>? = null,
    val success: Boolean
)

@Serializable
data class HistoryResponseDto(
    val messages: List<MessageDto>,
    val success: Boolean
)

@Serializable
data class DdpMessage(
    val msg: String, // e.g., "connect", "connected", "ping", "pong", "method", "result", "sub", "ready", "changed"
    val id: String? = null,
    val collection: String? = null,
    val fields: DdpFields? = null,
    val result: JsonElement? = null,
    val error: DdpError? = null
)

@Serializable
data class DdpFields(
    val eventName: String? = null,
    val args: List<JsonElement>? = null
)

@Serializable
data class DdpError(
    val message: String? = null,
    val error: String? = null
)

object ChatsDateParser {
    fun parse(element: JsonElement?): Long? {
        if (element == null) return null
        if (element is JsonPrimitive) {
            val str = element.content
            return try {
                Instant.parse(str).toEpochMilli()
            } catch (_: Exception) {
                str.toLongOrNull()
            }
        }
        if (element is JsonObject) {
            val dateVal = element["\$date"]
            if (dateVal is JsonPrimitive) {
                return dateVal.content.toLongOrNull()
            }
        }
        return null
    }
}
