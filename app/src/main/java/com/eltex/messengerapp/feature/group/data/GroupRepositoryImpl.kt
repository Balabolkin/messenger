package com.eltex.messengerapp.feature.group.data

import com.eltex.messengerapp.data.Constants
import com.eltex.messengerapp.feature.group.data.GroupApi.getChannelInfo
import com.eltex.messengerapp.feature.group.data.GroupApi.getChannelMembers
import com.eltex.messengerapp.feature.group.data.GroupApi.getGroupInfo
import com.eltex.messengerapp.feature.group.data.GroupApi.getGroupMembers
import com.eltex.messengerapp.feature.group.domain.GroupInfo
import com.eltex.messengerapp.feature.group.domain.GroupMember
import com.eltex.messengerapp.feature.group.domain.GroupRepository
import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : GroupRepository {

    override suspend fun getGroupInfo(roomId: String, roomType: String): GroupInfo {
        return when (roomType) {
            Constants.CHAT_TYPE_GROUP -> {
                val response = client.getGroupInfo(roomId)
                response.group.toDomain()
            }
            Constants.CHAT_TYPE_CHANNEL -> {
                val response = client.getChannelInfo(roomId)
                response.channel.toDomain()
            }
            else -> throw IllegalArgumentException("Unsupported room type: $roomType")
        }
    }

    override suspend fun getGroupMembers(roomId: String, roomType: String): List<GroupMember> {
        return when (roomType) {
            Constants.CHAT_TYPE_GROUP -> {
                val response = client.getGroupMembers(roomId)
                response.members.map { it.toDomain() }
            }
            Constants.CHAT_TYPE_CHANNEL -> {
                val response = client.getChannelMembers(roomId)
                response.members.map { it.toDomain() }
            }
            else -> throw IllegalArgumentException("Unsupported room type: $roomType")
        }
    }
}