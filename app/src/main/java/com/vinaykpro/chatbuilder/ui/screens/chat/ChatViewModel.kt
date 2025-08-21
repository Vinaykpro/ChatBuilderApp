package com.vinaykpro.chatbuilder.ui.screens.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.text.htmlEncode
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.DateInfo
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import com.vinaykpro.chatbuilder.data.local.UserInfo
import com.vinaykpro.chatbuilder.densityCompat
import com.vinaykpro.chatbuilder.drawBubble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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

    var fullMessageList: List<MessageEntity>? = null

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

    fun addNewMessage(message: String, user: UserInfo) {
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

    fun hideUnhideChat(hiddenState: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val updatedState = if (hiddenState == 0) 1 else 0
                chatDao.updateHiddenState(chatId, updatedState)
            }
            onDone()
        }
    }

    fun loadUserList(darkColors: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val users = dao.getUsersList(chatId)
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

    fun loadDatesList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                datesList = dao.getDatesList(chatId)
            }
        }
    }

    fun loadAndShowAd(context: Context, onAdFinished: () -> Unit, onFailed: (String) -> Unit) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-2813592783630195/1017615260",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    var rewardEarned = false
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            if (rewardEarned) onAdFinished()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            onFailed(adError.message)
                        }
                    }
                    ad.show(context as Activity) {
                        rewardEarned = true
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    onFailed(loadAdError.message)
                }
            }
        )
    }


    fun loadAndExport(
        context: Context,
        chatName: String,
        senderId: Int,
        onUpdate: (Int) -> Unit,
        onDone: (Uri?) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fullMessageList = dao.getAllMessages(chatId)
                if (fullMessageList != null) {
                    val doc = PdfDocument()
                    var ind = 0
                    var pageNo = 1
                    var progress = 0
                    var rightInfoWidth = 0f
                    var lastUserId: Int = fullMessageList!![ind].userid ?: 0
                    val chatInfo =
                        if (chatName.contains("Chat with")) chatName else "Chat with $chatName"
                    while (ind < fullMessageList!!.size) {
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNo).create()
                        val page = doc.startPage(pageInfo)
                        val canvas = page.canvas

                        var startY = 30f

                        val infoPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.GRAY
                            textSize = 14f * canvas.densityCompat()
                            isFakeBoldText = true
                        }
                        if (rightInfoWidth == 0f) {
                            rightInfoWidth = infoPaint.measureText("Exported by: ChatBuilder")
                        }
                        canvas.drawText(
                            chatInfo,
                            16f,
                            20f,
                            infoPaint
                        )
                        canvas.drawText(
                            "Exported by: ChatBuilder",
                            595 - rightInfoWidth - 16f,
                            20f,
                            infoPaint
                        )
                        Log.i(
                            "vkpro",
                            "Making page: $page , index: $ind / total: ${fullMessageList!!.size}"
                        )
                        while (true) {
                            if (startY >= 842 || ind >= fullMessageList!!.size) break
                            val msg = fullMessageList!![ind]
                            startY += if (lastUserId == msg.userid) 2f else 5f
                            val h = drawBubble(
                                canvas,
                                isOutgoing = senderId == msg.userid,
                                isFirst = lastUserId != msg.userid,
                                name = msg.username,
                                date = msg.date,
                                message = msg.message ?: "",
                                timeText = msg.date + ", " + msg.time,
                                maxBubbleInnerWidthPx = 400,
                                startY = startY
                            )
                            lastUserId = msg.userid ?: 0
                            if (h > 0) {
                                startY += h
                                ind++
                            } else {
                                if (ind >= fullMessageList!!.size) break
                                if (startY <= 820) {
                                    canvas.drawText(
                                        "ChatBuilder: https://play.google.com/store/apps/details?id=com.vinaykpro.chatbuilder",
                                        16f,
                                        830f,
                                        infoPaint
                                    )
                                }
                            }
                        }
                        doc.finishPage(page)
                        pageNo++
                        if (ind >= ((fullMessageList?.size ?: 0) * (progress / 100.toFloat()))) {
                            if (progress < 100) progress++
                            onUpdate(progress)
                        }
                    }

                    saveFileToDownloads(
                        context as Activity,
                        "$chatInfo.pdf",
                        "application/pdf",
                        writeData = { out ->
                            doc.writeTo(out)
                        },
                        onSaved = { uri ->
                            onDone(uri)
                        },
                        onDenied = { }
                    )
                    doc.close()
                }
            }
        }
    }

    @SuppressLint("UseKtx")
    fun loadAndExportToHTML(
        context: Context,
        chatName: String,
        senderId: Int,
        onUpdate: (Int) -> Unit,
        onDone: (Uri?) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fullMessageList = dao.getAllMessages(chatId)
                if (fullMessageList == null) return@withContext
                val htmlUntilName = "<html> \n" +
                        "  <head> \n" +
                        "    <title>Chat by ChatBuilder App</title> \n" +
                        "    <meta charset=\"UTF-8\"> <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\"> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n" +
                        "    <style>\n" +
                        "        html {box-sizing: border-box;height: 100%;margin: 0;padding: 0;}\n" +
                        "body {\n" +
                        "  -webkit-font-smoothing: antialiased;\n" +
                        "  -moz-osx-font-smoothing: grayscale;\n" +
                        "  font-family: \"Roboto\", sans-serif;\n" +
                        "  margin: 0;\n" +
                        "  padding: 0;\n" +
                        "  height: 100%;\n" +
                        "  background:#e7dcd2;\n" +
                        "}\n" +
                        ".user-bar {\n" +
                        "  height: 55px;\n" +
                        "  background: #005e54;\n" +
                        "  color: #fff;\n" +
                        "  font-size: 24px;\n" +
                        "  position: absolute;\n" +
                        "  width:100%;\n" +
                        "  z-index: 1;\n" +
                        "}\n" +
                        ".user-bar .avatar {\n" +
                        "  margin: 0 0 0 5px;\n" +
                        "  padding-top:9px;\n" +
                        "  width: 36px;\n" +
                        "  height: 36px;\n" +
                        "  margin-left:12px;\n" +
                        "}\n" +
                        ".user-bar .avatar svg {\n" +
                        "  border-radius: 50%;\n" +
                        "  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.1);\n" +
                        "  display: inline;width: 100%;\n" +
                        "}\n" +
                        ".user-bar .name {\n" +
                        "  position:absolute;\n" +
                        "  top:10px;\n" +
                        "  left:50px;\n" +
                        "  font-size: 17px;\n" +
                        "  font-weight: 600;\n" +
                        "  text-overflow: ellipsis;\n" +
                        "  letter-spacing: 0.3px;\n" +
                        "  margin: 0 0 0 8px;\n" +
                        "  overflow: hidden;\n" +
                        "  white-space: nowrap;\n" +
                        "  width: auto;\n" +
                        "}\n" +
                        ".user-bar .status {\n" +
                        "  margin-left:1px;\n" +
                        "  display: block;\n" +
                        "  font-size: 13px;\n" +
                        "  font-weight: 400;\n" +
                        "  letter-spacing: 0;\n" +
                        "  width:auto;\n" +
                        "}\n" +
                        ".actionsbar {\n" +
                        "    position:absolute;\n" +
                        "    height:55px;\n" +
                        "    top:0;\n" +
                        "    right:0;\n" +
                        "}\n" +
                        ".actionsbar svg {\n" +
                        "    height:25px;\n" +
                        "    width:25px;\n" +
                        "    margin:15px;\n" +
                        "    margin-left:5px;\n" +
                        "    margin-right:8px;\n" +
                        "}\n" +
                        "#tdots {\n" +
                        "    margin-left:0px;\n" +
                        "}\n" +
                        ".conversation {\n" +
                        "  position: absolute;\n" +
                        "  background: #e7dcd2;\n" +
                        "}\n" +
                        ".conversation ::-webkit-scrollbar {\n" +
                        "  transition: all .5s;\n" +
                        "  width: 5px;\n" +
                        "  height: 1px;\n" +
                        "  z-index: 10;\n" +
                        "}\n" +
                        ".conversation ::-webkit-scrollbar-track {\n" +
                        "  background: transparent;\n" +
                        "}\n" +
                        ".conversation ::-webkit-scrollbar-thumb {\n" +
                        "  background: #b3ada7;\n" +
                        "}\n" +
                        ".conversation .conversation-container {\n" +
                        "  position: relative;\n" +
                        "  top:55px;\n" +
                        "  box-shadow: inset 0 10px 10px -10px #000000;\n" +
                        "  overflow-x: hidden;\n" +
                        "  padding:0px 16px;\n" +
                        "  margin-bottom: 5px;\n" +
                        "  height:calc(100vh - 115px);\n" +
                        "  z-index:3;\n" +
                        "  width:calc(100vw - 32px);\n" +
                        "}\n" +
                        ".note {\n" +
                        "  clear:both;\n" +
                        "  line-height: 18px;\n" +
                        "  padding: 6px;\n" +
                        "  padding-left:8px;\n" +
                        "  margin-top:5px;\n" +
                        "  max-width: 100%;\n" +
                        "  padding:8px;\n" +
                        "  background-color:#f7f7f0;\n" +
                        "  border-radius:10px;\n" +
                        "  font-size:12px;\n" +
                        "  font-weight:300;\n" +
                        "  width:fit-content;\n" +
                        "  height:fit-content;\n" +
                        "  margin-left:50%;\n" +
                        "  transform: translateX(-50%);\n" +
                        "  text-align: center;\n" +
                        "}\n" +
                        ".message {\n" +
                        "  color: #000;\n" +
                        "  clear: both;\n" +
                        "  line-height: 18px;\n" +
                        "  font-size: 15px;\n" +
                        "  padding: 6px;\n" +
                        "  padding-left:8px;\n" +
                        "  position: relative;\n" +
                        "  margin: 2px 0;\n" +
                        "  margin-top:5px;\n" +
                        "  max-width: 80%;\n" +
                        "  word-wrap: break-word;\n" +
                        "}\n" +
                        ".message2 {\n" +
                        "  color: #000;\n" +
                        "  clear: both;\n" +
                        "  line-height: 18px;\n" +
                        "  font-size: 15px;\n" +
                        "  padding: 6px;\n" +
                        "  padding-left:8px;\n" +
                        "  position: relative;\n" +
                        "  margin: 1px;\n" +
                        "  max-width: 80%;\n" +
                        "  word-wrap: break-word;\n" +
                        "}\n" +
                        ".message:after {\n" +
                        "  position: absolute;\n" +
                        "  content: \"\";\n" +
                        "  width: 0;\n" +
                        "  height: 0;\n" +
                        "  border-style: solid;\n" +
                        "}\n" +
                        ".metadata {\n" +
                        "  float: right;\n" +
                        "  padding: 0 0 0 7px;\n" +
                        "  position: relative;\n" +
                        "  bottom: -4px;\n" +
                        "}\n" +
                        ".metadata .time {\n" +
                        "  color: rgba(0, 0, 0, .45);\n" +
                        "  font-size: 11px;\n" +
                        "  display: inline-block;\n" +
                        "}\n" +
                        ".metadata .tick {\n" +
                        "  display: inline-block;\n" +
                        "  margin-left: 2px;\n" +
                        "  position: relative;\n" +
                        "  top: 4px;\n" +
                        "  height: 16px;\n" +
                        "  width: 16px;\n" +
                        "  background-image: url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='15'%3E%3Cpath d='M15.01 3.316l-.478-.372a.365.365 0 0 0-.51.063L8.666 9.88a.32.32 0 0 1-.484.032l-.358-.325a.32.32 0 0 0-.484.032l-.378.48a.418.418 0 0 0 .036.54l1.32 1.267a.32.32 0 0 0 .484-.034l6.272-8.048a.366.366 0 0 0-.064-.512zm-4.1 0l-.478-.372a.365.365 0 0 0-.51.063L4.566 9.88a.32.32 0 0 1-.484.032L1.892 7.77a.366.366 0 0 0-.516.005l-.423.433a.364.364 0 0 0 .006.514l3.255 3.185a.32.32 0 0 0 .484-.033l6.272-8.048a.365.365 0 0 0-.063-.51z' fill='%234fc3f7'/%3E%3C/svg%3E\"); background-repeat: no-repeat;\n" +
                        "}\n" +
                        ".metadata .tick svg:first-child {\n" +
                        "  -webkit-backface-visibility: hidden;\n" +
                        "          backface-visibility: hidden;\n" +
                        "  -webkit-transform: perspective(800px) rotateY(180deg);\n" +
                        "          transform: perspective(800px) rotateY(180deg);\n" +
                        "}\n" +
                        ".metadata .tick svg:last-child {\n" +
                        "  -webkit-backface-visibility: hidden;\n" +
                        "          backface-visibility: hidden;\n" +
                        "  -webkit-transform: perspective(800px) rotateY(0deg);\n" +
                        "          transform: perspective(800px) rotateY(0deg);\n" +
                        "}\n" +
                        ".metadata .tick-animation svg:first-child {\n" +
                        "  -webkit-transform: perspective(800px) rotateY(0);\n" +
                        "          transform: perspective(800px) rotateY(0);\n" +
                        "}\n" +
                        ".metadata .tick-animation svg:last-child {\n" +
                        "  -webkit-transform: perspective(800px) rotateY(-179.9deg);\n" +
                        "transform: perspective(800px) rotateY(-179.9deg);\n" +
                        "}\n" +
                        ".message:first-child {\n" +
                        "  margin: 16px 0 8px;\n" +
                        "}\n" +
                        ".message.received {\n" +
                        "  background: #fff;\n" +
                        "  border-radius: 0px 8px 8px 8px;\n" +
                        "  float: left;\n" +
                        "}\n" +
                        ".message2.received {\n" +
                        "  background: #fff;\n" +
                        "  border-radius: 8px 8px 8px 8px;\n" +
                        "  float: left;\n" +
                        "}\n" +
                        ".message.received .metadata {\n" +
                        "  padding: 0 0 0 16px;\n" +
                        "}\n" +
                        ".message.received:after {\n" +
                        "  border-width: 0px 10px 10px 0;\n" +
                        "  border-radius:5px 0px 0px 0px;\n" +
                        "  border-color: transparent #fff transparent transparent;\n" +
                        "  top: 0;left: -10px;\n" +
                        "}\n" +
                        ".message.sent {\n" +
                        "  background: #e1ffc7;\n" +
                        "  border-radius: 8px 0px 8px 8px;\n" +
                        "  float: right;\n" +
                        "}\n" +
                        ".message2.sent {\n" +
                        "  background: #e1ffc7;\n" +
                        "  border-radius: 8px 8px 8px 8px;\n" +
                        "  float: right;\n" +
                        "}\n" +
                        ".message.sent:after {\n" +
                        "  border-width: 0px 0px 10px 10px;\n" +
                        "  border-radius:0px 5px 0px 0px;\n" +
                        "  border-color: transparent transparent transparent #e1ffc7;\n" +
                        "  top: 0;\n" +
                        "  right: -10px;\n" +
                        "}\n" +
                        ".marvel-device .status-bar {\n" +
                        "    display: none;\n" +
                        "  }\n" +
                        ".convochatbg {\n" +
                        "   top:calc(100vh - 60px);\n" +
                        "   position: absolute;\n" +
                        "   background: #e7dcd2;\n" +
                        "   height:60px;\n" +
                        "   bottom:0px; \n" +
                        "   width: 100vw;\n" +
                        "} \n" +
                        ".msginput { position: relative; background: #fff; height: 45px; top:7.5px; left:6.5px; margin-right: 66px; border-radius: 50px; } \n" +
                        "#mic { position: absolute; width: 47px; height: 47px; top:6.5px; right: 6.5px; } \n" +
                        "#emoji { position: absolute; height: 28px; width: 28px; top:8.5px; left:8.5px; } \n" +
                        "#messagetxt { position: absolute; color: #666; font-weight: 500; left:43px; top:-1.5px; } \n" +
                        "#docs { position: absolute; height: 28px; width: 28px; top:8.5px; right:12px; }\n" +
                        ".username {position:relative; font-size:14px; font-weight:600; color:black; top:-1px;}\n" +
                        ".chatbuilder-bubble {\n" +
                        "        position: fixed;\n" +
                        "        bottom: 60px;\n" +
                        "        right: 0;\n" +
                        "        background: white;\n" +
                        "        border-radius: 16px 0 0 16px;\n" +
                        "        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);\n" +
                        "        display: flex;\n" +
                        "        align-items: center;\n" +
                        "        padding: 8px;\n" +
                        "        padding-right: 20px;\n" +
                        "        font-family: \"Segoe UI\", sans-serif;\n" +
                        "        font-size: 16px;\n" +
                        "        font-weight: 600;\n" +
                        "        color: #222;\n" +
                        "        cursor: pointer;\n" +
                        "        transition: width 0.4s ease, padding 0.4s ease;\n" +
                        "        overflow: hidden;\n" +
                        "        white-space: nowrap;\n" +
                        "        max-width: 260px;\n" +
                        "        z-index: 100;\n" +
                        "      }\n" +
                        "      .chatbuilder-bubble.collapsed {\n" +
                        "        transform: translateX(80%);\n" +
                        "        padding: 12px;\n" +
                        "        justify-content: center;\n" +
                        "      }\n" +
                        "      .chatbuilder-icon {\n" +
                        "        margin-right: 12px;\n" +
                        "        flex-shrink: 0;\n" +
                        "      }\n" +
                        "      .chatbuilder-bubble div {\n" +
                        "        padding-left: 8px;\n" +
                        "      }\n" +
                        "      .chatbuilder-bubble div p {\n" +
                        "        margin: 0; padding: 0;\n" +
                        "        font-size: 14px;\n" +
                        "        line-height: 16px;\n" +
                        "      }\n" +
                        "      .chatbuilder-link {\n" +
                        "        -webkit-tap-highlight-color: transparent;outline: none;line-height: 20px;\n" +
                        "        font-size: 20px;\n" +
                        "        font-weight: 700;\n" +
                        "        color: #1976d2;\n" +
                        "        text-decoration: none;\n" +
                        "      }\n" +
                        "      #arrow {\n" +
                        "        transform: scaleY(2);\n" +
                        "        color: lightgray;\n" +
                        "        padding-left: 8px;\n" +
                        "        padding-right: 20px;\n" +
                        "        padding-bottom: 3px;\n" +
                        "        height: 100%;\n" +
                        "      }\n" +
                        "      #arrow.collapsed {\n" +
                        "        transform: rotateZ(90deg);\n" +
                        "      }\n" +
                        "    </style>\n" +
                        "</head> \n" +
                        "<body>\n" +
                        "<div class=\"chat\">\n" +
                        "     <div class=\"chat-container\">\n" +
                        "            <div id=\"call\" class=\"user-bar\">\n" +
                        "                <div class=\"back\">\n" +
                        "                <i class=\"zmdi zmdi-arrow-left\"></i>\n" +
                        "              </div>\n" +
                        "              <div class=\"avatar\">\n" +
                        "        <svg     version=\"1.1\"     xmlns=\"http://www.w3.org/2000/svg\"     xmlns:xlink=\"http://www.w3.org/1999/xlink\"     x=\"0%\" y=\"0%\"     width=\"100%\" height=\"100%\"     viewBox=\"0 0 24.0 24.0\"     enable-background=\"new 0 0 24.0 24.0\"     xml:space=\"preserve\">     <path         fill=\"#CDD8E0\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M11.98,0.19C18.54,0.19,23.86,5.51,23.86,12.07C23.86,18.63,18.54,23.95,11.98,23.95C5.42,23.95,0.10,18.63,0.10,12.07C0.10,5.51,5.42,0.19,11.98,0.19z\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M11.93,6.50C14.17,6.50,15.99,8.32,15.99,10.56C15.99,12.80,14.17,14.62,11.93,14.62C9.69,14.62,7.87,12.80,7.87,10.56C7.87,8.32,9.69,6.50,11.93,6.50z\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M4.21,20.40C5.45,15.85,15.91,13.33,19.60,20.42C16.60,24.14,8.16,24.74,4.21,20.40z\"/> </svg>\n" +
                        "              </div>\n" +
                        "              <div class=\"name\">\n" +
                        "                <span id=\"name\">";
                val fromNameUntilBody = "</span>\n" +
                        "                <span class=\"status\">ChatBuilder by Vinaykpro</span>\n" +
                        "              </div>\n" +
                        "              <div class=\"actionsbar\">\n" +
                        "<svg     version=\"1.1\"     xmlns=\"http://www.w3.org/2000/svg\"     xmlns:xlink=\"http://www.w3.org/1999/xlink\"     x=\"0%\" y=\"0%\"     width=\"100%\" height=\"100%\"     viewBox=\"0 0 24.0 24.0\"     enable-background=\"new 0 0 24.0 24.0\"     xml:space=\"preserve\">     <path         fill=\"#FFFFFF\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M5.03,6.56L14.22,6.56A2.31 2.31 0 0 1 16.53,8.88L16.53,16.30A2.31 2.31 0 0 1 14.22,18.62L5.03,18.62A2.31 2.31 0 0 1 2.72,16.30L2.72,8.88A2.31 2.31 0 0 1 5.03,6.56z\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M21.38,16.08C21.38,17.46,20.45,17.32,19.87,16.83L17.41,14.78Q17.04,14.41,17.04,13.83L17.04,10.94Q17.04,10.47,17.41,10.09L19.87,8.03C20.39,7.56,21.44,7.77,21.44,8.93L21.38,16.08z\"/> </svg>\n" +
                        "<svg\n" +
                        "    version=\"1.1\"\n" +
                        "    xmlns=\"http://www.w3.org/2000/svg\"\n" +
                        "    xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                        "    x=\"0%\" y=\"0%\"\n" +
                        "    width=\"100%\" height=\"100%\"\n" +
                        "    viewBox=\"0 0 24.0 24.0\"\n" +
                        "    enable-background=\"new 0 0 24.0 24.0\"\n" +
                        "    xml:space=\"preserve\">\n" +
                        "    <path\n" +
                        "        fill=\"#FFFFFF\"\n" +
                        "        stroke=\"#000000\"\n" +
                        "        fill-opacity=\"1.000\"\n" +
                        "        stroke-opacity=\"1.000\"\n" +
                        "        fill-rule=\"nonzero\"\n" +
                        "        stroke-width=\"0.0\"\n" +
                        "        stroke-linejoin=\"miter\"\n" +
                        "        stroke-linecap=\"square\"\n" +
                        "        d=\"M20.00,15.52L19.95,18.47C20.00,19.26,19.46,19.81,18.77,19.88C11.36,20.56,3.56,13.33,3.99,5.06Q4.10,3.96,5.34,3.96L7.74,3.96Q9.13,3.96,9.31,5.54L9.53,8.03Q9.60,8.99,8.79,9.42C7.10,10.32,7.36,11.16,8.09,12.18Q9.78,14.37,12.49,16.09C14.12,17.06,13.98,14.44,15.17,14.44L19.04,14.68Q20.01,14.77,20.00,15.52z\"/>\n" +
                        "</svg>\n" +
                        "\n" +
                        "<svg id=\"tdots\"\n" +
                        "    version=\"1.1\"\n" +
                        "    xmlns=\"http://www.w3.org/2000/svg\"\n" +
                        "    xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                        "    x=\"0%\" y=\"0%\"\n" +
                        "    width=\"100%\" height=\"100%\"\n" +
                        "    viewBox=\"0 0 24.0 24.0\"\n" +
                        "    enable-background=\"new 0 0 24.0 24.0\"\n" +
                        "    xml:space=\"preserve\">\n" +
                        "    <path\n" +
                        "        fill=\"#FFFFFF\"\n" +
                        "        stroke=\"#000000\"\n" +
                        "        fill-opacity=\"1.000\"\n" +
                        "        stroke-opacity=\"1.000\"\n" +
                        "        fill-rule=\"nonzero\"\n" +
                        "        stroke-width=\"0.0\"\n" +
                        "        stroke-linejoin=\"miter\"\n" +
                        "        stroke-linecap=\"square\"\n" +
                        "        d=\"M11.69,4.38C12.81,4.38,13.72,5.28,13.72,6.41C13.72,7.53,12.81,8.43,11.69,8.43C10.57,8.43,9.66,7.53,9.66,6.41C9.66,5.28,10.57,4.38,11.69,4.38z\"/>\n" +
                        "    <path\n" +
                        "        fill=\"#FFFFFF\"\n" +
                        "        stroke=\"#000000\"\n" +
                        "        fill-opacity=\"1.000\"\n" +
                        "        stroke-opacity=\"1.000\"\n" +
                        "        fill-rule=\"nonzero\"\n" +
                        "        stroke-width=\"0.0\"\n" +
                        "        stroke-linejoin=\"miter\"\n" +
                        "        stroke-linecap=\"square\"\n" +
                        "        d=\"M11.69,9.83C12.81,9.83,13.72,10.74,13.72,11.86C13.72,12.98,12.81,13.89,11.69,13.89C10.57,13.89,9.66,12.98,9.66,11.86C9.66,10.74,10.57,9.83,11.69,9.83z\"/>\n" +
                        "    <path\n" +
                        "        fill=\"#FFFFFF\"\n" +
                        "        stroke=\"#000000\"\n" +
                        "        fill-opacity=\"1.000\"\n" +
                        "        stroke-opacity=\"1.000\"\n" +
                        "        fill-rule=\"nonzero\"\n" +
                        "        stroke-width=\"0.0\"\n" +
                        "        stroke-linejoin=\"miter\"\n" +
                        "        stroke-linecap=\"square\"\n" +
                        "        d=\"M11.69,15.48C12.81,15.48,13.72,16.39,13.72,17.51C13.72,18.63,12.81,19.54,11.69,19.54C10.57,19.54,9.66,18.63,9.66,17.51C9.66,16.39,10.57,15.48,11.69,15.48z\"/>\n" +
                        "</svg>\n" +
                        "              </div>\n" +
                        "            </div>\n" +
                        "            <div class=\"conversation\">\n" +
                        "                <div id=\"messagesBox\" class=\"conversation-container\">\n\n"

                val fromBodyToEnd = "</div>\n" +
                        "            <div class=\"convochatbg\"> \n" +
                        "            <div class=\"msginput\">\n" +
                        "            <!-- Emoji --><svg id=\"emoji\"     version=\"1.1\"     xmlns=\"http://www.w3.org/2000/svg\"     xmlns:xlink=\"http://www.w3.org/1999/xlink\"     x=\"0%\" y=\"0%\"     width=\"100%\" height=\"100%\"     viewBox=\"0 0 24.0 24.0\"     enable-background=\"new 0 0 24.0 24.0\"     xml:space=\"preserve\">     <path fill=\"#FFFFFF\" stroke=\"#86949B\" fill-opacity=\"0.000\" stroke-opacity=\"1.000\" fill-rule=\"nonzero\"         stroke-width=\"0.8010312\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M11.96,3.06C16.99,3.06,21.07,7.13,21.07,12.16C21.07,17.19,16.99,21.27,11.96,21.27C6.93,21.27,2.85,17.19,2.85,12.16C2.85,7.13,6.93,3.06,11.96,3.06z\"/>     <path         fill=\"#86949B\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M9.34,8.47C9.92,8.47,10.39,9.21,10.39,10.12C10.39,11.02,9.92,11.76,9.34,11.76C8.75,11.76,8.28,11.02,8.28,10.12C8.28,9.21,8.75,8.47,9.34,8.47z\"/>     <path         fill=\"#86949B\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M14.56,8.47C15.14,8.47,15.61,9.21,15.61,10.12C15.61,11.02,15.14,11.76,14.56,11.76C13.98,11.76,13.50,11.02,13.50,10.12C13.50,9.21,13.98,8.47,14.56,8.47z\"/>     <path         fill=\"#86949B\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M7.07,14.64C9.02,18.94,14.56,18.94,17.09,14.47Q17.56,13.19,16.38,13.31Q11.96,13.80,7.54,13.28Q6.33,13.28,7.07,14.64z\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M8.33,14.76C10.39,16.16,13.95,16.16,15.77,14.79Q16.88,13.80,15.52,14.12Q11.91,14.45,8.50,14.06Q7.07,13.80,8.33,14.76z\"/> </svg> \n" +
                        "            <!-- Message --><p id=\"messagetxt\">Message</p> \n" +
                        "            <!-- Docs --><svg id=\"docs\"     version=\"1.1\"     xmlns=\"http://www.w3.org/2000/svg\"     xmlns:xlink=\"http://www.w3.org/1999/xlink\"     x=\"0%\" y=\"0%\"     width=\"100%\" height=\"100%\"     viewBox=\"0 0 24.0 24.0\"     enable-background=\"new 0 0 24.0 24.0\"     xml:space=\"preserve\">     <path         fill=\"#000000\"         stroke=\"#86949B\"         fill-opacity=\"0.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"1.08\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M12.66,5.77L20.28,13.36C23.27,16.09,17.20,23.05,13.90,19.75L3.98,9.82C1.55,7.26,6.44,3.26,8.49,5.44L15.76,13.01C17.09,14.22,15.46,16.54,13.54,14.87L8.70,10.18\"/> </svg> </div> <svg id=\"mic\" version=\"1.1\"     xmlns=\"http://www.w3.org/2000/svg\"     xmlns:xlink=\"http://www.w3.org/1999/xlink\"     x=\"0%\" y=\"0%\"     width=\"100%\" height=\"100%\"     viewBox=\"0 0 24.0 24.0\"     enable-background=\"new 0 0 24.0 24.0\"     xml:space=\"preserve\">     <path         fill=\"#2BA783\"         stroke=\"#000000\"         fill-opacity=\"1.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.0\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M12.11,0.98C18.26,0.98,23.25,5.97,23.25,12.13C23.25,18.28,18.26,23.27,12.11,23.27C5.95,23.27,0.96,18.28,0.96,12.13C0.96,5.97,5.95,0.98,12.11,0.98z\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#FFFFFF\"         fill-opacity=\"0.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"2.6880002\"         stroke-linejoin=\"miter\"         stroke-linecap=\"round\"         d=\"M12.13,8.84L12.13,11.87\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#FFFFFF\"         fill-opacity=\"0.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.84000003\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M9.31,12.30C9.55,15.43,14.65,15.33,15.00,12.30\"/>     <path         fill=\"#FFFFFF\"         stroke=\"#FFFFFF\"         fill-opacity=\"0.000\"         stroke-opacity=\"1.000\"         fill-rule=\"nonzero\"         stroke-width=\"0.84000003\"         stroke-linejoin=\"miter\"         stroke-linecap=\"square\"         d=\"M12.16,14.71L12.16,16.39\"/> </svg> \n" +
                        "            </div>\n" +
                        "    </div> \n" +
                        "</div> \n" +
                        "</div>\n" +
                        "</div> \n" +
                        "</div>\n" +
                        "</div> \n" +
                        "</div>\n" +
                        "<div id=\"chatbuilderBubble\" class=\"chatbuilder-bubble\">\n" +
                        "      <p id=\"arrow\">></p>\n" +
                        "      <img\n" +
                        "        src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAACxEAAAsRAX9kX5EAAAXJaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA5LjEtYzAwMiA3OS5kYmEzZGEzLCAyMDIzLzEyLzEzLTA1OjA2OjQ5ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIiB4bWxuczpzdEV2dD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL3NUeXBlL1Jlc291cmNlRXZlbnQjIiB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1lbnRzLzEuMS8iIHhtbG5zOnBob3Rvc2hvcD0iaHR0cDovL25zLmFkb2JlLmNvbS9waG90b3Nob3AvMS4wLyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgMjUuNyAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDI1LTA4LTE4VDIyOjA2OjQ3KzA1OjMwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDI1LTA4LTE4VDIyOjA2OjQ3KzA1OjMwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAyNS0wOC0xOFQyMjowNjo0NyswNTozMCIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo2YWY2OGE4ZS05ZmU5LWVhNDYtOTZiYy0zMGQxMzM3M2Y4YjEiIHhtcE1NOkRvY3VtZW50SUQ9ImFkb2JlOmRvY2lkOnBob3Rv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\"\n" +
                        "      />\n" +
                        "      <div>\n" +
                        "        <p class=\"chatbuilder-text\">Exported by</p>\n" +
                        "        <a\n" +
                        "          href=\"https://play.google.com/store/apps/details?id=com.vinaykpro.chatbuilder\"\n" +
                        "          target=\"_blank\"\n" +
                        "          class=\"chatbuilder-link\"\n" +
                        "          >ChatBuilder</a>\n" +
                        "      </div>\n" +
                        "    </div>\n" +
                        "<script>\n" +
                        "    window.onload = () => {\n" +
                        "    const bubble = document.getElementById(\"chatbuilderBubble\");\n" +
                        "      const arrow = document.getElementById(\"arrow\");\n" +
                        "      setTimeout(() => {\n" +
                        "        bubble.classList.add(\"collapsed\");\n" +
                        "        arrow.innerHTML = '<'\n" +
                        "      }, 5000);\n" +
                        "      arrow.addEventListener(\"click\", (e) => {\n" +
                        "        bubble.classList.toggle(\"collapsed\");\n" +
                        "        arrow.innerHTML = arrow.innerText == '<' ? '>' : '<'\n" +
                        "      });\n" +
                        "    }\n" +
                        "</script>\n" +
                        "</body>\n" +
                        " </html>"

                var ind = 0
                var progress = 0
                var messagesBody = StringBuilder()
                var lastUserId = 0
                var lastDate = ""
                for (msg in fullMessageList!!) {
                    val id: Int = msg.userid ?: 0
                    val name: String = msg.username ?: ""
                    val message: String = msg.message?.htmlEncode() ?: ""
                    if (msg.date != lastDate) {
                        messagesBody.append(getNote(msg.date ?: ""))
                    }
                    messagesBody.append(
                        if (id == 0) {
                            getNote(msg.date ?: "")
                        } else if (id == senderId) getSentMessage(
                            message,
                            name,
                            msg.time ?: "",
                            id != lastUserId
                        )
                        else getReceivedMessage(message, name, msg.time ?: "", id != lastUserId)
                    )
                    lastDate = msg.date ?: ""
                    lastUserId = id
                    if (ind >= ((fullMessageList?.size ?: 0) * (progress / 100.toFloat()))) {
                        if (progress < 100) progress++
                        onUpdate(progress)
                    }
                    ind++
                }

                val finalHtmlCode = htmlUntilName + chatName +
                        fromNameUntilBody +
                        messagesBody.toString() +
                        fromBodyToEnd
                val chatInfo =
                    if (chatName.contains("chat with")) chatName else "Chat with $chatName"
                saveFileToDownloads(
                    context as Activity,
                    "$chatInfo.html",
                    "text/html",
                    writeData = { out ->
                        out.write(finalHtmlCode.toByteArray(Charsets.UTF_8))
                    },
                    onSaved = { uri ->
                        onDone(uri)
                    },
                    onDenied = { }
                )
            }
        }
    }

    fun saveFileToDownloads(
        activity: Activity,
        fileName: String,
        mimeType: String,
        writeData: (OutputStream) -> Unit,
        onSaved: (Uri?) -> Unit,
        onDenied: () -> Unit
    ) {
        // Android 9 and below → check permission
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                onDenied()
                return
            }
        }

        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = activity.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/ChatBuilder")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    writeData(out) // directly write your data here
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } else {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val chatBuilderDir = File(downloadsDir, "ChatBuilder")
            if (!chatBuilderDir.exists()) chatBuilderDir.mkdirs()

            val file = File(chatBuilderDir, fileName)
            FileOutputStream(file).use { out ->
                writeData(out)
            }
            Uri.fromFile(file)
        }

        onSaved(uri)
    }

    fun getNote(text: String): String {
        return "<div class='note'>$text</div>"
    }

    fun getSentMessage(text: String, name: String, time: String, isFirst: Boolean): String {
        return if (isFirst) {
            "<div class='message sent'><span class='username'>$name</span><br>$text<span class='metadata'> <span class='time'>$time</span><span class='tick'></span></span></div>"
        } else {
            "<div class='message2 sent'>$text<span class='metadata'> <span class='time'>$time</span><span class='tick'></span></span></div>"
        }
    }

    fun getReceivedMessage(text: String, name: String, time: String, isFirst: Boolean): String {
        return if (isFirst) {
            "<div class='message received'><span class='username'>$name</span><br>$text <span class='metadata'> <span class='time'>$time</span></span></span></div>"
        } else {
            "<div class='message2 received'>$text <span class='metadata'> <span class='time'>$time</span></span></span></div>"
        }
    }

    fun updateMessageBarUserIndex(dir: Int) {
        val updated = _messageBarSenderIndex.value + dir
        if (updated > 0 && updated < userList.size) {
            _messageBarSenderIndex.value = updated
        }
    }

    fun updateReceiverNameVisibility(visible: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatDao.updateReceiverVisibleState(chatId, visible)
            }
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

    fun updateSenderId(senderId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatDao.updateSender(chatId, senderId)
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