package com.vinaykpro.chatbuilder.ui.screens.livechat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LiveChatViewModelFactory(
    private val application: Application,
    private val chatId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LiveChatViewModel(application, chatId) as T
    }
}