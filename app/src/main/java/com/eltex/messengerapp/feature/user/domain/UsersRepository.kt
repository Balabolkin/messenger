package com.eltex.messengerapp.feature.user.domain

import com.eltex.messengerapp.util.Result

interface UsersRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun searchUsers(query: String): Result<List<User>>
}