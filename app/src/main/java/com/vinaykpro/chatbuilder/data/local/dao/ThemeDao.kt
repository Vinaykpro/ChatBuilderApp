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

    @Query("SELECT EXISTS(SELECT 1 FROM themes WHERE id = :id)")
    fun isThemeAvailable(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTheme(theme: ThemeEntity): Long

    @Query("SELECT * FROM themes")
    fun getAllThemes(): Flow<List<ThemeEntity>>

    @Query("DELETE FROM themes where id = :themeId")
    fun deleteTheme(themeId: Int)
}