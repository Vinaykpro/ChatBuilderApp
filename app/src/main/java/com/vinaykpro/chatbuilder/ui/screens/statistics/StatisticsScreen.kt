package com.vinaykpro.chatbuilder.ui.screens.statistics

import android.app.Application
import android.content.Intent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.utils.StatsExporter
import com.vinaykpro.chatbuilder.ui.components.BarGraphLayout
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.BigNativeAdView
import com.vinaykpro.chatbuilder.ui.components.ChartLayout
import com.vinaykpro.chatbuilder.ui.components.ElevatedCard
import com.vinaykpro.chatbuilder.ui.components.LongConvoItem
import com.vinaykpro.chatbuilder.ui.components.TopKeywordsLayout
import com.vinaykpro.chatbuilder.ui.components.UserWordsItem
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import java.io.File
import java.util.Locale

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun //SharedTransitionScope.
        StatisticsScreen(
    chatId: Int = 1,
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false,
    isPremium: Boolean = false,
) {
    val context = LocalContext.current

    val model: StatisticsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = chatId,
        refreshKey = 0,
        fallback = R.drawable.user
    )

    val adState1 by model.nativeAdSlot1.collectAsState()
    val adState2 by model.nativeAdSlot2.collectAsState()
    val adState3 by model.nativeAdSlot3.collectAsState()

    model.loadStats(context, chatId, !isDarkTheme)

    val statCards = listOf<Triple<Int, Int, String>>(
        Triple(R.drawable.ic_stat_msg, 0, "Messages"),
        Triple(R.drawable.ic_stat_date, 0, "Days Active"),
        Triple(R.drawable.ic_stat_streak, 0, "Streak"),
        Triple(R.drawable.ic_stat_media, 0, "Media"),
    )

    var expandedSection by rememberSaveable {
        mutableStateOf(ExpandedSection.NONE)
    }

