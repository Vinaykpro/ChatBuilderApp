package com.vinaykpro.chatbuilder.ui.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.vinaykpro.chatbuilder.data.utils.ChatsList

class HomeViewModel(
    private val chatsList: ChatsList
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        _state.value = HomeState(chatsList = chatsList.getChats())
    }
}