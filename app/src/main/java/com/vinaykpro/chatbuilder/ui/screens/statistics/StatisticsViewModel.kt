package com.vinaykpro.chatbuilder.ui.screens.statistics

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinaykpro.chatbuilder.data.local.AppDatabase
import com.vinaykpro.chatbuilder.data.local.BarGraphItem
import com.vinaykpro.chatbuilder.data.local.ChatEntity
import com.vinaykpro.chatbuilder.data.local.EmojiUtils
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageEntity
//import com.vinaykpro.chatbuilder.data.local.StatEntity
import com.vinaykpro.chatbuilder.data.local.generateRandomDarkColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    //    private val dao = AppDatabase.getInstance(application).statsDao()
    private val chatDao = AppDatabase.getInstance(application).chatDao()
    private val messageDao = AppDatabase.getInstance(application).messageDao()

    var chatStatistics: ChatEntity? = null //StatEntity? = null
    var chatDetails: ChatEntity? = null

    private var messages: List<MessageEntity> = emptyList()

    var isLoading by mutableStateOf(true)
        private set

    var selectedChat by mutableStateOf<ChatEntity?>(null)

    private var isInitial = true

    var topDatesWithMessages: List<PeakDay> = emptyList()

    var overallChatGrowth: List<Pair<String, Int>> = emptyList()
    var messageCountByWeekDay: List<Pair<String, Int>> = emptyList()
    var messageCountByHour: List<Pair<String, Int>> = emptyList()

    //        var userStatsInfo: List<UserStatsInfo> = emptyList()
    var messageCountByUser: List<BarGraphItem> = emptyList()
    var mediaCountByUser: List<BarGraphItem> = emptyList()
    var emojiCountByUser: List<BarGraphItem> = emptyList()

    var conversationStartsByUser: List<BarGraphItem> = emptyList()
    var totalConversations: Int = 0

    var longestConversatins: List<ConversationInfo> = emptyList()

    var mostUsedWords: List<Pair<String, Int>> = emptyList()
    var mostUsedEmojis: List<Pair<String, Int>> = emptyList()

    var mostUsedEmojisByUser: List<Pair<String, List<Pair<String, Int>>>> = emptyList()
    var mostUsedWordsByUser: List<Pair<String, List<Pair<String, Int>>>> = emptyList()

    var messageCount = 0
    var activeDays = 0
    var currStreak = 0
    var streak = 0
    var mediaCount = 0
    var emojiCount = 0
    var deletedCount = 0

    fun loadStats(context: Context, chatId: Int, isDark: Boolean) {
        if (!isInitial) return
        isInitial = false
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
//                chatStatistics = dao.getStatsEntityById(chatId)
                chatDetails = chatDao.getChatEntityById(chatId)

                if (chatStatistics == null) {
                    messages = messageDao.getAllMessages(chatId)

                    Log.d("Message count", "-------COUNTTTTT ($chatId) -------")
                    Log.d("Message count", "${messages.size}")

                    val dateFormat = detectDateFormat(messages)

                    if (messages.isEmpty() || dateFormat == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Insufficient/invalid data! Please try again or with another chat",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@withContext
                        }
                    }

                    Log.d("statdebug", "------------- date format ------------")
                    Log.d("statdebug", dateFormat.toString())
                    if (dateFormat != null)
                        Log.d(
                            "statdebug",
                            "dateFormat: ${dateFormat.dayIndex}/${dateFormat.monthIndex}/${dateFormat.yearIndex}"
                        )

                    var messageCountByDateMap: MutableMap<String, Int> = mutableMapOf()
                    var messageIdByDate: MutableMap<String, Int> = mutableMapOf()

                    var messageCountByWeekDayArr = IntArray(7)

                    var messageCountByHourOfDay = IntArray(24)

                    var userStatsMap: MutableMap<Int, UserStatsTemp> = mutableMapOf()

                    val conversations = mutableListOf<ConversationInfo>()

                    val wordCounts = mutableMapOf<String, Int>()
                    val emojiCounts = mutableMapOf<String, Int>()

                    var prevDate: String? = null
                    var prevDay: Calendar? = null

                    var currentConversation: ConversationInfo? = null
                    var prevDateTime: Calendar? = null

                    for (m in messages) {

                        val currDate = m.date
                        val time = m.time

                        if (m.messageType == MESSAGETYPE.MESSAGE) {
                            messageCount++

                            if (currDate != null) // messageCount on date
                                if (messageCountByDateMap.contains(currDate)) {
                                    messageCountByDateMap.put(
                                        currDate,
                                        (messageCountByDateMap[currDate] ?: 0) + 1
                                    )
                                } else {
                                    messageCountByDateMap.put(currDate, 1)
                                    messageIdByDate.put(currDate, m.messageId)
                                }

                            if (!time.isNullOrBlank()) { // messageCount by hour of day
                                try {
                                    val normalized = time.trim().lowercase()
                                    val separator = if (':' in normalized) ':' else '.'
                                    val hourPart = normalized.substringBefore(separator).trim()
                                    var hour = hourPart.toInt()
                                    when {
                                        "am" in normalized -> if (hour == 12) hour = 0
                                        "pm" in normalized -> if (hour != 12) hour += 12
                                    }
                                    if (hour in 0..23)
                                        messageCountByHourOfDay[hour]++
                                } catch (_: Exception) {
                                }
                            }

                            // Individual stats for count
                            if (m.userid != null) {
                                val stats = userStatsMap[m.userid] ?: UserStatsTemp(
                                    name = m.username ?: "User ${m.userid}"
                                )
                                stats.messageCount++
                                if (m.fileId != null ||
                                    (m.message ?: "").toLowerCase().contains("media omitted") ||
                                    (m.message ?: "").toLowerCase().contains("file attached")
                                ) stats.mediaCount++
                                userStatsMap.put(m.userid, stats)
                            }

                            val tokens = (m.message ?: "").split(Regex("\\s+"))

                            // count each word
                            for (token in tokens) {
                                if (token.isEmpty()) continue
                                // Clean punctuation and lowercase the word
                                val cleanWord =
                                    token.lowercase().replace(Regex("[^a-zA-Z0-9]"), "")
                                if (cleanWord.isNotEmpty()) {
                                    wordCounts[cleanWord] = (wordCounts[cleanWord] ?: 0) + 1
                                    // Individual stats for words
                                    if (m.userid != null) {
                                        val stats = userStatsMap[m.userid] ?: UserStatsTemp(
                                            name = m.username ?: "User ${m.userid}"
                                        )
                                        stats.topWords[cleanWord] =
                                            (stats.topWords[cleanWord] ?: 0) + 1
                                        userStatsMap.put(m.userid, stats)
                                    }
                                }
                            }

                            val emojis = EmojiUtils.extract(m.message ?: "")
                            if (emojis.isNotEmpty()) {
                                emojiCount += emojis.size
                                for (e in emojis) {
                                    emojiCounts[e] = (emojiCounts[e] ?: 0) + 1

                                    // Individual stats for emojis
                                    if (m.userid != null) {
                                        val stats = userStatsMap[m.userid] ?: UserStatsTemp(
                                            name = m.username ?: "User ${m.userid}"
                                        )
                                        stats.emojiCount++
                                        stats.topEmojis[e] = (stats.topEmojis[e] ?: 0) + 1
                                        userStatsMap.put(m.userid, stats)
                                    }
                                }
                            }
                        }

                        if (m.fileId != null ||
                            (m.message ?: "").toLowerCase().contains("media omitted") ||
                            (m.message ?: "").toLowerCase().contains("file attached") ||
                            (m.message ?: "").toLowerCase().contains("arquivo anexado") ||
                            (m.message ?: "").toLowerCase().contains("mídia omitida")
                        ) {
                            mediaCount++
                        }

                        if (m.fileId != null ||
                            (m.message ?: "").toLowerCase().contains("this message was deleted") ||
                            (m.message ?: "").toLowerCase().contains("you deleted this message")
                        ) {
                            deletedCount++
                        }

                        // date checks
                        if (dateFormat != null && currDate != null) {
                            try {
                                val parts = currDate.split(dateFormat.separator)

                                var year = parts[dateFormat.yearIndex].toInt()

                                if (year < 100)
                                    year += if (year >= 70) 1900 else 2000

                                val today = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, parts[dateFormat.monthIndex].toInt() - 1)
                                    set(Calendar.DAY_OF_MONTH, parts[dateFormat.dayIndex].toInt())
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                if (m.messageType == MESSAGETYPE.MESSAGE) { // Count per weekday msgs
                                    messageCountByWeekDayArr[today.get(Calendar.DAY_OF_WEEK) - 1]++
                                }

                                if (prevDay != null) {
                                    val diffDays =
                                        (today.timeInMillis - prevDay.timeInMillis) / 86_400_000L
                                    when (diffDays) {
                                        0L -> {}
                                        1L -> {
                                            currStreak++
                                        }

                                        else -> {
                                            currStreak = 1
                                        }
                                    }
                                    streak = maxOf(streak, currStreak)
                                } else {
                                    currStreak = 1
                                    streak = 1
                                }
                                prevDate = currDate
                                prevDay = today
                            } catch (_: Exception) {
                            }
                        }

                        if (dateFormat != null && currDate != null && time != null && m.messageType == MESSAGETYPE.MESSAGE) {
                            // ---------- Longest conversation tracking ----------

                            try {
                                val parts = currDate.split(dateFormat.separator)

                                var year = parts[dateFormat.yearIndex].toInt()

                                if (dateFormat.yearDigits == 2)
                                    year += if (year >= 70) 1900 else 2000

                                val normalized = time.trim().lowercase()
                                val separator = if (':' in normalized) ':' else '.'
                                var hour = normalized.substringBefore(separator).trim().toInt()
                                var minutePart = normalized.substringAfter(separator).trim()
                                var minute =
                                    if (minutePart.length > 1)
                                        minutePart.substring(0, 2).trim().toInt()
                                    else minutePart.trim().toInt()
                                when {
                                    "am" in normalized -> if (hour == 12) hour = 0
                                    "pm" in normalized -> if (hour != 12) hour += 12
                                }
                                if (hour in 0..23 && minute in 0..59) {
                                    // All fine for message to calculate for date & time
                                    val todayWithTime = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(
                                            Calendar.MONTH,
                                            parts[dateFormat.monthIndex].toInt() - 1
                                        )
                                        set(
                                            Calendar.DAY_OF_MONTH,
                                            parts[dateFormat.dayIndex].toInt()
                                        )
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    if (prevDateTime == null) {
                                        currentConversation = ConversationInfo(
                                            startMessageId = m.messageId,
                                            startedByUserName = m.username ?: "User ${m.userid}",
                                            startDate = formatCustomDate(
                                                dateStr = m.date,
                                                separator = dateFormat.separator.toString(),
                                                dayIdx = dateFormat.dayIndex,
                                                monthIdx = dateFormat.monthIndex,
                                                yearIdx = dateFormat.yearIndex,
                                            ),
                                            startTime = m.time,
                                            messageCount = 1,
                                            color = generateRandomDarkColor(isDark)
                                        )
                                        if (m.userid != null) {
                                            val stats = userStatsMap[m.userid] ?: UserStatsTemp(
                                                name = m.username ?: "User ${m.userid}"
                                            )
                                            totalConversations++
                                            stats.convStartCount++
                                            userStatsMap.put(m.userid, stats)
                                        }
                                    } else {
                                        val diffMinutes =
                                            (todayWithTime.timeInMillis - prevDateTime.timeInMillis) / 60000L
                                        if (diffMinutes < 61) {
                                            currentConversation?.messageCount++
                                        } else {
                                            currentConversation?.let {
                                                if (it.messageCount > 1) {
                                                    conversations.add(it)
                                                }
                                            }
                                            currentConversation = ConversationInfo(
                                                startMessageId = m.messageId,
                                                startedByUserName = m.username
                                                    ?: "User ${m.userid}",
                                                startDate = formatCustomDate(
                                                    dateStr = m.date,
                                                    separator = dateFormat.separator.toString(),
                                                    dayIdx = dateFormat.dayIndex,
                                                    monthIdx = dateFormat.monthIndex,
                                                    yearIdx = dateFormat.yearIndex,
                                                ),
                                                startTime = m.time,
                                                messageCount = 1,
                                                color = generateRandomDarkColor(isDark)
                                            )
                                            if (m.userid != null) {
                                                val stats = userStatsMap[m.userid] ?: UserStatsTemp(
                                                    name = m.username ?: "User ${m.userid}"
                                                )
                                                totalConversations++
                                                stats.convStartCount++
                                                userStatsMap.put(m.userid, stats)
                                            }
                                        }
                                    }
                                    prevDateTime = todayWithTime
                                }
                            } catch (e: Error) {
                            }
                        }
                    }

                    Log.d("statdebug", "------------ Message count by date -----------")
                    for (m in messageCountByDateMap) {
                        Log.d("statdebug", "date: ${m.key} , count: ${m.value}")
                    }

                    Log.d("statdebug", "------------ Message count by weekday -----------")
                    for (m in messageCountByWeekDayArr) {
                        Log.d("statdebug", "day --- value = $m")
                    }

                    Log.d("statdebug", "------------ Message count by time -----------")

                    for (m in messageCountByHourOfDay) {
                        Log.d("statdebug", "day --- value = $m")
                    }

                    activeDays = messageCountByDateMap.size

                    topDatesWithMessages = messageCountByDateMap.entries
                        .sortedByDescending { it.value }
                        .take(5)
                        .map {
                            PeakDay(
                                startMessageId = messageIdByDate[it.key] ?: 0,
                                date = formatCustomDate(
                                    dateStr = it.key,
                                    separator = (dateFormat?.separator ?: '/').toString(),
                                    dayIdx = dateFormat?.dayIndex ?: 0,
                                    monthIdx = dateFormat?.monthIndex ?: 1,
                                    yearIdx = dateFormat?.yearIndex ?: 2,
                                ),
                                messageCount = it.value,
                                color = generateRandomDarkColor(isDark)
                            )
                        }

                    messageCountByWeekDay = listOf(
                        Pair("Mon", messageCountByWeekDayArr[1]),
                        Pair("Tue", messageCountByWeekDayArr[2]),
                        Pair("Wed", messageCountByWeekDayArr[3]),
                        Pair("Thu", messageCountByWeekDayArr[4]),
                        Pair("Fri", messageCountByWeekDayArr[5]),
                        Pair("Sat", messageCountByWeekDayArr[6]),
                        Pair("Sun", messageCountByWeekDayArr[0]),
                    )
                    messageCountByHour = listOf(
                        Pair("12AM - 1AM", messageCountByHourOfDay[0]),
                        Pair("1AM - 2AM", messageCountByHourOfDay[1]),
                        Pair("2AM - 3AM", messageCountByHourOfDay[2]),
                        Pair("3AM - 4AM", messageCountByHourOfDay[3]),
                        Pair("4AM - 5AM", messageCountByHourOfDay[4]),
                        Pair("5AM - 6AM", messageCountByHourOfDay[5]),
                        Pair("6AM - 7AM", messageCountByHourOfDay[6]),
                        Pair("7AM - 8AM", messageCountByHourOfDay[7]),
                        Pair("8AM - 9AM", messageCountByHourOfDay[8]),
                        Pair("9AM - 10AM", messageCountByHourOfDay[9]),
                        Pair("10AM - 11AM", messageCountByHourOfDay[10]),
                        Pair("11AM - 12PM", messageCountByHourOfDay[11]),
                        Pair("12PM - 1PM", messageCountByHourOfDay[12]),
                        Pair("1PM - 2PM", messageCountByHourOfDay[13]),
                        Pair("2PM - 3PM", messageCountByHourOfDay[14]),
                        Pair("3PM - 4PM", messageCountByHourOfDay[15]),
                        Pair("4PM - 5PM", messageCountByHourOfDay[16]),
                        Pair("5PM - 6PM", messageCountByHourOfDay[17]),
                        Pair("6PM - 7PM", messageCountByHourOfDay[18]),
                        Pair("7PM - 8PM", messageCountByHourOfDay[19]),
                        Pair("8PM - 9PM", messageCountByHourOfDay[20]),
                        Pair("9PM - 10PM", messageCountByHourOfDay[21]),
                        Pair("10PM - 11PM", messageCountByHourOfDay[22]),
                        Pair("11PM - 12AM", messageCountByHourOfDay[23])
                    )

                    messageCountByUser = userStatsMap.entries
                        .sortedByDescending { it.value.messageCount }
                        .map {
                            BarGraphItem(
                                it.value.name,
                                generateRandomDarkColor(isDark),
                                it.value.messageCount
                            )
                        }

                    mediaCountByUser = userStatsMap.entries
                        .sortedByDescending { it.value.mediaCount }
                        .map {
                            BarGraphItem(
                                it.value.name,
                                generateRandomDarkColor(isDark),
                                it.value.mediaCount
                            )
                        }

                    emojiCountByUser = userStatsMap.entries
                        .sortedByDescending { it.value.emojiCount }
                        .map {
                            BarGraphItem(
                                it.value.name,
                                generateRandomDarkColor(isDark),
                                it.value.emojiCount
                            )
                        }

                    conversationStartsByUser = userStatsMap.entries
                        .sortedByDescending { it.value.convStartCount }
                        .map {
                            BarGraphItem(
                                it.value.name,
                                generateRandomDarkColor(isDark),
                                it.value.convStartCount
                            )
                        }

                    longestConversatins = conversations
                        .sortedByDescending { it.messageCount }
                        .take(5)

                    mostUsedWords = wordCounts.entries
                        .filter {
                            it.key != "media" && it.key != "omitted" &&
                                    it.key != "deleted" && it.key != "message" &&
                                    it.key != "this" && it.key != "was" &&
                                    it.key.length >= 3
                        }
                        .sortedByDescending { it.value }
                        .take(30)
                        .map {
                            Pair(it.key, it.value)
                        }

                    mostUsedEmojis = emojiCounts.entries
                        .sortedByDescending { it.value }
                        .take(20)
                        .map {
                            Pair(it.key, it.value)
                        }

                    mostUsedEmojisByUser = userStatsMap.entries
                        .sortedByDescending { it.value.emojiCount }
                        .map {
                            Pair(
                                it.value.name,
                                it.value.topEmojis.entries
                                    .sortedByDescending { it.value }
                                    .take(10)
                                    .map {
                                        Pair(it.key, it.value)
                                    }
                            )
                        }

                    mostUsedWordsByUser = userStatsMap.entries
                        .sortedByDescending { it.value.emojiCount }
                        .map {
                            Pair(
                                it.value.name,
                                it.value.topWords.entries
                                    .filter {
                                        it.key != "media" && it.key != "omitted" &&
                                                it.key != "deleted" && it.key != "message" &&
                                                it.key != "this" && it.key != "was" &&
                                                it.key.length >= 3
                                    }
                                    .sortedByDescending { it.value }
                                    .take(10)
                                    .map {
                                        Pair(it.key, it.value)
                                    }
                            )
                        }

                    overallChatGrowth = groupMessageCounts(
                        data = messageCountByDateMap,
                        separator = (dateFormat?.separator ?: '/').toString(),
                        dayIndex = dateFormat?.dayIndex ?: 0,
                        monthIndex = dateFormat?.monthIndex ?: 1,
                        yearIndex = dateFormat?.yearIndex ?: 2
                    )
                }
                isLoading = false
            }
        }
    }

    fun detectDateFormat(messages: List<MessageEntity>): DateFormatInfo? {

        val separator = run {
            Log.d("DATE", "First date = ${messages.firstOrNull()?.date}")
            Log.d("DATE", "First non-null = ${messages.firstNotNullOfOrNull { it.date }}")

            val first = messages.firstNotNullOfOrNull { it.date }
                ?: return null

            listOf('/', '-', '.').firstOrNull { first.contains(it) }
                ?: return null
        }

        val rows = mutableListOf<Triple<List<Int>, String, String>>()

        for (m in messages) {

            val date = m.date ?: continue

            val split = date.split(separator)

            if (split.size != 3)
                continue

            val nums = split.mapNotNull { it.toIntOrNull() }

            if (nums.size != 3)
                continue

            rows += Triple(nums, date, m.time ?: "")
        }

        Log.d("DATE", "Rows = ${rows.size}")

        if (rows.isEmpty())
            return null

        // ---------------- YEAR DETECTION ----------------

        var yearIndex = -1
        var yearDigits = 2

        for (i in 0..2) {
            if (rows.any { it.first[i] > 31 }) {
                yearIndex = i
                yearDigits = 4
                break
            }
        }

        if (yearIndex == -1) {

            val distinct = IntArray(3)

            for (i in 0..2)
                distinct[i] = rows.map { it.first[i] }.distinct().size

            yearIndex = distinct.indices.minBy { distinct[it] }
        }

        val remain = (0..2).filter { it != yearIndex }

        val candidates = listOf(
            DateFormatInfo(separator, remain[0], remain[1], yearIndex, yearDigits), // DMY
            DateFormatInfo(separator, remain[1], remain[0], yearIndex, yearDigits)  // MDY
        )

        var best: DateFormatInfo? = null
        var bestScore = Int.MIN_VALUE

        for (candidate in candidates) {

            var score = 0
            var previousMillis: Long? = null

            for ((nums, _, _) in rows) {

                try {

                    var year = nums[candidate.yearIndex]

                    if (candidate.yearDigits == 2)
                        year += if (year >= 70) 1900 else 2000

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, nums[candidate.monthIndex] - 1)
                        set(Calendar.DAY_OF_MONTH, nums[candidate.dayIndex])
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val currentMillis = calendar.timeInMillis

                    if (previousMillis != null) {

                        val diff = (currentMillis - previousMillis!!) / 86_400_000L

                        when {
                            diff == 0L -> score += 2
                            diff in 1..7 -> score += 6
                            diff in 8..31 -> score += 4
                            diff in 32..365 -> score += 2
                            diff > 365 -> score -= 5
                            diff < 0 -> score -= 20
                        }
                    }
                    previousMillis = currentMillis
                } catch (_: Exception) {
                    score -= 100
                }
            }

            if (score > bestScore) {
                bestScore = score
                best = candidate
            }
        }

        // If both are equally likely, default to DD/MM
        return best ?: DateFormatInfo(
            separator,
            remain[0],
            remain[1],
            yearIndex,
            yearDigits
        )
    }

    fun groupMessageCounts(
        data: Map<String, Int>,
        separator: String,
        dayIndex: Int,
        monthIndex: Int,
        yearIndex: Int
    ): List<Pair<String, Int>> {

        if (data.isEmpty()) return emptyList()

        fun parse(date: String): LocalDate {
            val p = date.split(separator)

            val day = p[dayIndex].trim().toInt()
            val month = p[monthIndex].trim().toInt()

            var year = p[yearIndex].trim()
            if (year.length == 2)
                year = "20$year"

            return LocalDate.of(year.toInt(), month, day)
        }

        val sorted = data.map { parse(it.key) to it.value }
            .sortedBy { it.first }

        val start = sorted.first().first
        val end = sorted.last().first

        val totalMonths =
            (end.year - start.year) * 12 +
                    (end.monthValue - start.monthValue) + 1

        val totalDays =
            ChronoUnit.DAYS.between(start, end).toInt() + 1

        val mode = when {
            totalDays <= 31 -> Mode.DAYS3
            totalMonths <= 3 -> Mode.DAYS15
            totalMonths <= 12 -> Mode.MONTH1
            totalMonths <= 24 -> Mode.MONTH2
            totalMonths <= 36 -> Mode.MONTH3
            totalMonths <= 60 -> Mode.MONTH6
            else -> Mode.MONTH12
        }

        val result = linkedMapOf<String, Int>()

        when (mode) {

            Mode.DAYS3,
            Mode.DAYS15 -> {

                val size = when {
                    totalDays <= 7 -> 1
                    mode == Mode.DAYS3 -> 3
                    else -> maxOf(
                        1,
                        kotlin.math.ceil(totalDays / 12.0).toInt()
                    )
                }

                val buckets = linkedMapOf<LocalDate, Int>()

                sorted.forEach { (date, count) ->

                    val startDay =
                        ((date.dayOfMonth - 1) / size) * size + 1

                    val bucketStart =
                        LocalDate.of(date.year, date.month, startDay)

                    buckets[bucketStart] =
                        (buckets[bucketStart] ?: 0) + count
                }

                buckets.forEach { (bucket, count) ->

                    val endDay = minOf(
                        bucket.dayOfMonth + size - 1,
                        YearMonth.of(bucket.year, bucket.month)
                            .lengthOfMonth()
                    )


                    val label =
                        if (size == 1)
                            "${bucket.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} " +
                                    "${bucket.dayOfMonth} ${bucket.year}"
                        else
                            "${bucket.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} " +
                                    "${bucket.dayOfMonth} - $endDay ${bucket.year}"

                    result[label] = count
                }
            }

            else -> {

                val monthsPerGroup = when (mode) {
                    Mode.MONTH1 -> 1
                    Mode.MONTH2 -> 2
                    Mode.MONTH3 -> 3
                    Mode.MONTH6 -> 6
                    Mode.MONTH12 -> 12
                    else -> 1
                }

                val buckets = linkedMapOf<Pair<Int, Int>, Int>()

                sorted.forEach { (date, count) ->

                    val absoluteMonth = date.year * 12 + (date.monthValue - 1)

                    val group = absoluteMonth / monthsPerGroup

                    buckets[group to monthsPerGroup] =
                        (buckets[group to monthsPerGroup] ?: 0) + count
                }

                buckets.forEach { (key, count) ->

                    val group = key.first
                    val size = key.second

                    val startAbsolute = group * size
                    val startYear = startAbsolute / 12
                    val startMonth = startAbsolute % 12 + 1

                    val endAbsolute = startAbsolute + size - 1
                    val endYear = endAbsolute / 12
                    val endMonth = endAbsolute % 12 + 1

                    val label =
                        if (size == 1) {

                            "${
                                Month.of(startMonth)
                                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            } $startYear"

                        } else {

                            val first =
                                Month.of(startMonth)
                                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

                            val last =
                                Month.of(endMonth)
                                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

                            if (startYear == endYear)
                                "$first - $last $startYear"
                            else
                                "$first $startYear - $last $endYear"
                        }

                    result[label] = count
                }
            }
        }

        return result.entries.map { it.key to it.value }
    }

    fun formatCustomDate(
        dateStr: String,
        separator: String,
        dayIdx: Int,
        monthIdx: Int,
        yearIdx: Int
    ): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        return try {
            val parts = dateStr.split(separator)

            // Extract raw components based on provided indexes
            val dayInt = parts[dayIdx].toInt()
            val monthInt = parts[monthIdx].toInt()
            val yearInt = parts[yearIdx].toInt()

            // Validate calendar ranges
            if (dayInt !in 1..31 || monthInt !in 1..12 || yearInt < 0) {
                return dateStr
            }

            // Get the appropriate suffix (st, nd, rd, th)
            val suffix = when {
                dayInt in 11..13 -> "th" // Special case exceptions
                dayInt % 10 == 1 -> "st"
                dayInt % 10 == 2 -> "nd"
                dayInt % 10 == 3 -> "rd"
                else -> "th"
            }

            // Output format example: "1st Oct 2026"
            "$dayInt$suffix ${months[monthInt - 1]} $yearInt"
        } catch (e: Exception) {
            // Returns original string on format errors or array out of bounds
            dateStr
        }
    }
}

