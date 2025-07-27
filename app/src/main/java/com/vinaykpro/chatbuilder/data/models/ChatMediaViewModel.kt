package com.vinaykpro.chatbuilder.data.models

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.FileEntity
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatMediaViewModel(application: Application) : AndroidViewModel(application) {
    private val messageDao = AppDatabase.getInstance(application).messageDao()
    private val mediaDao = AppDatabase.getInstance(application).fileDao()
    private val chatDao = AppDatabase.getInstance(application).chatDao()

    @SuppressLint("StaticFieldLeak")
    val context = application.applicationContext

    var allMediaMessages: List<MessageEntity> = emptyList()
    var mediaMap: Map<Int, FileEntity> = emptyMap()
    var previewMediaMessages: List<MessageEntity> = emptyList()
    var currentChat: ChatEntity? = null

    fun load(chatid: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                currentChat = chatDao.getChatEntityById(chatid)
                allMediaMessages = messageDao.getAllMediaMessages(chatid)
                mediaMap = mediaDao.getFilesByChatId(chatid).associateBy { m -> m.fileid }
                val types = setOf(FILETYPE.IMAGE, FILETYPE.VIDEO)
                previewMediaMessages = allMediaMessages.filter { msg ->
                    mediaMap[msg.fileId]?.type in types
                }
            }
        }
    }

    fun updateChat(chat: ChatEntity) {
        viewModelScope.launch {
            chatDao.addOrUpdateChat(chat)
        }
    }

    fun clearChat(chatId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Deleting media first
                for (f in mediaMap) {
                    Log.i("vkpro", "Deleting ${f.value.filename}")
                    val file = File(context.getExternalFilesDir(null), f.value.filename)
                    file.delete()
                }
                val iconFile = File(context.filesDir, "icons/icon$chatId.jpg")
                iconFile.delete()
                messageDao.deleteMessages(chatId = chatId)
                chatDao.deleteChatById(chatId)
            }
        }
    }
}