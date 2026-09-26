package com.vinaykpro.chatbuilder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun themeDao(): ThemeDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `chats` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `chats` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "localdb"
                )
                    .addMigrations(MIGRATION_1_2)
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
//
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL(
//                    """
//            CREATE TABLE IF NOT EXISTS `stats` (
//                `chatid` INTEGER NOT NULL,
//                `messageCount` INTEGER NOT NULL,
//                `streak` INTEGER NOT NULL,
//                `mediaCount` INTEGER NOT NULL,
//                `messageCountByDatePairs` TEXT NOT NULL,
//                `messageCountByWeekDay` TEXT NOT NULL,
//                `messageCountByHour` TEXT NOT NULL,
//                `userStatsList` TEXT NOT NULL,
//                `longestConversationsList` TEXT NOT NULL,
//                `topWords` TEXT NOT NULL,
//                `topEmojis` TEXT NOT NULL,
//                PRIMARY KEY(`chatid`)
//            )
//        """.trimIndent()
//                )
//            }
//        }
    }
}
