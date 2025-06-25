package com.vinaykpro.chatbuilder.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

}
