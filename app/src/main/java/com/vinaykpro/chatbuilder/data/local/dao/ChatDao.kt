package com.vinaykpro.chatbuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE chatid = :id")
    fun getChatById(id: Int): Flow<ChatEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addChat(chat: ChatEntity): Long

    @Query("SELECT * FROM chats")
    suspend fun getAllChats(): List<ChatEntity>
}