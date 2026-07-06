package com.eltex.messengerapp.di

import com.eltex.messengerapp.feature.group.data.GroupRepositoryImpl
import com.eltex.messengerapp.feature.group.domain.GroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface GroupModule {
    @Binds
    @Singleton
    fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository
}