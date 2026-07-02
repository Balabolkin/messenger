package com.eltex.messengerapp.feature.user.data

import android.util.Log
import com.eltex.messengerapp.feature.user.domain.User
import com.eltex.messengerapp.feature.user.domain.UsersRepository
import com.eltex.messengerapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi
) : UsersRepository {

    override suspend fun getUsers(): Result<List<User>> =
        try {
            val response = usersApi.getUsers()

            if (response.success) {
                Result.Success(response.users.map { it.toDomain() })
            } else {
                Result.Error("Ошибка загрузки пользователей")
            }
        } catch (e: Exception) {
            Result.Error("Ошибка сети: ${e.message}")
        }


    override suspend fun searchUsers(query: String): Result<List<User>> =
        try {
            val response = usersApi.searchUsers(query)
            if (response.success) {
                Result.Success(response.items.map { it.toDomain() })
            } else {
                Result.Error("Ошибка поиска пользователей")
            }
        } catch (e: Exception) {
            Result.Error("Ошибка сети: ${e.message}")
        }

}