package com.vinaykpro.chatbuilder.ui.screens.livechat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveChatViewModel(application: Application, private val chatId: Int) :
    AndroidViewModel(application) {
    private val chatDao = AppDatabase.getInstance(application).chatDao()
    private val msgDao = AppDatabase.getInstance(application).messageDao()

    var chatDetails: ChatEntity? = ChatEntity()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    internal var isInitialLoad = true

    var currMsg by mutableIntStateOf(0)
    var currMsgInputBar by mutableStateOf("")


    fun initialLoad(
        rangeStart: Int,
        rangeEnd: Int,
        isTypingEnabled: Boolean,
        isInputTypingEnabled: Boolean
    ) {
        if (!isInitialLoad) return
        isInitialLoad = false
        viewModelScope.launch {
            _isLoading.value = true

            val fetchedChat = withContext(Dispatchers.IO) { chatDao.getChatEntityById(chatId) }
            val fetchedMessages =
                withContext(Dispatchers.IO) { msgDao.getMessagesInRange(rangeStart, rangeEnd) }

            chatDetails = fetchedChat
            _messages.update { fetchedMessages }
            _isLoading.value = false

            launch {
                delay(1000)
                currMsg = rangeStart - 1
                while (currMsg < rangeEnd) {
                    delay(1000)
                    val msg = messages.value[currMsg + 1 - rangeStart]
                    if (msg.userid != chatDetails?.senderId) {
                        if (isTypingEnabled) {
                            _isTyping.value = true
                            delay(
                                ((msg.message?.length
                                    ?: 0) * 80).toLong()
                            )
                        } else {
                            delay(
                                ((msg.message?.length
                                    ?: 0) * 40).toLong()
                            )
                        }
                    } else {
                        if (msg.message != null && isInputTypingEnabled) {
                            for (i in 0..(msg.message.length - 1)) {
                                delay(if (msg.message[i] == ' ') 220 else 80)
                                currMsgInputBar += msg.message[i]
                            }
                            delay(300)
                        } else delay(1000)
                    }
                    currMsg++
                    currMsgInputBar = ""
                    _isTyping.value = false
                }
            }
        }
    }
}