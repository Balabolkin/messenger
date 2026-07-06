package com.eltex.messengerapp.feature.group.domain

data class GroupMember(
    val id: String,
    val username: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val role: MemberRole = MemberRole.MEMBER
)

enum class MemberRole {
    ADMIN,
    MEMBER
}