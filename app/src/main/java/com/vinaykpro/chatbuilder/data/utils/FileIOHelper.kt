package com.vinaykpro.chatbuilder.data.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.vinaykpro.chatbuilder.data.local.DecodeResponse
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.FileEntity
import com.vinaykpro.chatbuilder.data.local.IMPORTRESULT
import com.vinaykpro.chatbuilder.data.local.ImportResponse
import com.vinaykpro.chatbuilder.data.local.MESSAGESTATUS
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import com.vinaykpro.chatbuilder.data.local.ZipItem
import com.vinaykpro.chatbuilder.data.local.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FileIOHelper {
    private val messagePattern = Regex(
        """^(\d{1,4})[./-](\d{1,4})[./-](\d{1,4}),\s*(\d{1,2})[:.](\d{1,2})\s*(.*?)\s*[-–]\s*(.+?):\s*(.*)$"""
    )
    private val systemMessagePattern = Regex(
        """^(\d{1,4})[./-](\d{1,4})[./-](\d{1,4}),\s*(\d{1,2})[:.](\d{1,2})\s*(.*?)\s*[-–]\s*(.*)$"""
    )
    private val context: Context

    constructor(context: Context) {
        this.context = context
    }

    private var fileUri: Uri? = null
    private var chatId: Int = 0
    fun init(id: Int, uri: Uri) {
        fileUri = uri
        chatId = id
    }

    suspend fun checkFile(): ImportResponse = withContext(
        Dispatchers.IO
    ) {
        var messages: List<MessageEntity>? = null
        val files: MutableList<ZipItem> = mutableListOf()
        var mediaIndexes: List<Int>? = null
        var chatName = "New Chat"
        var senderId: Int? = null

        if (fileUri == null) return@withContext ImportResponse(
            result = IMPORTRESULT.FAILURE,
            response = "File not found"
        )
        val filename = getFileName(context, fileUri!!) ?: ""
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri!!)
            if (inputStream != null) {
                val zipInputStream = ZipInputStream(inputStream)
                val firstEntry = zipInputStream.nextEntry
                var isChatFound = false
                if (firstEntry != null) {
                    var zipEntry: ZipEntry? = firstEntry
                    var zipIndex = 0
                    while (zipEntry != null) {
                        val fileName = zipEntry.name
                        if (!isChatFound && fileName.endsWith(".txt", ignoreCase = true)) {
                            try {
                                val betterFileName =
                                    maxOf(filename, fileName, compareBy { it.length })
                                val result = decodeChat(zipInputStream, betterFileName)
                                messages = result.messages
                                mediaIndexes = result.mediaIndexes
                                chatName = result.chatName
                                senderId = result.senderId
                                Log.i("vkpro", "import done")
                            } catch (e: Exception) {
                                Log.i("vkpro", e.toString())
                            }
                            isChatFound = messages != null
                        } else if (!fileName.endsWith(".vcf")) {
                            val fileType = getFileType(fileName, zipInputStream)
                            val fileSize = getZipItemSize(zipInputStream)

                            val item = ZipItem(
                                name = fileName,
                                index = zipIndex,
                                type = fileType,
                                byteCount = fileSize,
                                size = formatFileSize(fileSize),
                            )
                            Log.i(
                                "vkpro",
                                "${item.index}. ${item.name} - ${item.type} - ${item.byteCount} - ${item.size}"
                            )
                            files.add(item)
                        }
                        zipIndex++
                        zipEntry = zipInputStream.nextEntry
                        Log.i("vkpro", "Next entry: ${zipEntry?.name}")
                    }
                    zipInputStream.close()
                } else {
                    // If not a zip file pretend its a .txt
                    inputStream.close()
                    val txtInputStream = context.contentResolver.openInputStream(fileUri!!)
                    txtInputStream.use { stream ->
                        if (stream != null) {
                            val result = decodeChat(stream, filename)
                            messages = result.messages
                            chatName = result.chatName
                            senderId = result.senderId
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("vkpro", "Error : $e")
        }
        Log.i("vkpro", "Messages : ${messages?.size}")
        if (messages != null) {
            return@withContext ImportResponse(
                name = chatName,
                senderId = senderId,
                messages = messages,
                isMediaFound = files.isNotEmpty(),
                mediaItems = files,
                mediaIndexes = mediaIndexes
            )
        }
        return@withContext ImportResponse(
            result = IMPORTRESULT.FAILURE,
            response = "File invalid or corrupted"
        )
    }

    fun decodeChat(file: InputStream, fileName: String): DecodeResponse {
        val userMap = mutableMapOf<String, Int>() // For remembering users
        var nextUserId = 1
        val messageList: MutableList<MessageEntity> = mutableListOf()
        val mediaIndexes: MutableList<Int> = mutableListOf()
        val reader = BufferedReader(InputStreamReader(file))
        var line: String? = reader.readLine()
        var currMessage: MessageEntity? = null
        var invalidCount = 0
        var totalCount = 0
        var index = 0
        while (line != null) {
            if (totalCount == 50 && (invalidCount > 45)) return DecodeResponse(null, null, "", null)
            val isMessage = messagePattern.matchEntire(line)
            val isNote = if (isMessage == null) systemMessagePattern.matchEntire(line) else null
            totalCount++
            if (isMessage != null) {
                if (currMessage != null) {
                    messageList.add(currMessage); currMessage = null;
                    index++
                }
                val (d1, d2, d3, h, m, meridian, name, msg) = isMessage.destructured
                val userId = userMap.getOrPut(name) { val id = nextUserId; nextUserId++; id }
                currMessage = MessageEntity(
                    chatid = chatId,
                    messageType = MESSAGETYPE.MESSAGE,
                    userid = userId,
                    username = name,
                    message = msg,
                    date = "$d1/$d2/$d3",
                    time = "$h:$m $meridian",
                    timestamp = null,
                    fileId = null,
                    messageStatus = MESSAGESTATUS.SEEN,
                    replyMessageId = null
                )
                if (msg.contains("(file attached)")) mediaIndexes.add(index)
            } else if (isNote != null) {
                if (currMessage != null) {
                    messageList.add(currMessage); currMessage = null
                    index++
                }
                val (d1, d2, d3, h, m, meridian, msg) = isNote.destructured
                val note = MessageEntity(
                    chatid = chatId,
                    messageType = MESSAGETYPE.NOTE,
                    message = msg,
                    date = "$d1/$d2/$d3",
                    time = "$h:$m $meridian",
                    timestamp = null,
                    userid = null,
                    username = null,
                    fileId = null,
                    messageStatus = null,
                    isStarred = false,
                    isForwarded = false,
                    replyMessageId = null,
                )
                messageList.add(note)
                index++
            } else {
                invalidCount++
                if (currMessage != null) {
                    currMessage = currMessage.copy(message = currMessage.message + "\n" + line)
                } else {
                    //So when it cannot be decoded just showing raw content by note
                    val note = MessageEntity(
                        chatid = chatId,
                        messageType = MESSAGETYPE.NOTE,
                        message = line,
                        date = "02/01/2004",
                        time = "12:00 am",
                        timestamp = null,
                        userid = null,
                        username = null,
                        fileId = null,
                        messageStatus = null,
                        isStarred = false,
                        isForwarded = false,
                        replyMessageId = null,
                    )
                    messageList.add(note)
                    index++
                }
            }
            line = reader.readLine()?.trim()
            if (line == null && currMessage != null) {
                Log.i("vkpro", "caught last msg ${currMessage.message}")
                messageList.add(currMessage)
            }
        }
        var chatname = ""
        var senderId: Int? = null
        for ((name, id) in userMap) {
            if (fileName.contains(name) && name.length > chatname.length) {
                chatname = name
            } else {
                senderId = id
            }
        }
        if (chatname == "") chatname =
            fileName.replace(Regex("whatsapp chat with", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\.[a-zA-Z0-9]{1,5}$"), "")
                .trim()

        return DecodeResponse(
            messages = messageList,
            mediaIndexes = mediaIndexes,
            chatName = chatname,
            senderId = senderId
        )
    }

    suspend fun saveFiles(files: List<ZipItem>): List<FileEntity> = withContext(Dispatchers.IO) {
        Log.i("vkpro", "In save files method")
        val savedFilesData = mutableListOf<FileEntity>()
        if (fileUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri!!)
                if (inputStream != null) {
                    val zipInputStream = ZipInputStream(inputStream)
                    var zipEntry = zipInputStream.nextEntry
                    var zipIndex = 0
                    var fileIndex = 0
                    while (zipEntry != null) {
                        val currFile = files[fileIndex]
                        Log.i(
                            "vkpro",
                            "ZipIndex: {$zipIndex} , FileIndex: {$fileIndex}}"
                        )
                        if (currFile.index == zipIndex) fileIndex++
                        if (currFile.index == zipIndex && currFile.isSelected) {
                            val filename =
                                "${getUniqueName()}.${
                                    currFile.name
                                        .substringAfterLast('.', missingDelimiterValue = "")
                                        .takeIf { it.length in 1..5 } ?: ""
                                }"

                            val outputFile = File(context.getExternalFilesDir(null), filename)

                            try {
                                FileOutputStream(outputFile).use { outputStream ->
                                    val buffer = ByteArray(4096)
                                    var count: Int
                                    while (zipInputStream.read(buffer).also { count = it } != -1) {
                                        outputStream.write(buffer, 0, count)
                                    }
                                }
                                var thumbHeight: Int? = null
                                var thumbWidth: Int? = null
                                var duration: String = ""
                                if (currFile.type == FILETYPE.IMAGE) {
                                    val res = getImageDimensions(outputFile)
                                    thumbWidth = res.first
                                    thumbHeight = res.second
                                } else if (currFile.type == FILETYPE.VIDEO) {
                                    val res = getVideoDimensions(outputFile)
                                    thumbWidth = res.first
                                    thumbHeight = res.second
                                    duration = res.third
                                }
                                savedFilesData.add(
                                    FileEntity(
                                        chatid = chatId,
                                        displayname = currFile.name,
                                        filename = filename,
                                        type = currFile.type,
                                        thumbWidth = thumbWidth,
                                        thumbHeight = thumbHeight,
                                        size = currFile.size,
                                        duration = duration,
                                        lastModified = System.currentTimeMillis()
                                    )
                                )
                            } catch (e: Exception) {
                                Log.i("vkpro", "Errorrr saving file ${e.toString()}")
                            }

                            Log.i("vkpro", "saved file ${currFile.name} with $filename")
                        }
                        zipIndex++
                        zipEntry = zipInputStream.nextEntry
                    }
                    zipInputStream.close()
                }
            } catch (e: Exception) {
                Log.i("vkpro", "Error : $e")
            }
        }
        return@withContext savedFilesData
    }

    suspend fun getChatId(): Int {
        return chatId
    }

    fun getUniqueName(): String {
        val randomPart = (0..4)
            .map { ('a'..'z') + ('A'..'Z') }
            .map { it.random() }
            .joinToString("")

        return "file_${System.currentTimeMillis()}_$randomPart"
    }

    fun getZipItemSize(zis: ZipInputStream): Long {
        val buffer = ByteArray(4096)
        var total = 0L
        var bytesRead: Int

        while (zis.read(buffer).also { bytesRead = it } != -1) {
            total += bytesRead
        }

        return total
    }

    fun getFileType(name: String, zis: ZipInputStream): Int {
        val lower = name.lowercase()

        return when {
            lower.endsWith(".txt") -> FILETYPE.TEXT
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(
                ".webp"
            ) -> FILETYPE.IMAGE

            lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm") || lower.endsWith(
                ".3gp"
            ) -> FILETYPE.VIDEO

            lower.endsWith(".mp3") || lower.endsWith(".opus") || lower.endsWith(".ogg") || lower.endsWith(
                ".m4a"
            ) -> FILETYPE.AUDIO

            lower.endsWith(".zip") -> FILETYPE.ZIP
            lower.endsWith(".exe") || lower.endsWith(".apk") || lower.endsWith(".pdf") -> FILETYPE.FILE

            else -> {
                val piece = ByteArray(8)
                zis.read(piece)
                when {
                    piece.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte())) -> FILETYPE.IMAGE
                    piece.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)) -> FILETYPE.IMAGE
                    piece.startsWith(byteArrayOf(0x4F, 0x67, 0x67, 0x53)) -> FILETYPE.AUDIO
                    piece.startsWith(byteArrayOf(0x50, 0x4B)) -> FILETYPE.ZIP
                    piece.startsWith(byteArrayOf(0x00, 0x00, 0x00, 0x18)) -> FILETYPE.VIDEO
                    piece.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())) ||
                            piece.startsWith("From ".toByteArray()) -> FILETYPE.TEXT

                    else -> FILETYPE.FILE
                }
            }
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    fun getVideoDimensions(file: File): Triple<Int, Int, String> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            Triple(width, height, formatDuration(durationMs))
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(0, 0, "")
        } finally {
            retriever.release()
        }
    }

    fun getImageDimensions(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options.outWidth to options.outHeight
    }

    fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60

        return if (hours > 0)
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else
            String.format("%02d:%02d", minutes, seconds)
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (this.size < prefix.size) return false
        for (i in prefix.indices) {
            if (this[i] != prefix[i]) return false
        }
        return true
    }


}