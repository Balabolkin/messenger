package com.eltex.messengerapp.feature.profile.domain

import com.eltex.messengerapp.feature.user.domain.User
import com.eltex.messengerapp.util.Result
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUser(): Flow<Result<User>>
    suspend fun logout()
}