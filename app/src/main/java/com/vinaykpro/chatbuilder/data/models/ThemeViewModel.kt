package com.vinaykpro.chatbuilder.data.models

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.ExportedTheme
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.themeDao()

    private val _selectedThemeId = MutableStateFlow(1)
    val selectedThemeId: StateFlow<Int> = _selectedThemeId

    @OptIn(ExperimentalCoroutinesApi::class)
    val themeEntity: StateFlow<ThemeEntity?> = _selectedThemeId
        .flatMapLatest { id -> dao.getThemeByIdFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val themes: StateFlow<List<ThemeEntity>> = dao.getAllThemes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val json = Json { encodeDefaults = true }

    fun trySavedIdSwitch(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (dao.isThemeAvailable(id)) _selectedThemeId.value = id
            }
        }
    }

    fun changeTheme(id: Int) {
        _selectedThemeId.value = id
    }

    fun updateTheme(theme: ThemeEntity) {
        viewModelScope.launch {
            dao.insertTheme(theme)
        }
    }

    fun addBlankTheme() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertTheme(
                    ThemeEntity(
                        name = "New theme",
                        headerstyle = json.encodeToString(HeaderStyle()),
                        bodystyle = json.encodeToString(BodyStyle()),
                        messagebarstyle = json.encodeToString(MessageBarStyle())
                    )
                )
            }
        }
    }

    suspend fun saveCustomIcon(
        uri: Uri,
        context: Context,
        themeId: Int,
        filename: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val themeDir = File(context.filesDir, "theme$themeId")
                if (!themeDir.exists()) themeDir.mkdirs()
                val inputStream = context.contentResolver.openInputStream(uri)
                val iconFile = File(themeDir, filename)

                inputStream?.use { input ->
                    FileOutputStream(iconFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("SaveIcon", "Failed to save icon", e)
            }
            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun exportTheme(context: Context, onDone: (File) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val fileName =
                    if (themeEntity.value!!.name.contains("theme", ignoreCase = true))
                        "CB ${themeEntity.value?.name}.zip"
                    else "CB ${themeEntity.value?.name} theme.zip"

                val zipFile =
                    File(
                        context.externalCacheDir,
                        fileName
                    )
                val icons = listOf(
                    "icon.png",
                    "ic_back.png",
                    "ic_profile.png",
                    "ic_3dots.png",
                    "ic_nav1.png",
                    "ic_nav2.png",
                    "ic_nav3.png",

                    "ic_ticks_seen.png",

                    "ic_outer_icon.png",
                    "ic_left_inner_icon.png",
                    "ic_right_inner_icon.png",
                    "ic_bottom_nav1.png",
                    "ic_bottom_nav2.png",
                    "ic_bottom_nav3.png"
                )
                ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                    themeEntity.value.let {
                        val themeFile = File(context.cacheDir, "theme.vkpro")
                        val theme = themeEntity.value!!
                        val exported = ExportedTheme(
                            name = theme.name,
                            author = theme.author,
                            appcolor = theme.appcolor,
                            appcolordark = theme.appcolordark,
                            headerstyle = json.decodeFromString(theme.headerstyle),
                            bodystyle = json.decodeFromString(theme.bodystyle),
                            messagebarstyle = json.decodeFromString(theme.messagebarstyle)
                        )
                        themeFile.writeText(json.encodeToString<ExportedTheme>(exported))
                        val entry = ZipEntry("theme.vkpro")
                        zos.putNextEntry(entry)
                        FileInputStream(themeFile).use { fis ->
                            fis.copyTo(zos)
                        }
                        zos.closeEntry()

                        val themeDir = File(context.filesDir, "theme${theme.id}")
                        if (themeDir.exists()) {
                            icons.forEach { name ->
                                val iconFile = File(themeDir, name)
                                if (iconFile.exists()) {
                                    val entry = ZipEntry(name)
                                    zos.putNextEntry(entry)
                                    FileInputStream(iconFile).use { fis ->
                                        fis.copyTo(zos)
                                    }
                                    zos.closeEntry()
                                }
                            }
                        }
                    }
                }
                onDone(zipFile)
            }
        }
    }

    fun deleteTheme(context: Context, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                themeEntity.value.let {
                    val id = themeEntity.value!!.id
                    val themeDir = File(context.filesDir, "theme$id")
                    if (themeDir.exists()) {
                        themeDir.deleteRecursively()
                    }
                    _selectedThemeId.value = 1
                    dao.deleteTheme(id)
                }
            }
            onDone()
        }
    }

    fun importTheme(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val zipInputStream = ZipInputStream(inputStream)
                        var entry = zipInputStream.nextEntry
                        val bufferedEntries = mutableListOf<Pair<String, ByteArray>>()
                        var themeId: Int? = null
                        while (entry != null) {
                            val name = entry.name
                            Log.i("vkpro", "THEME_IMPORT: At file - $name")

                            val data = zipInputStream.readBytes()

                            if (name.endsWith(".vkpro")) {
                                val theme =
                                    json.decodeFromString<ExportedTheme>(data.toString(Charsets.UTF_8))
                                themeId = dao.insertTheme(
                                    ThemeEntity(
                                        name = theme.name,
                                        author = theme.author,
                                        appcolor = theme.appcolor,
                                        appcolordark = theme.appcolordark,
                                        headerstyle = json.encodeToString(theme.headerstyle),
                                        bodystyle = json.encodeToString(theme.bodystyle),
                                        messagebarstyle = json.encodeToString(theme.messagebarstyle)
                                    )
                                ).toInt()
                            } else {
                                bufferedEntries.add(name to data)
                            }
                            zipInputStream.closeEntry()
                            entry = zipInputStream.nextEntry
                        }

                        if (themeId != null) {
                            val themeDir = File(context.filesDir, "theme$themeId")
                            themeDir.mkdirs()

                            for ((name, bytes) in bufferedEntries) {
                                val file = File(themeDir, name)
                                file.parentFile?.mkdirs()
                                file.writeBytes(bytes)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Invalid theme file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Toast.makeText(context, "Theme successfully imported", Toast.LENGTH_SHORT).show()
        }
    }
}