//    LaunchedEffect(expandedSection) {
//        model.logMemory("After StatisticsScreen composed")
//    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            BasicToolbar(
                "Temp toolbar",
                onBackClick = {}
            )
        }
        if (model.isLoading) {
            item {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.Asset("file_scan.json")
                )
                Column(
                    modifier = Modifier
                        .fillParentMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(350.dp)
                    )

                    Text(
                        text = "Analyzing chat for statistics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 25.dp, bottom = 25.dp)
                    )
                }
            }
        } else {
            item {
                ElevatedCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = profilePicPainter,
                            contentDescription = "icon",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = model.chatDetails?.name ?: "User",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    lineHeight = 25.sp,
                                    fontWeight = FontWeight(500),
                                    color = if (!isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                )
                            )
                            Text(
                                text = "Statistics generated are for this chat only",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = if (!isDarkTheme) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray
                                )
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (c in statCards)
                        ElevatedCard(
                            modifier = Modifier
                                .weight(0.25f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .padding(bottom = 10.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                .1f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painterResource(c.first),
                                        contentDescription = "Stat type",
                                        modifier = Modifier
                                            .size(46.dp)
                                            .padding(12.dp),
                                        colorFilter = ColorFilter.tint(
                                            color = LightColorScheme.primary
                                        )
                                    )
                                }
                                Text(
                                    text = String.format(
                                        Locale.US, "%,d",
                                        if (c.third.toLowerCase()
                                                .contains("message")
                                        ) model.messageCount
                                        else if (c.third.toLowerCase()
                                                .contains("active")
                                        ) model.activeDays
                                        else if (c.third.toLowerCase()
                                                .contains("streak")
                                        ) model.streak
                                        else if (c.third.toLowerCase()
                                                .contains("media")
                                        ) model.mediaCount
                                        else "-"
                                    ),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight(500),
                                        color = if (!isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                    )
                                )
                                Text(
                                    text = c.third,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = if (!isDarkTheme) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Overall chat growth",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    ChartLayout(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .padding(start = 10.dp, end = 15.dp),
                        points = model.overallChatGrowth
                    )
                }
            }

            if (!isPremium) {
                item(contentType = "big_ad") {
                    ElevatedCard {
                        BigNativeAdView(
                            ad = adState1,
                            isDark = isDarkTheme,
                            onLoadAdRequested = {
                                model.loadAdForSlot1(context, "ca-app-pub-2813592783630195/8308318124")
                            }
                        )
                    }
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Messages on day of week",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    ChartLayout(
                        points = model.messageCountByWeekDay, modifier = Modifier.padding(18.dp)
                    )
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Messages per hour of day",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    ChartLayout(
                        points = model.messageCountByHour,
                        modifier = Modifier.padding(18.dp),
                        labels = listOf(
                            "12A", "2A", "4A", "6A", "8A", "10A",
                            "12P", "2P", "4P", "6P", "8P", "10P"
                        )
                    )
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Messages by user",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                    )
                    BarGraphLayout(
                        if (expandedSection != ExpandedSection.MESSAGES_BY_USER) model.messageCountByUser.take(
                            10
                        )
                        else model.messageCountByUser,
                        model.messageCount
                    )

                    if (model.messageCountByUser.size > 10)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (expandedSection == ExpandedSection.MESSAGES_BY_USER)
                                        expandedSection = ExpandedSection.NONE
                                    else
                                        expandedSection = ExpandedSection.MESSAGES_BY_USER
                                }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (expandedSection != ExpandedSection.MESSAGES_BY_USER) "View all" else "View less",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    else
                        Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Media by user",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    BarGraphLayout(
                        if (expandedSection != ExpandedSection.MEDIA_BY_USER) model.mediaCountByUser.take(
                            10
                        )
                        else model.mediaCountByUser,
                        model.mediaCount
                    )

                    if (model.messageCountByUser.size > 10)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (expandedSection == ExpandedSection.MEDIA_BY_USER)
                                        expandedSection = ExpandedSection.NONE
                                    else
                                        expandedSection = ExpandedSection.MEDIA_BY_USER
                                }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (expandedSection != ExpandedSection.MEDIA_BY_USER) "View all" else "View less",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    else
                        Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Emojis by user",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    BarGraphLayout(
                        if (expandedSection != ExpandedSection.EMOJIS_BY_USER) model.emojiCountByUser.take(
                            10
                        )
                        else model.emojiCountByUser,
                        model.emojiCount
                    )

                    if (model.messageCountByUser.size > 10)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (expandedSection == ExpandedSection.EMOJIS_BY_USER)
                                        expandedSection = ExpandedSection.NONE
                                    else
                                        expandedSection = ExpandedSection.EMOJIS_BY_USER
                                }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (expandedSection != ExpandedSection.EMOJIS_BY_USER) "View all" else "View less",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    else
                        Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Conversations started by user",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    BarGraphLayout(
                        if (expandedSection != ExpandedSection.CONV_BY_USER)
                            model.conversationStartsByUser.take(10)
                        else model.conversationStartsByUser,
                        model.totalConversations
                    )

                    if (model.conversationStartsByUser.size > 10)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (expandedSection == ExpandedSection.CONV_BY_USER)
                                        expandedSection = ExpandedSection.NONE
                                    else
                                        expandedSection = ExpandedSection.CONV_BY_USER
                                }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (expandedSection != ExpandedSection.CONV_BY_USER) "View all" else "View less",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    else
                        Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (!isPremium) {
                item(contentType = "big_ad") {
                    ElevatedCard {
                        BigNativeAdView(
                            ad = adState2,
                            isDark = isDarkTheme,
                            onLoadAdRequested = {
                                model.loadAdForSlot2(context, "ca-app-pub-2813592783630195/8308318124")
                            }
                        )
                    }
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Longest conversations",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    for ((index, conv) in model.longestConversatins.withIndex()) {
                        LongConvoItem(
                            title = "On ${conv.startDate}",
                            subtitle = conv.startedByUserName,
                            progress = conv.messageCount.toFloat() / model.longestConversatins[0].messageCount,
                            count = String.format(Locale.US, "%,d", conv.messageCount),
                            color = conv.color,
                            rank = index + 1,
                            onClick = {
                                navController.navigate("chat/$chatId?messageId=${conv.startMessageId}")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Peak activity days",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    for ((ind, day) in model.topDatesWithMessages.withIndex()) {
                        LongConvoItem(
                            title = day.date,
                            subtitle = "",
                            progress = day.messageCount.toFloat() / model.topDatesWithMessages[0].messageCount,
                            count = String.format(Locale.US, "%,d", day.messageCount),
                            color = day.color,
                            rank = ind + 1,
                            onClick = {
                                navController.navigate("chat/$chatId?messageId=${day.startMessageId}")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Most used words",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 10.dp, start = 16.dp, bottom = 12.dp)
                    )
                    TopKeywordsLayout(
                        keywords = model.mostUsedWords,
                        isDark = isDarkTheme
                    )
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Most used emojis",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 10.dp, start = 16.dp, bottom = 12.dp)
                    )
                    TopKeywordsLayout(
                        keywords = model.mostUsedEmojis,
                        isDark = isDarkTheme,
                        isEmoji = true,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            if (!isPremium) {
                item(contentType = "big_ad") {
                    ElevatedCard {
                        BigNativeAdView(
                            ad = adState3,
                            isDark = isDarkTheme,
                            onLoadAdRequested = {
                                model.loadAdForSlot3(context, "ca-app-pub-2813592783630195/8308318124")
                            }
                        )
                    }
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Most used emojis by user",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    for ((ind, user) in model.mostUsedEmojisByUser.withIndex())
                        if (expandedSection != ExpandedSection.EMOJIS_PER_USER && ind >= 10) break;
                        else
                            UserWordsItem(
                                rank = ind + 1,
                                name = user.first,
                                color = Color(0xFF1976D2),
                                pairs = user.second,
                                isDark = isDarkTheme,
                                isEmoji = true
                            )
                    if (model.conversationStartsByUser.size > 10)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (expandedSection == ExpandedSection.EMOJIS_PER_USER)
                                        expandedSection = ExpandedSection.NONE
                                    else
                                        expandedSection = ExpandedSection.EMOJIS_PER_USER
                                }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (expandedSection != ExpandedSection.EMOJIS_PER_USER) "View all" else "View less",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    else
                        Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                ElevatedCard {
                    Text(
                        text = "Most used words by user",
                        fontSize = 17.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                    )
                    for ((ind, user) in model.mostUsedWordsByUser.withIndex())
                        if (expandedSection != ExpandedSection.WORDS_PER_USER && ind >= 10) break;
                        else
                            UserWordsItem(
                                rank = ind + 1,
                                name = user.first,
                                color = Color(0xFF1976D2),
                                pairs = user.second,
                                isDark = isDarkTheme,
                                isEmoji = false
                            )
                    if (model.conversationStartsByUser.size > 10)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (expandedSection == ExpandedSection.WORDS_PER_USER)
                                        expandedSection = ExpandedSection.NONE
                                    else
                                        expandedSection = ExpandedSection.WORDS_PER_USER
                                }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (expandedSection != ExpandedSection.WORDS_PER_USER) "View all" else "View less",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    else
                        Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

    }

    BasicToolbar(
        "Statistics",
        icon1 = if (model.isLoading) null else painterResource(R.drawable.ic_export),
        onIcon1Click = {
            val html = StatsExporter.export(model)
            val file = File(
                context.externalCacheDir,
                "Chat Stats with ${model.chatDetails?.name ?: "User"} by ChatBuilder.html"
            )
            file.writeText(html)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(
                    intent,
                    "Share stats by ChatBuilder with"
                )
            )
        },
        onBackClick = {
            navController.popBackStack()
        })
}

enum class ExpandedSection {
    NONE,
    MESSAGES_BY_USER,
    MEDIA_BY_USER,
    EMOJIS_BY_USER,
    CONV_BY_USER,
    EMOJIS_PER_USER,
    WORDS_PER_USER,
}
