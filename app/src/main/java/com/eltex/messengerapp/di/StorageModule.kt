package com.eltex.messengerapp.di

import com.eltex.messengerapp.data.AuthStorage
import com.eltex.messengerapp.data.AuthStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface StorageModule {
    @Binds
    @Singleton
    fun bindAuthStorage(impl: AuthStorageImpl): AuthStorage
}