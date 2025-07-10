package com.vinaykpro.chatbuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinaykpro.chatbuilder.data.local.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files WHERE chatid = :chatid")
    fun getFilesByChatId(chatid: Int): Flow<FileEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addFiles(files: List<FileEntity>): List<Long>

    @Query("SELECT * FROM files")
    fun getAllFiles(): Flow<List<FileEntity>>
}