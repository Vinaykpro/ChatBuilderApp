package com.vinaykpro.chatbuilder.data.local

import androidx.compose.runtime.Immutable

@Immutable
data class ImportResponse(
    val result: Int = IMPORTRESULT.SUCCESS,
    val response: String = "Import successful",
    val name: String? = null,
    val senderId: Int? = null,
    val messages: List<MessageEntity>? = null,
    val isMediaFound: Boolean = false,
    val mediaItems: List<ZipItem>? = null,
    val mediaIndexes: List<Int>? = null,
    val widgetLen: Int? = null,
)

@Immutable
data class DecodeResponse(
    val messages: List<MessageEntity>?,
    val mediaIndexes: List<Int>?,
    val chatName: String,
    val senderId: Int?
)

@Immutable
data class ZipItem(
    val name: String,
    val index: Int,
    val type: Int,
    val byteCount: Long,
    val size: String,
    val isSelected: Boolean = true
)

object IMPORTRESULT {
    const val SUCCESS = 0
    const val FAILURE = 1
}

object IMPORTSTATE {
    const val NONE = 0
    const val STARTED = 1
    const val MEDIASELECTION = 2
    const val UNSUPPORTEDFILE = 3
    const val ALMOSTCOMPLETED = 4
    const val WATCHAD = 5
}

object FILETYPE {
    const val FILE = 0
    const val TEXT = 1
    const val IMAGE = 2
    const val VIDEO = 3
    const val AUDIO = 4
    const val ZIP = 5
}

fun formatFileSize(bytes: Long): String {
    if (bytes < 0) return "Unknown"
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        bytes < kb -> "${bytes}B"
        bytes < mb -> "%.2f KB".format(bytes / kb)
        bytes < gb -> "%.2f MB".format(bytes / mb)
        else -> "%.2f GB".format(bytes / gb)
    }
}