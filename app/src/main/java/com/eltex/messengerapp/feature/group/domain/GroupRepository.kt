package com.eltex.messengerapp.feature.group.domain

interface GroupRepository {
    suspend fun getGroupInfo(roomId: String, roomType: String): GroupInfo
    suspend fun getGroupMembers(roomId: String, roomType: String): List<GroupMember>
}

data class GroupInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val membersCount: Int,
    val description: String? = null,
    val avatarUrl: String? = null
)