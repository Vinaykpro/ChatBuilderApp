package com.vinaykpro.chatbuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinaykpro.chatbuilder.data.local.ChatEntity
//import com.vinaykpro.chatbuilder.data.local.StatEntity
import kotlinx.coroutines.flow.Flow

//@Dao
//interface StatDao {
//    @Query("SELECT * FROM stats WHERE chatid = :id")
//    fun getStatsById(id: Int): Flow<StatEntity?>
//
//    @Query("SELECT * FROM stats WHERE chatid = :id")
//    fun getStatsEntityById(id: Int): StatEntity?
//
//    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
//    suspend fun addOrUpdateStats(chat: ChatEntity): Long
//}
