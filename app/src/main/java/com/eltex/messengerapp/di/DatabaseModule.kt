package com.eltex.messengerapp.di

import android.content.Context
import androidx.room.Room
import com.eltex.messengerapp.data.database.AppDatabase
import com.eltex.messengerapp.data.database.dao.ChatDao
import com.eltex.messengerapp.data.database.dao.MessageDao
import com.eltex.messengerapp.data.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}