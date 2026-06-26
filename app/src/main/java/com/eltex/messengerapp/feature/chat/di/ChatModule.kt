package com.eltex.messengerapp.feature.chat.di

import com.eltex.messengerapp.feature.chat.data.ChatRepositoryImpl
import com.eltex.messengerapp.feature.chat.domain.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface ChatModule {
    @Binds
    @Singleton
    fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
