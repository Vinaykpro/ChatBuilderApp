package com.vinaykpro.chatbuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE messageId = :id")
    fun getMessages(id: Int): Flow<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addMessage(chat: MessageEntity)
}