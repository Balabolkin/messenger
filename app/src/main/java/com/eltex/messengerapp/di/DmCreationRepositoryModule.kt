package com.eltex.messengerapp.di

import com.eltex.messengerapp.feature.chats.creation.dm.data.DmCreationRepositoryImpl
import com.eltex.messengerapp.feature.chats.creation.dm.domain.DmCreationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DmCreationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDmCreationRepository(
        impl: DmCreationRepositoryImpl
    ): DmCreationRepository
}