package com.eltex.messengerapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eltex.messengerapp.data.database.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY COALESCE(lastMessageTs, ts) DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("SELECT * FROM chats WHERE rid = :rid LIMIT 1")
    suspend fun getChatByRid(rid: String): ChatEntity?

    @Query("UPDATE chats SET unread = 0 WHERE rid = :rid")
    suspend fun markChatAsRead(rid: String)

    @Query("DELETE FROM chats WHERE _id = :id")
    suspend fun deleteChatById(id: String)

    @Query("DELETE FROM chats")
    suspend fun clearChats()
}