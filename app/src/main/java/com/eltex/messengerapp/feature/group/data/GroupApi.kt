package com.eltex.messengerapp.feature.group.data

import com.eltex.messengerapp.data.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

object GroupApi {
    suspend fun HttpClient.getGroupMembers(roomId: String): GroupMembersResponse {
        return get(Constants.GROUP_MEMBERS_PATH) {
            parameter(Constants.PARAM_ROOM_ID, roomId)
        }.body()
    }

    suspend fun HttpClient.getGroupInfo(roomId: String): GroupInfoResponse {
        return get(Constants.GROUP_INFO_PATH) {
            parameter(Constants.PARAM_ROOM_ID, roomId)
        }.body()
    }

    suspend fun HttpClient.getChannelMembers(roomId: String): ChannelMembersResponse {
        return get(Constants.CHANNEL_MEMBERS_PATH) {
            parameter(Constants.PARAM_ROOM_ID, roomId)
        }.body()
    }

    suspend fun HttpClient.getChannelInfo(roomId: String): ChannelInfoResponse {
        return get(Constants.CHANNEL_INFO_PATH) {
            parameter(Constants.PARAM_ROOM_ID, roomId)
        }.body()
    }
}