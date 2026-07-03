package com.eltex.messengerapp.di

import com.eltex.messengerapp.feature.user.data.UsersRepositoryImpl
import com.eltex.messengerapp.feature.user.domain.UsersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UsersRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUsersRepository(
        impl: UsersRepositoryImpl
    ): UsersRepository
}