package com.eltex.messengerapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.eltex.messengerapp.data.database.dao.ChatDao
import com.eltex.messengerapp.data.database.dao.MessageDao
import com.eltex.messengerapp.data.database.dao.UserDao
import com.eltex.messengerapp.data.database.entities.ChatEntity
import com.eltex.messengerapp.data.database.entities.MessageEntity
import com.eltex.messengerapp.data.database.entities.UserEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "messenger_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}