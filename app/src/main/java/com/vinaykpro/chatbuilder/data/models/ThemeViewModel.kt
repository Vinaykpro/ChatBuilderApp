package com.vinaykpro.chatbuilder.data.models

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.themeDao()

    private val _selectedThemeId = MutableStateFlow(1)
    val selectedThemeId: StateFlow<Int> = _selectedThemeId

    @OptIn(ExperimentalCoroutinesApi::class)
    val themeEntity: StateFlow<ThemeEntity?> = _selectedThemeId
        .flatMapLatest { id -> dao.getThemeByIdFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val themes: StateFlow<List<ThemeEntity>> = flow {
        emit(dao.getAllThemes())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun changeTheme(id: Int) {
        _selectedThemeId.value = id
    }

    fun updateTheme(theme: ThemeEntity) {
        viewModelScope.launch {
            dao.insertTheme(theme)
        }
    }

    suspend fun saveCustomIcon(
        uri: Uri,
        context: Context,
        themeId: Int,
        filename: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val themeDir = File(context.filesDir, "theme$themeId")
                if (!themeDir.exists()) themeDir.mkdirs()
                val inputStream = context.contentResolver.openInputStream(uri)
                val iconFile = File(themeDir, filename)

                inputStream?.use { input ->
                    FileOutputStream(iconFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("SaveIcon", "Failed to save icon", e)
            }
            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }
}
