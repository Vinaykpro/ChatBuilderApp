package com.vinaykpro.chatbuilder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val chatid: Int = 0,
    val name: String = "Vinaykpro",
    val status: String = "online",
    val senderId: Int? = null,
    val lastOpenedMsgId: Int? = null,
    val lastmsg: String = "",
    val hidden: Int = 0, // 0 -> false
    val lastmsgtime: String = "",
    val lastopened: Long = System.currentTimeMillis(),
)