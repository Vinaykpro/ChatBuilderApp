package com.vinaykpro.chatbuilder.data.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.FileEntity
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatMediaViewModel(application: Application) : AndroidViewModel(application) {
    private val messageDao = AppDatabase.getInstance(application).messageDao()
    private val mediaDao = AppDatabase.getInstance(application).fileDao()

    var allMediaMessages: List<MessageEntity> = emptyList()
    var mediaMap: Map<Int, FileEntity> = emptyMap()
    var previewMediaMessages: List<MessageEntity> = emptyList()

    fun load(chatid: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allMediaMessages = messageDao.getAllMediaMessages(chatid)
                mediaMap = mediaDao.getFilesByChatId(chatid).associateBy { m -> m.fileid }
                val types = setOf(FILETYPE.IMAGE, FILETYPE.VIDEO)
                previewMediaMessages = allMediaMessages.filter { msg ->
                    mediaMap[msg.fileId]?.type in types
                }
            }
        }
    }
}