package com.eltex.messengerapp.feature.chats.creation.dm.domain

import com.eltex.messengerapp.feature.chats.data.SubscriptionDto
import com.eltex.messengerapp.util.Result

interface DmCreationRepository {
    suspend fun createDm(username: String): Result<SubscriptionDto>
}