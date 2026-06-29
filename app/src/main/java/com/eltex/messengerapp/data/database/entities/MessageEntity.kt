package com.eltex.messengerapp.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import com.eltex.messengerapp.feature.chats.data.MessageDto
import com.eltex.messengerapp.feature.chats.data.UserDto
import kotlinx.serialization.json.JsonElement

@Entity(
    tableName = "messages",
    primaryKeys = ["_id", "rid"],
    indices = [Index(value = ["rid"])]
)
data class MessageEntity(
    val _id: String,
    val rid: String,
    val msg: String? = null,
    val ts: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileType: String? = null,
    val pinned: Boolean = false
) {
    fun toMessageDto(): MessageDto {
        return MessageDto(
            _id = _id,
            rid = rid,
            msg = msg,
            ts = ts as? JsonElement,
            u = if (userId != null) UserDto(_id = userId, username = username) else null,
            attachments = if (fileUrl != null) {
                listOf(
                    com.eltex.messengerapp.feature.chats.data.AttachmentDto(
                        title = fileName,
                        image_url = if (fileType == "image") fileUrl else null,
                        video_url = if (fileType == "video") fileUrl else null
                    )
                )
            } else null,
            unread = null
        )
    }
}