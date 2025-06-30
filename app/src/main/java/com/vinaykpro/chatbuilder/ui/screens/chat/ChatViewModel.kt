package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vinaykpro.chatbuilder.data.local.AppDatabase

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).messageDao()

//    val messages = dao.getAllMessages().stateIn(
//        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
//    )

//    fun insertMessage(msg: MessageEntity) {
//        viewModelScope.launch {
//            dao.insert(msg)
//        }
//    }
//
//    fun insertMessages(list: List<MessageEntity>) {
//        viewModelScope.launch {
//            dao.insertAll(list)
//        }
//    }
}