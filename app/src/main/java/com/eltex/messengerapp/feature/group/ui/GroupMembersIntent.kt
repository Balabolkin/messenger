package com.eltex.messengerapp.feature.group.ui

sealed interface GroupMembersIntent {
    data object LoadGroupMembers : GroupMembersIntent
    data class MemberClicked(val memberId: String) : GroupMembersIntent
    data object BackClicked : GroupMembersIntent
}