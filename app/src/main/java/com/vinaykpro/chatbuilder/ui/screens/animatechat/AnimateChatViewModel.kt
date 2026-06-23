package com.vinaykpro.chatbuilder.ui.screens.animatechat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnimateChatViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = AppDatabase.getInstance(application).chatDao()

    val chatsList: StateFlow<List<ChatEntity>> = chatDao.getAllChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedChat by mutableStateOf<ChatEntity?>(null)

    var rangeStart by mutableIntStateOf(-1)
    var rangeEnd by mutableIntStateOf(-1)

    var isTypingEnabled by mutableStateOf<Boolean>(true)
    var isInputTypingEnabled by mutableStateOf<Boolean>(true)

    private var isInitial = true
    var currMsg by mutableIntStateOf(0)
    var typing by mutableStateOf<Boolean>(false)

    var currMsgInputBar by mutableStateOf("")

    val msg1 = "Hi bro! this is how it animates, click start to see your chat play in live"

    fun initialLoad() {
        if (!isInitial) return;
        isInitial = false;
        viewModelScope.launch {
            launch {
                while (true) {
                    currMsg = 0
                    delay(300)
                    if (isInputTypingEnabled) {
                        for (i in 0..(msg1.length - 1)) {
                            if (!isInputTypingEnabled) break
                            delay(if (msg1[i] == ' ') 180 else 50)
                            currMsgInputBar += msg1[i]
                        }
                        delay(300)
                    } else delay(700)
                    currMsg++
                    currMsgInputBar = ""
                    delay(300)
                    if (isTypingEnabled) typing = true
                    delay(1800)
                    currMsg++
                    typing = false
                    delay(1000)
                }
            }
        }
    }
}