package com.eltex.messengerapp.feature.chats.creation.dm.data

import com.eltex.messengerapp.feature.chats.creation.dm.domain.DmCreationRepository
import com.eltex.messengerapp.feature.chats.data.SubscriptionDto
import com.eltex.messengerapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DmCreationRepositoryImpl @Inject constructor(
    private val dmApi: DmApi
) : DmCreationRepository {
    override suspend fun createDm(username: String): Result<SubscriptionDto> {
        return try {
            val response = dmApi.createDm(username)

            val room = response.room
            if (room != null) {
                val subscription = SubscriptionDto(
                    _id = room._id,
                    rid = room._id,
                    name = username,
                    fname = username,
                    t = "d",
                    unread = 0,
                    alert = false,
                    ts = null,
                    ls = null,
                    lastMessage = null
                )
                Result.Success(subscription)
            } else {
                Result.Error("Ошибка создания чата: ${response.error ?: "unknown"}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error("Проблема при создании чата с указанным пользователем, попробуйте позднее")
        }
    }


}