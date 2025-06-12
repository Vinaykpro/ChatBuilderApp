package com.vinaykpro.chatbuilder.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vinaykpro.chatbuilder.data.utils.ChatsList

class HomeViewModelFactory(
    private val chatsList: ChatsList
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(chatsList) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
