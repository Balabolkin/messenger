package com.eltex.messengerapp.feature.group.data

import com.eltex.messengerapp.data.Constants
import com.eltex.messengerapp.feature.group.domain.GroupInfo
import com.eltex.messengerapp.feature.group.domain.GroupMember
import com.eltex.messengerapp.feature.group.domain.MemberRole
import kotlinx.serialization.Serializable

@Serializable
data class GroupMemberDto(
    val _id: String,
    val username: String,
    val name: String? = null,
    val avatarETag: String? = null,
    val roles: List<String>? = emptyList()
) {
    fun toDomain(): GroupMember {
        val role = when {
            roles?.contains(Constants.ROLE_ADMIN) == true -> MemberRole.ADMIN
            else -> MemberRole.MEMBER
        }
        return GroupMember(
            id = _id,
            username = username,
            name = name,
            avatarUrl = avatarETag?.let { "${Constants.BASE_URL}avatar/$username" },
            role = role
        )
    }
}

@Serializable
data class GroupInfoResponse(
    val group: GroupInfoDto,
    val success: Boolean
)

@Serializable
data class GroupInfoDto(
    val _id: String,
    val name: String? = null,
    val fname: String? = null,
    val usersCount: Int = 0,
    val description: String? = null,
    val topic: String? = null,
    val t: String? = null,
    val msgs: Int = 0,
    val ts: String? = null,
    val teamId: String? = null,
    val avatarETag: String? = null,
    val teamMain: Boolean = false
) {
    fun toDomain(): GroupInfo {
        val avatarUrl = avatarETag?.let {
            "${Constants.BASE_URL}avatar/room/${_id}?etag=$it"
        } ?: "${Constants.BASE_URL}avatar/room/${_id}"

        return GroupInfo(
            id = _id,
            name = name ?: Constants.DEFAULT_GROUP_NAME,
            displayName = fname ?: name ?: Constants.DEFAULT_GROUP_NAME,
            membersCount = usersCount,
            description = description ?: topic,
            avatarUrl = avatarUrl
        )
    }
}

@Serializable
data class GroupMembersResponse(
    val members: List<GroupMemberDto>,
    val success: Boolean,
    val total: Int = 0
)
@Serializable
data class ChannelInfoResponse(
    val channel: ChannelInfoDto,
    val success: Boolean
)

@Serializable
data class ChannelInfoDto(
    val _id: String,
    val name: String? = null,
    val fname: String? = null,
    val usersCount: Int = 0,
    val description: String? = null,
    val topic: String? = null,
    val t: String? = null,
    val msgs: Int = 0,
    val ts: String? = null,
    val avatarETag: String? = null
) {
    fun toDomain(): GroupInfo {
        val avatarUrl = avatarETag?.let {
            "${Constants.BASE_URL}avatar/room/${_id}?etag=$it"
        } ?: "${Constants.BASE_URL}avatar/room/${_id}"

        return GroupInfo(
            id = _id,
            name = name ?: Constants.DEFAULT_GROUP_NAME,
            displayName = fname ?: name ?: Constants.DEFAULT_GROUP_NAME,
            membersCount = usersCount,
            description = description ?: topic,
            avatarUrl = avatarUrl
        )
    }
}

@Serializable
data class ChannelMembersResponse(
    val members: List<GroupMemberDto>,
    val success: Boolean,
    val total: Int = 0
)