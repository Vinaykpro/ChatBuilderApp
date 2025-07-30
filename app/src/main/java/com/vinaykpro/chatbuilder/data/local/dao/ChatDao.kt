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
    fun getChatById(id: Int): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE chatid = :id")
    fun getChatEntityById(id: Int): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addOrUpdateChat(chat: ChatEntity): Long

    @Query("UPDATE chats SET senderId = :senderId WHERE chatid = :chatId")
    suspend fun updateSender(chatId: Int, senderId: Int)

    @Query("SELECT * FROM chats ORDER BY lastopened")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats")
    fun getAllChatEntities(): List<ChatEntity>

    @Query("DELETE FROM chats WHERE chatid = :id")
    suspend fun deleteChatById(id: Int)

}