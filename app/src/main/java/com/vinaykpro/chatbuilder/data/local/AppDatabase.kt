package com.vinaykpro.chatbuilder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vinaykpro.chatbuilder.data.local.dao.ChatDao
import com.vinaykpro.chatbuilder.data.local.dao.FileDao
import com.vinaykpro.chatbuilder.data.local.dao.MessageDao
import com.vinaykpro.chatbuilder.data.local.dao.ThemeDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(
    entities = [ThemeEntity::class, ChatEntity::class, MessageEntity::class, FileEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun themeDao(): ThemeDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "localdb"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val json = Json {
                                    encodeDefaults = true
                                }
                                INSTANCE?.themeDao()?.insertTheme(
                                    ThemeEntity(
                                        headerstyle = json.encodeToString(HeaderStyle()),
                                        bodystyle = json.encodeToString(BodyStyle()),
                                        messagebarstyle = json.encodeToString(MessageBarStyle())
                                    )
                                )
                                INSTANCE?.themeDao()?.insertTheme(
                                    ThemeEntity(
                                        name = "Theme 2", appcolor = "#FF017F6C",
                                        headerstyle = json.encodeToString(HeaderStyle(color_navbar = "#FF017F6C")),
                                        bodystyle = json.encodeToString(BodyStyle(color_senderbubble = "#FFE1FFC7")),
                                        messagebarstyle = json.encodeToString(
                                            MessageBarStyle(
                                                color_outerbutton = "#FF017F6C",
                                                color_rightinnerbutton = "#FF017F6C",
                                                color_outerbutton_dark = "#FF017F6C",
                                                color_rightinnerbutton_dark = "#FF017F6C"
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }).build().also { INSTANCE = it }
            }
        }
    }
}
