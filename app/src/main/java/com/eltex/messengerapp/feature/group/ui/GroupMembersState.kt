package com.eltex.messengerapp.feature.group.ui

import com.eltex.messengerapp.feature.group.domain.GroupInfo
import com.eltex.messengerapp.feature.group.domain.GroupMember

data class GroupMembersState(
    val isLoading: Boolean = false,
    val groupInfo: GroupInfo? = null,
    val members: List<GroupMember> = emptyList(),
    val error: String? = null
)