package com.vinaykpro.chatbuilder.data.local

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Stable
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val messageId: Int = 0,
    val chatid: Int,
    val messageType: Int = MESSAGETYPE.NOTE,
    val userid: Int?,
    val username: String?,
    val message: String?,
    val date: String?,
    val time: String?,
    val timestamp: Long?,
    val fileId: Int?,
    val messageStatus: Int?,
    val isStarred: Boolean = false,
    val isForwarded: Boolean = false,
    val replyMessageId: Int?,
)

object MESSAGETYPE {
    const val MESSAGE = 0
    const val NOTE = 1
}

object MESSAGESTATUS {
    const val SEEN = 0
    const val DELIVERED = 1
    const val SENT = 2
    const val WAITING = 3
}
