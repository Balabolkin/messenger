package com.eltex.messengerapp.di

import com.eltex.messengerapp.feature.chats.data.ChatsRepositoryImpl
import com.eltex.messengerapp.feature.chats.domain.ChatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface ChatsModule {
    @Binds
    @Singleton
    fun bindChatsRepository(impl: ChatsRepositoryImpl): ChatsRepository
}
