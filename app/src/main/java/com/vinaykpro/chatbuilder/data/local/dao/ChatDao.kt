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

    @Query("UPDATE chats SET lastopened = :timestamp WHERE chatid = :chatId")
    suspend fun updateLastOpened(chatId: Int, timestamp: Long)

    @Query("SELECT * FROM chats WHERE hidden = 0 ORDER BY isPinned DESC, lastopened DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE hidden = 1 ORDER BY isPinned DESC, lastopened DESC")
    fun getAllHiddenChats(): Flow<List<ChatEntity>>

    @Query("UPDATE chats SET hidden = :state WHERE chatid = :chatId")
    fun updateHiddenState(chatId: Int, state: Int)

    @Query("UPDATE chats SET hidden = :state WHERE chatid IN (:chatIds)")
    suspend fun updateHiddenStateBulk(chatIds: Collection<Int>, state: Int)

    @Query("UPDATE chats SET isPinned = :pinned WHERE chatid IN (:chatIds)")
    suspend fun updatePinnedStateBulk(chatIds: Collection<Int>, pinned: Boolean)

    @Query("UPDATE chats SET isFavorite = :favorite WHERE chatid IN (:chatIds)")
    suspend fun updateFavoriteStateBulk(chatIds: Collection<Int>, favorite: Boolean)

    @Query("UPDATE chats SET showReceiverName = :state WHERE chatid = :chatId")
    fun updateReceiverVisibleState(chatId: Int, state: Boolean)

    @Query("SELECT * FROM chats")
    fun getAllChatEntities(): List<ChatEntity>

    @Query("DELETE FROM chats WHERE chatid = :id")
    suspend fun deleteChatById(id: Int)

    @Query("DELETE FROM chats WHERE chatid IN (:chatIds)")
    suspend fun deleteChatsByIds(chatIds: Collection<Int>)
}