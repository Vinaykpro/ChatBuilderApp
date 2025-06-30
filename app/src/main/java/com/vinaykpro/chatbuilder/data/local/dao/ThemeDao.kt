package com.vinaykpro.chatbuilder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes WHERE id = :id")
    fun getThemeByIdFlow(id: Int): Flow<ThemeEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTheme(theme: ThemeEntity): Long

    @Query("SELECT * FROM themes")
    suspend fun getAllThemes(): List<ThemeEntity>
}