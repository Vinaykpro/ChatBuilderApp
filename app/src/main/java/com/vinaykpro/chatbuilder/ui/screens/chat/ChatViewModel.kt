package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application, private val chatId: Int) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).messageDao()
    private val chatDao = AppDatabase.getInstance(application).chatDao()
    private val fileDao = AppDatabase.getInstance(application).fileDao()

    val chatDetails: StateFlow<ChatEntity?> = chatDao.getChatById(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages

    //private val _searchedResults = MutableStateFlow<List<Int>>(emptyList())
    //val searchedResults: StateFlow<List<Int>> = _searchedResults.asStateFlow()
    var searchedResults: List<Int> = emptyList()
    var searchedItemsSet: Set<Int> = emptySet()
    var currentSearchIndex = 0
    var searchTerm: String? = null

    private var nextId = 0
    private var prevId = 0
    private var pageSize = 80
    internal var isLoadingNext = false
    internal var isLoadingPrev = false
    internal var isLoading = true
    internal var isInitialLoad = true
    internal var needScroll = true
    private var hasMoreNext = true
    private var hasMorePrev = true

    var showToast = false
    var toast: String? = null

    fun initialLoad(lastOpenedMsgId: Int?) {
        if (!isInitialLoad) return
        viewModelScope.launch {
            Log.i("vkpro", "lastid = $lastOpenedMsgId")
            withContext(Dispatchers.IO) {
                if (lastOpenedMsgId == null) {
                    isLoading = true
                    val newMessages = dao.getMessagesPaged(chatId, pageSize)
                    if (newMessages.isNotEmpty()) {
                        prevId = newMessages[0].messageId
                        nextId = newMessages[newMessages.size - 1].messageId
                        _messages.update { it + newMessages }
                    }
                    isLoading = false
                } else {
                    loadMessagesAtId(lastOpenedMsgId)
                }
                isInitialLoad = false
                isLoadingNext = false
                isLoadingPrev = false
            }
        }
    }

    fun search(text: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.i("vkpro", "searching for $text")
                searchedResults = dao.getSearchResultsInChat(chatId, "%$text%")
                searchedItemsSet = searchedResults.toSet()
                Log.i("vkpro", "res size: ${searchedResults.size}")
                if (searchedResults.isNotEmpty()) {
                    searchTerm = text
                    goToSearchedItem(0)
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

    fun goToSearchedItem(direction: Int) {
        currentSearchIndex += direction
        Log.i("vkpro", "going next")
        viewModelScope.launch {
            if (currentSearchIndex < searchedResults.size)
                loadMessagesAtId(searchedResults[currentSearchIndex])
            Log.i("vkpro", "loading at id: ${searchedResults[currentSearchIndex]}")
        }
    }

    suspend fun loadMessagesAtId(id: Int) = withContext(Dispatchers.IO) {
        if (isLoading && !isInitialLoad) return@withContext
        isLoading = true
        val newMessagesNext =
            dao.getNextMessages(chatId, id - 1, pageSize / 2)
        val newMessagesPrev =
            dao.getPreviousMessages(chatId, id, pageSize / 2)
        var newMessages = emptyList<MessageEntity>()
        if (newMessagesNext.isNotEmpty() || newMessagesPrev.isNotEmpty()) {
            newMessages = newMessagesPrev.reversed() + newMessagesNext
            prevId = newMessages[0].messageId
            nextId = newMessages[newMessages.size - 1].messageId
            _messages.update { newMessages }
        }
        isLoadingNext = false
        isLoadingPrev = false
        isLoading = false
        needScroll = true
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
        if (isLoadingPrev || isLoadingNext || !hasMorePrev) return@withContext
        isLoadingPrev = true

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

    fun saveScrollPosition(msgId: Int) {
        val current = chatDetails.value ?: return
        viewModelScope.launch {
            chatDao.addOrUpdateChat(current.copy(lastOpenedMsgId = msgId))
        }
    }
}