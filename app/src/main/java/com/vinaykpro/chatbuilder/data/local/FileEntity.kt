package com.vinaykpro.chatbuilder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val fileid: Int = 0,
    val chatid: Int?,
    val displayname: String = "Display name.jpg",
    val filename: String = "file_id.jpg",
    val type: Int = FILETYPE.FILE,
    val thumbHeight: Int? = null,
    val thumbWidth: Int? = null,
    val size: String,
    val lastModified: Long
)