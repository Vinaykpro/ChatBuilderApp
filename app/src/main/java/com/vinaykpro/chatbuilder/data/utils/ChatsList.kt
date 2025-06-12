package com.vinaykpro.chatbuilder.data.utils

import com.vinaykpro.chatbuilder.data.models.ChatItemModel

class ChatsList() {
    private var chatsList: List<ChatItemModel> = List(10) { i ->
        ChatItemModel(
            icon = "default",
            name = "User $i",
            lastMessage = "Last message from User $i",
            lastSeen = "0${i}:${i}0 AM"
        )
    }

    fun getChats(): List<ChatItemModel> {
        return chatsList
    }
}