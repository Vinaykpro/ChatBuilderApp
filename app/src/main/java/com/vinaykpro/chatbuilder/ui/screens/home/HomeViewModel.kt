package com.vinaykpro.chatbuilder.ui.screens.home

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.IMPORTRESULT
import com.vinaykpro.chatbuilder.data.local.IMPORTSTATE
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import com.vinaykpro.chatbuilder.data.local.ZipItem
import com.vinaykpro.chatbuilder.data.utils.FileIOHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

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

    fun importChatFromFile(file: Uri) {
        importJob = viewModelScope.launch {
            val id = dao.addOrUpdateChat(
                ChatEntity(
                    name = "New Chat",
                    lastmsg = "Import in progress"
                )
            )
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
                    messageDao.insertMessages(messages = importedMesssages)
                    importState = IMPORTSTATE.SUCCESS
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
                importState = IMPORTSTATE.SUCCESS
            } else {
                importState = IMPORTSTATE.SUCCESS
            }
            messageDao.insertMessages(messages = importedMesssages)
        }
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
        importState = IMPORTSTATE.NONE
        importJob?.cancel()
        saveJob?.cancel()
        importedMesssages = emptyList()
        importedFileList = emptyList()
        mediaIndexes = emptyList()
    }
}