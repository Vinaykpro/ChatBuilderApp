package com.vinaykpro.chatbuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages where chatid = :chatID")
    fun getAllMessages(chatID: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY messageId ASC LIMIT :limit")
    suspend fun getMessagesPaged(chatId: Int, limit: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND messageId > :afterMessageId ORDER BY messageId ASC LIMIT :limit")
    suspend fun getNextMessages(chatId: Int, afterMessageId: Int, limit: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND messageId < :beforeMessageId ORDER BY messageId DESC LIMIT :limit")
    suspend fun getPreviousMessages(
        chatId: Int,
        beforeMessageId: Int,
        limit: Int
    ): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addMessage(chat: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT messageId FROM messages ORDER BY messageId DESC LIMIT 1")
    suspend fun getLastMessageId(): Int?
}