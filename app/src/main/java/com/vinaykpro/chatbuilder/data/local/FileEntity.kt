package com.vinaykpro.chatbuilder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val fileid: Int = 0,
    val chatid: Int? = null,
    val displayname: String = "Display name.jpg",
    val filename: String = "file_id.jpg",
    val type: Int = FILETYPE.FILE,
    val thumbHeight: Int? = null,
    val thumbWidth: Int? = null,
    val size: String = "",
    val duration: String = "",
    val lastModified: Long = System.currentTimeMillis()
)