enum class Mode {
    DAYS3,
    DAYS15,
    MONTH1,
    MONTH2,
    MONTH3,
    MONTH6,
    MONTH12
}

data class DateFormatInfo(
    val separator: Char,
    val dayIndex: Int,
    val monthIndex: Int,
    val yearIndex: Int,
    val yearDigits: Int
)

data class UserStatsTemp(
    var name: String = "Other user",
    var messageCount: Int = 0,
    var mediaCount: Int = 0,
    var emojiCount: Int = 0,
    var topEmojis: MutableMap<String, Int> = mutableMapOf<String, Int>(),
    var topWords: MutableMap<String, Int> = mutableMapOf<String, Int>(),
    var convStartCount: Int = 0,
    var deletedCount: Int = 0
)

//data class UserStatsInfo(
//    val name: String = "Other user",
//    val messageCount: Int = 0,
//    val mediaCount: Int = 0,
//    val emojiCount: Int = 0,
//    val topEmojis: List<Pair<String, Int>> = emptyList(),
//    val topWords: List<Pair<String, Int>> = emptyList(),
//    val convStartCount: Int = 0,
//    val deletedCount: Int = 0
//)

data class ConversationInfo(
    val startMessageId: Int,
    val startedByUserName: String,
    val startDate: String,
    val startTime: String,
    var messageCount: Int = 1,
    val color: Color
)

data class PeakDay(
    val startMessageId: Int,
    val date: String,
    var messageCount: Int = 1,
    val color: Color
)