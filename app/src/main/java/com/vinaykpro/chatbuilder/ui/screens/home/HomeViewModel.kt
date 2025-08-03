package com.vinaykpro.chatbuilder.ui.screens.home

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.IMPORTRESULT
import com.vinaykpro.chatbuilder.data.local.IMPORTSTATE
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import com.vinaykpro.chatbuilder.data.local.ZipItem
import com.vinaykpro.chatbuilder.data.utils.FileIOHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val instance = AppDatabase.getInstance(application)
    private val dao = instance.chatDao()
    private val filesDao = instance.fileDao()
    private val messageDao = instance.messageDao()
    private val fileHelper = FileIOHelper(context = getApplication<Application>())
    private var importJob: Job? = null
    private var saveJob: Job? = null

    var importState by mutableIntStateOf(IMPORTSTATE.NONE)
    var importedMesssages: List<MessageEntity> = emptyList()
    var importedFileList by mutableStateOf<List<ZipItem>>(emptyList())
    var mediaIndexes: List<Int> = emptyList()

    val chatsList: StateFlow<List<ChatEntity>> = dao.getAllChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var rewardedAd: RewardedAd? = null
    var rewardedAdState: Int = 0 // 0 -> loading, 1 -> adAvailable, -1 failedToLoad
    var importMedia: Boolean? = null
    var chatId: Int? = null

    fun loadRewardedAd(context: Context, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-2813592783630195/7841555247",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAdState = 1
                    continueImport()
                    onLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    rewardedAdState = -1
                    continueImport()
                    onFailed(loadAdError.message)
                }
            }
        )
    }

    fun continueImport(atMedia: Boolean = false) {
        if (atMedia) importState = IMPORTSTATE.ALMOSTCOMPLETED
        if (importedMesssages.isNotEmpty() && rewardedAdState != 0 && importMedia != null) {
            val state = rewardedAdState
            if (state == 1) importState = IMPORTSTATE.WATCHAD
            else {
                keepOrSkipFiles(importMedia == true)
                importMedia = null
            }
        }
    }

    fun startRewardAd(context: Context) {
        rewardedAd?.show(context as Activity) {
            rewardedAdState = 0
            rewardedAd = null
            keepOrSkipFiles(importMedia == true)
            importMedia = null
        }
    }

    fun importChatFromFile(context: Context, file: Uri) {
        if (rewardedAd == null) {
            rewardedAdState = 0
            loadRewardedAd(context, onLoaded = {
                rewardedAdState = 1
            }, onFailed = {
                rewardedAdState = -1
            })
        }
        importJob = viewModelScope.launch {
            val id = dao.addOrUpdateChat(
                ChatEntity(
                    name = "New Chat",
                    lastmsg = "Import in progress"
                )
            )
            chatId = id.toInt()
            fileHelper.init(id.toInt(), file)
            importState = IMPORTSTATE.STARTED
            val res = fileHelper.checkFile()
            Log.i("vkpro", "vm res ${res.result}")
            if (res.result == IMPORTRESULT.SUCCESS) {
                importedMesssages = res.messages ?: emptyList()
                val lastMessage = importedMesssages[importedMesssages.size - 1]
                val beginningMsgId = messageDao.getLastMessageId() ?: 0
                dao.addOrUpdateChat(
                    ChatEntity(
                        chatid = id.toInt(),
                        name = res.name ?: "New Chat ${id.toInt()}",
                        showReceiverName = res.users != null && res.users.size > 2,
                        senderId = res.senderId,
                        lastOpenedMsgId = if (beginningMsgId == 0) 0 else beginningMsgId + 1,
                        lastmsg = lastMessage.message ?: "",
                        lastmsgtime = lastMessage.date ?: "",
                    )
                )
                if (res.isMediaFound) {
                    importedFileList = res.mediaItems ?: emptyList()
                    mediaIndexes = res.mediaIndexes ?: emptyList()
                    importState = IMPORTSTATE.MEDIASELECTION
                } else {
                    importMedia = false
                    if (rewardedAdState != 0) continueImport()
                }
            } else {
                dao.deleteChatById(id.toInt())
                importState = IMPORTSTATE.UNSUPPORTEDFILE
            }

            Log.i("vkpro", res.response)
        }
    }

    fun keepOrSkipFiles(keep: Boolean = true) {
        importState = IMPORTSTATE.ALMOSTCOMPLETED
        saveJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (keep) {
                    val filesRes = fileHelper.saveFiles(importedFileList)
                    val fileIDs = filesDao.addFiles(filesRes)
                    var ids = ""
                    var mIxs = ""
                    fileIDs.forEach { l -> ids += ",${l.toInt()}" }
                    mediaIndexes.forEach { x -> mIxs += "$x," }
                    Log.i("vkpro", "added ids: $ids")
                    Log.i("vkpro", "media idxs we got: $mIxs")
                    if (filesRes.isNotEmpty()) {
                        Log.i("vkpro", "inside modifying messages")
                        try {
                            val messages = importedMesssages.toMutableList()
                            for (i in mediaIndexes) {
                                val message = messages[i].message!!
                                Log.i("vkpro", "inside checking messages \nmsg: $message")
                                for (ind in 0 until filesRes.size) {
                                    val file = filesRes[ind]
                                    if (message.contains(file.displayname)) {
                                        val newMsg = message.lines().takeIf { it.size > 1 }?.drop(1)
                                            ?.joinToString("\n")
                                        messages[i] =
                                            messages[i].copy(
                                                fileId = fileIDs[ind].toInt(),
                                                message = newMsg
                                            )
                                        Log.i("vkpro", "Found!! oldMsg $message ; newMsg $newMsg")
                                        break
                                    }
                                }
                            }
                            importedMesssages = messages
                        } catch (e: Exception) {
                            Log.i("vkpro", "Error matching: ${e.toString()}")
                        }
                    }
                }
                messageDao.insertMessages(messages = importedMesssages)
                importState = IMPORTSTATE.SUCCESS
            }
            importedMesssages = emptyList()
            importedFileList = emptyList()
            mediaIndexes = emptyList()
        }
    }

    fun continueImportWithMedia() {

    }

    fun addChat() {
        viewModelScope.launch {
            val newId = dao.addOrUpdateChat(
                ChatEntity(
                    name = "New chat",
                )
            )
            dao.addOrUpdateChat(
                ChatEntity(
                    chatid = newId.toInt(),
                    name = "New Chat $newId",
                    status = "Tap to edit",
                    lastmsg = "Tap to open and customize"
                )
            )
        }
    }

    fun closeImport() {
        if (importState != IMPORTSTATE.SUCCESS) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    chatId?.let {
                        dao.deleteChatById(chatId!!)
                        chatId = null
                    }
                }
            }
        }
        importState = IMPORTSTATE.NONE
        importJob?.cancel()
        saveJob?.cancel()
        importedMesssages = emptyList()
        importedFileList = emptyList()
        mediaIndexes = emptyList()
    }
}