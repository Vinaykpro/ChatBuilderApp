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

    @Query("UPDATE chats SET lastmsg = :message, lastmsgtime = :date WHERE chatid = :chatId")
    suspend fun updateLastMesssage(chatId: Int, message: String, date: String)

    @Query("SELECT * FROM chats WHERE hidden = 0 ORDER BY lastopened")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE hidden = 1 ORDER BY lastopened")
    fun getAllHiddenChats(): Flow<List<ChatEntity>>

    @Query("UPDATE chats SET hidden = :state WHERE chatid = :chatId")
    fun updateHiddenState(chatId: Int, state: Int)

    @Query("SELECT * FROM chats")
    fun getAllChatEntities(): List<ChatEntity>

    @Query("DELETE FROM chats WHERE chatid = :id")
    suspend fun deleteChatById(id: Int)

}