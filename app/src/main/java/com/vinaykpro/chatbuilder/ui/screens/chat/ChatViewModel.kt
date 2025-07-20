package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.FileEntity
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val chatId: Int) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).messageDao()
    private val chatDao = AppDatabase.getInstance(application).chatDao()
    private val fileDao = AppDatabase.getInstance(application).fileDao()

    val chatDetails: StateFlow<ChatEntity?> = chatDao.getChatById(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val files: StateFlow<Map<Int, FileEntity>> = fileDao
        .getFilesByChatId(chatId)
        .map { fileList: List<FileEntity> ->
            fileList.associateBy { it.fileid }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    var mediaMessages: List<MessageEntity?> = emptyList()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages


    private var nextId = 0
    private var prevId = 0
    private var pageSize = 80
    private var isLoadingNext = true
    private var isLoadingPrev = true
    internal var isInitialLoad = true
    internal var isInitialScroll = true
    private var hasMoreNext = true
    private var hasMorePrev = true

    fun initialLoad(lastOpenedMsgId: Int?) {
        if (!isInitialLoad) return
        isInitialLoad = false
        viewModelScope.launch {
            Log.i("vkpro", "lastid = $lastOpenedMsgId")
            if (lastOpenedMsgId == null) {
                val newMessages = dao.getMessagesPaged(chatId, pageSize)
                if (newMessages.isNotEmpty()) {
                    prevId = newMessages[0].messageId
                    nextId = newMessages[newMessages.size - 1].messageId
                    _messages.update { it + newMessages }
                }
            } else {
                val newMessagesNext = dao.getNextMessages(chatId, lastOpenedMsgId - 1, pageSize / 2)
                val newMessagesPrev = dao.getPreviousMessages(chatId, lastOpenedMsgId, pageSize / 2)
                var newMessages = emptyList<MessageEntity>()
                if (newMessagesNext.isNotEmpty() || newMessagesPrev.isNotEmpty()) {
                    newMessages = newMessagesPrev.reversed() + newMessagesNext
                    prevId = newMessages[0].messageId
                    nextId = newMessages[newMessages.size - 1].messageId
                    _messages.update { it + newMessages }
                }
            }
            mediaMessages = dao.getAllMediaMessages(chatId)
            isLoadingNext = false
            isLoadingPrev = false
        }
    }

    fun loadNextPage() {
        if (isLoadingNext || isLoadingPrev || !hasMoreNext) return
        isLoadingNext = true

        viewModelScope.launch {
            val newMessages = dao.getNextMessages(chatId, nextId, pageSize)

            if (newMessages.isNotEmpty()) {
                nextId = newMessages[newMessages.size - 1].messageId
                _messages.update { it + newMessages }
            }

            if (newMessages.size < pageSize) {
                hasMoreNext = false
            }
            isLoadingNext = false
        }
    }

    fun loadPrevPage() {
        if (isLoadingPrev || isLoadingNext || !hasMorePrev) return
        isLoadingPrev = true

        viewModelScope.launch {
            val newMessages = dao.getPreviousMessages(chatId, prevId, pageSize)

            if (newMessages.isNotEmpty()) {
                val reversed = newMessages.reversed()
                prevId = reversed.first().messageId
                _messages.update { reversed + it }
            }

            if (newMessages.size < pageSize) {
                hasMorePrev = false
            }

            isLoadingPrev = false
        }
    }

    fun saveScrollPosition(msgId: Int) {
        val current = chatDetails.value ?: return
        viewModelScope.launch {
            chatDao.addOrUpdateChat(current.copy(lastOpenedMsgId = msgId))
        }
    }
}