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
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val instance = AppDatabase.getInstance(application)
    private val dao = instance.chatDao()
    private val filesDao = instance.fileDao()
    private val messageDao = instance.messageDao()
    private val fileHelper = FileIOHelper(context = getApplication<Application>())
    private var importJob: Job? = null
    private var saveJob: Job? = null

    private var isPremium: Boolean = false

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

    var isSelectionMode by mutableStateOf(false)
        private set
    var selectedChatIds by mutableStateOf(setOf<Int>())
        private set

    var clearChatsVisible by mutableStateOf(false)
        private set

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
            if (state == 1 && !isPremium) importState = IMPORTSTATE.WATCHAD
            else {
                keepOrSkipFiles(importMedia == true)
                importMedia = null
            }
        }
    }

    fun startRewardAd(context: Context) {
        val activity = context as? Activity ?: return
        rewardedAd?.show(activity) {
            rewardedAdState = 0
            rewardedAd = null
            keepOrSkipFiles(importMedia == true)
            importMedia = null
        }
    }

    fun startWithoutAd() {
        rewardedAdState = 0
        rewardedAd = null
        keepOrSkipFiles(importMedia == true)
        importMedia = null
    }

    fun importChatFromFile(context: Context, file: Uri, premium: Boolean) {
        Log.d("ImportChatFromFile", "isPremium = $premium")
        this.isPremium = premium
        if (rewardedAd == null) {
            rewardedAdState = 0
            if (!isPremium) {
                loadRewardedAd(context, onLoaded = {
                    rewardedAdState = 1
                }, onFailed = {
                    rewardedAdState = -1
                })
            } else rewardedAdState = -1
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
                val lastMessage = importedMesssages.lastOrNull()
                val beginningMsgId = messageDao.getLastMessageId() ?: 0
                dao.addOrUpdateChat(
                    ChatEntity(
                        chatid = id.toInt(),
                        name = res.name ?: "New Chat ${id.toInt()}",
                        showReceiverName = res.users != null && res.users.size > 2,
                        senderId = res.senderId,
                        lastOpenedMsgId = if (beginningMsgId == 0) 0 else beginningMsgId + 1,
                        lastmsg = lastMessage?.message ?: "",
                        lastmsgtime = lastMessage?.date ?: "",
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

//            Log.i("vkpro", res.response)
        }
    }

    fun keepOrSkipFiles(keep: Boolean = true) {
        importState = IMPORTSTATE.ALMOSTCOMPLETED
        saveJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (keep) {
                    val filesRes = fileHelper.saveFiles(importedFileList)
                    val fileIDs = filesDao.addFiles(filesRes)
                    if (filesRes.isNotEmpty()) {
                        Log.i("vkpro", "inside modifying messages with dynamic filename matching")
                        try {
                            val messages = importedMesssages.toMutableList()
                            val sortedFiles = filesRes.sortedByDescending { it.displayname.length }

                            for (i in messages.indices) {
                                val msgText = messages[i].message ?: continue
                                for (file in sortedFiles) {
                                    if (msgText.contains(file.displayname, ignoreCase = true)) {
                                        val matchingIndex = filesRes.indexOf(file)
                                        val newMsg = msgText.lines().takeIf { it.size > 1 }?.drop(1)
                                            ?.joinToString("\n")
                                        messages[i] = messages[i].copy(
                                            fileId = fileIDs[matchingIndex].toInt(),
                                            message = newMsg
                                        )
                                        Log.i("vkpro", "Matched media file: ${file.displayname} -> message index $i")
                                        break
                                    }
                                }
                            }
                            importedMesssages = messages
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error matching files to messages: $e")
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

    fun toggleChatSelection(chatId: Int) {
        val currentSet = selectedChatIds

        selectedChatIds = if (currentSet.contains(chatId)) {
            currentSet - chatId
        } else {
            currentSet + chatId
        }
        isSelectionMode = selectedChatIds.isNotEmpty()
    }

    fun clearSelection() {
        selectedChatIds = emptySet()
        isSelectionMode = false
    }

    fun hideChats() {
        if (selectedChatIds.isEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.updateHiddenStateBulk(selectedChatIds, 1)
                clearSelection()
            }
        }
    }

    fun pinSelectedChats() {
        if (selectedChatIds.isEmpty()) return
        val targetIds = selectedChatIds.toSet()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentChats = chatsList.value.filter { it.chatid in targetIds }
                val allPinned = currentChats.isNotEmpty() && currentChats.all { it.isPinned }
                dao.updatePinnedStateBulk(targetIds, !allPinned)
                withContext(Dispatchers.Main) {
                    clearSelection()
                }
            }
        }
    }

    fun favoriteSelectedChats() {
        if (selectedChatIds.isEmpty()) return
        val targetIds = selectedChatIds.toSet()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentChats = chatsList.value.filter { it.chatid in targetIds }
                val allFavorite = currentChats.isNotEmpty() && currentChats.all { it.isFavorite }
                dao.updateFavoriteStateBulk(targetIds, !allFavorite)
                withContext(Dispatchers.Main) {
                    clearSelection()
                }
            }
        }
    }

    fun setClearChatsVisibility(visible: Boolean) {
        clearChatsVisible = visible
    }


    fun deleteChats() {
        if (selectedChatIds.isEmpty()) return
        val chatIdsToDelete = selectedChatIds.toSet()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. Delete associated media files on disk
                try {
                    val mediaFiles = filesDao.getFilesByChatIds(chatIdsToDelete)
                    for (file in mediaFiles) {
                        val diskFile = File(context.getExternalFilesDir(null), file.filename)
                        if (diskFile.exists()) {
                            diskFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error deleting media files from disk", e)
                }

                // 2. Delete profile icon files on disk
                try {
                    for (id in chatIdsToDelete) {
                        val iconFile = File(context.filesDir, "icons/icon$id.jpg")
                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error deleting chat icons from disk", e)
                }

                // 3. Delete database records for files, messages, and chats
                filesDao.deleteFilesBulk(chatIdsToDelete)
                messageDao.deleteMessagesBulk(chatIdsToDelete)
                dao.deleteChatsByIds(chatIdsToDelete)

                // 4. Reset selection mode
                withContext(Dispatchers.Main) {
                    clearSelection()
                }
            }
            setClearChatsVisibility(false)
        }
    }
}