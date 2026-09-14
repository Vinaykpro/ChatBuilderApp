package com.vinaykpro.chatbuilder.data.local

import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.text.BreakIterator
import java.util.Locale
import kotlin.random.Random

@Immutable
data class ImportResponse(
    val result: Int = IMPORTRESULT.SUCCESS,
    val response: String = "Import successful",
    val name: String? = null,
    val senderId: Int? = null,
    val messages: List<MessageEntity>? = null,
    val users: List<UserInfo>? = null,
    val isMediaFound: Boolean = false,
    val mediaItems: List<ZipItem>? = null,
    val mediaIndexes: List<Int>? = null,
    val widgetLen: Int? = null,
)

@Immutable
data class DecodeResponse(
    val messages: List<MessageEntity>?,
    val users: List<UserInfo>? = null,
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

@Immutable
data class BarGraphItem(
    val name: String,
    val color: Color,
    val count: Int
)

data class ChartPoint(
    val label: String,
    val count: Int
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
    const val SUCCESS = 6
}

object FILETYPE {
    const val FILE = 0
    const val TEXT = 1
    const val IMAGE = 2
    const val VIDEO = 3
    const val AUDIO = 4
    const val ZIP = 5
}

object MyConstants {
    val homeMenuList = listOf(
        "Themes",
        "Hidden chats",
        "Animate a chat",
        "Rate this app",
        "Settings",
        "Help"
    )
    val appUrl = "https://play.google.com/store/apps/details?id=com.vinaykpro.chatbuilder"
    val chatMenuList = listOf(
        "Edit profile",
        "Search",
        "Theme",
        "View statistics",
        "Swap sender",
        "Go to Date",
        "Animate Chat",
        "Export to PDF/HTML",
        "Hide/Unhide chat",
        "Clear chat"
    )
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

fun Int.formatShort(): String {
    return when {
        this >= 1_000_000 -> {
            val formatted =
                String.format(Locale.ENGLISH, "%.1fM", this / 1_000_000.0).replace(".0M", "M")
            if (formatted.length > 4) "${this / 1_000_000}M" else formatted
        }

        this >= 100_000 -> {
            val formatted =
                String.format(Locale.ENGLISH, "%.1fL", this / 100_000.0).replace(".0L", "L")
            if (formatted.length > 4) "${this / 100_000}L" else formatted
        }

        this >= 1_000 -> {
            val formatted =
                String.format(Locale.ENGLISH, "%.1fK", this / 1_000.0).replace(".0K", "K")
            if (formatted.length > 4) "${this / 1_000}K" else formatted
        }

        else -> toString()
    }
}

val Boolean.toInt get() = if (this) 1 else 0

fun generateRandomDarkColor(dark: Boolean): Color {

    val hue = Random.nextFloat() * 360f

    val saturation = if (dark)
        0.6f + Random.nextFloat() * 0.3f      // 60% - 90%
    else
        0.45f + Random.nextFloat() * 0.25f    // 45% - 70%

    val lightness = if (dark)
        0.25f + Random.nextFloat() * 0.15f    // 25% - 40%
    else
        0.60f + Random.nextFloat() * 0.20f    // 60% - 80%

    return Color.hsl(hue, saturation, lightness)
}

object EmojiUtils {

    fun graphemeClusters(text: String): List<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val iterator = BreakIterator.getCharacterInstance()
            iterator.setText(text)

            val result = mutableListOf<String>()

            var start = iterator.first()
            var end = iterator.next()

            while (end != BreakIterator.DONE) {
                result += text.substring(start, end)
                start = end
                end = iterator.next()
            }

            return result
        }

        // API 21-23 fallback
        val result = mutableListOf<String>()
        var i = 0

        while (i < text.length) {
            val cp = Character.codePointAt(text, i)
            val sb = StringBuilder()
            sb.appendCodePoint(cp)
            i += Character.charCount(cp)

            // Join following ZWJ sequence if present
            while (i < text.length) {

                if (text[i] != '\u200D')
                    break

                sb.append('\u200D')
                i++

                if (i >= text.length)
                    break

                val next = Character.codePointAt(text, i)
                sb.appendCodePoint(next)
                i += Character.charCount(next)
            }

            result += sb.toString()
        }

        return result
    }

    fun isEmoji(cluster: String): Boolean {

        val cps = cluster.codePointsCompat()

        return cps.any { cp ->

            cp in 0x1F000..0x1FAFF ||      // Most emoji
                    cp in 0x2600..0x26FF ||
                    cp in 0x2700..0x27BF ||
                    cp in 0x2300..0x23FF ||
                    cp in 0x2B00..0x2BFF ||
                    cp in 0x2900..0x297F ||
                    cp in 0x1F1E6..0x1F1FF ||      // Flags
                    cp == 0x00A9 ||               // ©
                    cp == 0x00AE ||               // ®
                    cp == 0x203C ||               // ‼
                    cp == 0x2049 ||               // ⁉
                    cp == 0x2122 ||               // ™
                    cp == 0x2139 ||               // ℹ
                    cp == 0x3030 ||               // 〰
                    cp == 0x303D ||               // 〽
                    cp == 0x3297 ||               // 🉗
                    cp == 0x3299                  // 🉙
        }
                || cluster.contains('\uFE0F')      // emoji presentation
                || cluster.contains('\u200D')      // ZWJ sequence
                || cps.any { it in 0x1F3FB..0x1F3FF } // skin tones
    }

    fun extract(text: String): List<String> =
        graphemeClusters(text).filter(::isEmoji)

    private fun String.codePointsCompat(): List<Int> {
        val list = ArrayList<Int>()
        var i = 0
        while (i < length) {
            val cp = Character.codePointAt(this, i)
            list += cp
            i += Character.charCount(cp)
        }
        return list
    }
}