package com.vinaykpro.chatbuilder.ui.screens.home

import com.vinaykpro.chatbuilder.data.models.ChatItemModel
import com.vinaykpro.chatbuilder.data.utils.ChatsList

data class HomeState(
    val chatsList: List<ChatItemModel> = emptyList()
)