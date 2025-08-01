package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.DateInfo
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import com.vinaykpro.chatbuilder.data.local.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

class ChatViewModel(application: Application, private val chatId: Int) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).messageDao()
    private val chatDao = AppDatabase.getInstance(application).chatDao()
    private val fileDao = AppDatabase.getInstance(application).fileDao()

    val chatDetails: StateFlow<ChatEntity?> = chatDao.getChatById(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messageBarSenderIndex = MutableStateFlow(0)
    val messageBarSenderIndex: StateFlow<Int> = _messageBarSenderIndex

    //private val _searchedResults = MutableStateFlow<List<Int>>(emptyList())
    //val searchedResults: StateFlow<List<Int>> = _searchedResults.asStateFlow()
    var searchedResults: List<Int> = emptyList()
    var searchedItemsSet: Set<Int> = emptySet()
    var currentSearchIndex = 0
    var searchTerm: String? = null

    var scrollIndex: Int? = null

    var userList: List<UserInfo> = emptyList()
    var userColorMap: MutableMap<Int, Color> = mutableMapOf()

    var datesList: List<DateInfo> = emptyList()

    private var nextId = 0
    private var prevId = 0
    private var pageSize = 80
    internal var isLoadingNext = false
    internal var isLoadingPrev = false
    internal var isInitialLoad = true
    internal var needScroll = false
    private var hasMoreNext = true
    private var hasMorePrev = true

    var showToast = false
    var toast: String? = null

    fun initialLoad(lastOpenedMsgId: Int?) {
        if (!isInitialLoad) return
        viewModelScope.launch {
            Log.i("vkpro", "lastid = $lastOpenedMsgId")
            withContext(Dispatchers.IO) {
                if (lastOpenedMsgId == null || lastOpenedMsgId < 0) {
                    _isLoading.value = true
                    val newMessages = dao.getMessagesPaged(chatId, pageSize)
                    if (newMessages.isNotEmpty()) {
                        prevId = newMessages[0].messageId
                        nextId = newMessages[newMessages.size - 1].messageId
                        _messages.update { it + newMessages }
                    }
                    _isLoading.value = false
                } else {
                    loadMessagesAtId(lastOpenedMsgId)
                }
                isInitialLoad = false
                isLoadingNext = false
                isLoadingPrev = false
            }
        }
    }

    fun search(text: String, nearById: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.i("vkpro", "searching for $text")
                searchedResults = dao.getSearchResultsInChat(chatId, "%$text%")
                searchedItemsSet = searchedResults.toSet()
                currentSearchIndex = 0
                var maxDiff: Int = Int.MAX_VALUE
                searchedResults.forEachIndexed { ind, id ->
                    val diff = abs(nearById - id)
                    if (diff < maxDiff) {
                        currentSearchIndex = ind
                        //Log.i("vkpro", "index = $currentSearchIndex ; id = $id ; size = ${searchedResults.size}")
                        maxDiff = diff
                    }
                }
                //Log.i("vkpro", "res size: ${searchedResults.size}")
                if (searchedResults.isNotEmpty()) {
                    searchTerm = text
                    navigateSearchedItems(0)
                } else {
                    toast = "No results found for '$text'"
                    showToast = true
                }
            }
        }
    }

    fun resetSearch() {
        searchedResults = emptyList()
        searchedItemsSet = emptySet()
        currentSearchIndex = 0
        searchTerm = null
    }

    fun navigateSearchedItems(direction: Int) {
        currentSearchIndex += direction
        Log.i("vkpro", "going next")
        viewModelScope.launch {
            if (currentSearchIndex < searchedResults.size)
                loadMessagesAtId(searchedResults[currentSearchIndex])
            Log.i("vkpro", "loading at id: ${searchedResults[currentSearchIndex]}")
        }
    }

    fun navigateToDate(dateId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadMessagesAtId(dateId)
            }
        }
    }

    suspend fun loadMessagesAtId(id: Int) = withContext(Dispatchers.IO) {
        if (_isLoading.value && !isInitialLoad) return@withContext
        _isLoading.value = true
        needScroll = true
        val newMessagesNext =
            dao.getNextMessages(chatId, id - 1, pageSize / 2)
        val newMessagesPrev =
            dao.getPreviousMessages(chatId, id, pageSize / 2)
        scrollIndex = newMessagesPrev.size
        var newMessages = emptyList<MessageEntity>()
        if (newMessagesNext.isNotEmpty() || newMessagesPrev.isNotEmpty()) {
            newMessages = newMessagesPrev.reversed() + newMessagesNext
            prevId = newMessages[0].messageId
            nextId = newMessages[newMessages.size - 1].messageId
            _messages.update { newMessages }
        }
        isLoadingNext = false
        isLoadingPrev = false
        hasMorePrev = true
        hasMoreNext = true
        _isLoading.value = false
    }

    suspend fun loadNextPage() = withContext(Dispatchers.IO) {
        if (isLoadingNext || isLoadingPrev || !hasMoreNext) return@withContext
        isLoadingNext = true
        Log.i("vkpro:", "Loading next msgs")

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

    suspend fun loadPrevPage() = withContext(Dispatchers.IO) {
        //Log.i("vkpro", "Trying LoadPrev ${!isLoadingPrev} && ${!isLoadingNext} && $hasMorePrev")
        if (isLoadingPrev || isLoadingNext || !hasMorePrev) return@withContext
        isLoadingPrev = true
        Log.i("vkpro", "Started oading prev page")

        viewModelScope.launch {
            val newMessages = dao.getPreviousMessages(chatId, prevId, pageSize)

            if (newMessages.isNotEmpty()) {
                val reversed = newMessages.reversed()
                prevId = reversed.first().messageId
                _messages.update { reversed + it }
            }

            if (newMessages.size < pageSize && !isInitialLoad) {
                hasMorePrev = false
            }

            isLoadingPrev = false
        }
    }

    fun addNewMessage(chatId: Int, message: String, user: UserInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val (date, time) = java.util.Date().let {
                    java.text.SimpleDateFormat("d/M/yy", java.util.Locale.getDefault())
                        .format(it) to
                            java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                .format(it)
                }
                val message = MessageEntity(
                    messageType = MESSAGETYPE.MESSAGE,
                    chatid = chatId,
                    userid = user.userid,
                    username = user.username,
                    message = message,
                    date = date,
                    time = time,
                    timestamp = null,
                    fileId = null,
                    messageStatus = null,
                    replyMessageId = null
                )
                if (message.message != null && message.date != null)
                    chatDao.updateLastMesssage(chatId, message.message, message.date)
                val msgId = dao.addMessage(message).toInt()
                if (hasMoreNext) {
                    Log.i("vkpro", "Loading last page")
                    loadMessagesAtId(msgId)
                } else {
                    Log.i("vkpro", "Just scrolling after last page")
                    hasMoreNext = true
                    loadNextPage()
                    needScroll = true
                    scrollIndex = -1 // for scrolling to end
                }
            }
        }
    }

    fun hideUnhideChat(chatId: Int, hiddenState: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val updatedState = if (hiddenState == 0) 1 else 0
                chatDao.updateHiddenState(chatId, updatedState)
            }
            onDone()
        }
    }

    fun loadUserList(chatid: Int, darkColors: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val users = dao.getUsersList(chatid)
                userList = listOf(UserInfo(-1, "None")) + users
                _messageBarSenderIndex.value = if (userList.size > 1) 1 else 0
                val (start, end) = if (darkColors) 128 to 256 else 0 to 128
                for (user in userList) {
                    val randomColor = Color(
                        red = Random.nextInt(start, end),
                        green = Random.nextInt(start, end),
                        blue = Random.nextInt(start, end)
                    )
                    userColorMap[user.userid] = randomColor
                }
            }
        }
    }

    fun loadDatesList(chatid: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                datesList = dao.getDatesList(chatid)
            }
        }
    }

    fun updateMessageBarUserIndex(dir: Int) {
        val updated = _messageBarSenderIndex.value + dir
        if (updated > 0 && updated < userList.size) {
            _messageBarSenderIndex.value = updated
        }
    }

    fun addUser(name: String) {
        var newId = 0
        for (u in userList) {
            if (u.userid >= newId) newId = u.userid
        }
        userList = userList + listOf(UserInfo(newId + 1, name))
        _messageBarSenderIndex.value = userList.size - 1
    }

    fun updateSenderId(chatid: Int, senderId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatDao.updateSender(chatid, senderId)
            }
        }
    }

    fun saveScrollPosition(msgId: Int) {
        val current = chatDetails.value ?: return
        viewModelScope.launch {
            chatDao.addOrUpdateChat(current.copy(lastOpenedMsgId = msgId))
        }
    }
}