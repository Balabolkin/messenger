package com.eltex.messengerapp.feature.profile.data

import com.eltex.messengerapp.datastore.AuthDataStore
import com.eltex.messengerapp.feature.profile.domain.ProfileRepository
import com.eltex.messengerapp.feature.user.data.UserApi
import com.eltex.messengerapp.feature.user.domain.User
import com.eltex.messengerapp.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val userApi: UserApi
) : ProfileRepository {
    override fun getUser(): Flow<Result<User>> = flow {
        val token = authDataStore.getToken().first()
        val userId = authDataStore.getUserId().first()

        if (token == null || userId == null) {
            emit(Result.Error("Пользователь не авторизован"))
            return@flow
        }

        val result = try {
            val response = userApi.getUserInfo(userId)
            if (response.success) {
                Result.Success(response.user.toDomain())
            } else {
                Result.Error("Ошибка сервера: данные не получены")
            }
        } catch (e: Exception) {
            Result.Error("Ошибка сети: ${e.message}")

        }

        emit(result)
    }

    override suspend fun logout() {
        authDataStore.logout()
    }
}