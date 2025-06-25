package com.vinaykpro.chatbuilder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes WHERE id = :id")
    fun getThemeByIdFlow(id: Int): Flow<ThemeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: ThemeEntity): Long

    @Query("SELECT * FROM themes")
    suspend fun getAllThemes(): List<ThemeEntity>
}