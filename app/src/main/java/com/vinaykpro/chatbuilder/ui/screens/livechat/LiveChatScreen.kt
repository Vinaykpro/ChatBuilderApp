package com.vinaykpro.chatbuilder.ui.screens.livechat

import android.app.Application
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.data.models.ChatMediaViewModel
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatNote
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.screens.chat.HeaderIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.MessageBarIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.getHeaderIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.getMessageBarIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.toParsed
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.serialization.json.Json
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LiveChatScreen(
    chatId: Int,
    rangeStart: Int,
    rangeEnd: Int,
    isTypingEnabled: Boolean,
    isInputTypingEnabled: Boolean,
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false,
    chatMediaViewModel: ChatMediaViewModel
) {
    val context = LocalContext.current
    val theme = LocalThemeEntity.current

    val headerStyle = remember(theme.headerstyle) {
        try {
            Json.decodeFromString<HeaderStyle>(theme.headerstyle)
        } catch (e: Exception) {
            HeaderStyle()
        }
    }

    val headerIcons: HeaderIcons = getHeaderIcons(theme.id)

    val bodyStyle = remember(theme.bodystyle) {
        try {
            Json.decodeFromString<BodyStyle>(theme.bodystyle)
        } catch (_: Exception) {
            BodyStyle()
        }
    }

    val themeBodyColors = remember(bodyStyle, isDarkTheme) {
        bodyStyle.toParsed(isDarkTheme)
    }

    val blueTicksIcon =
        rememberCustomIconPainter(theme.id, "ic_ticks_seen.png", 0, R.drawable.doubleticks)

    val messageBarStyle = remember(theme.messagebarstyle) {
        try {
            Json.decodeFromString<MessageBarStyle>(theme.messagebarstyle)
        } catch (_: Exception) {
            MessageBarStyle()
        }
    }

    val messageBarIcons: MessageBarIcons = getMessageBarIcons(theme.id)

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp - 15.dp
    val screenWidthForMedia =
        min(LocalConfiguration.current.screenWidthDp, LocalConfiguration.current.screenHeightDp)

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    val model: LiveChatViewModel = viewModel(
        factory = LiveChatViewModelFactory(context.applicationContext as Application, chatId)
    )

    model.initialLoad(
        rangeStart,
        rangeEnd,
        isTypingEnabled,
        isInputTypingEnabled
    )

    val listState = rememberLazyListState()
    val messages by model.messages.collectAsState(initial = emptyList())
    val isLoading by model.isLoading.collectAsState(initial = true)
    val isTyping by model.isTyping.collectAsState(initial = false)

    var currentUserId by remember { mutableIntStateOf(model.chatDetails?.senderId ?: -1) }

    LaunchedEffect(model.chatDetails?.senderId) {
        currentUserId = model.chatDetails?.senderId ?: -1
    }

    LaunchedEffect(model.currMsg) {
        if (messages.isNotEmpty())
            listState.animateScrollToItem(index = messages.size - 1)
    }

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = model.chatDetails?.chatid,
        refreshKey = 0,
        fallback = R.drawable.user
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBodyColors.chatBackground)
            .padding(
                bottom = WindowInsets.ime
                    .only(WindowInsetsSides.Bottom)
                    .exclude(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChatToolbar(
            name = model.chatDetails?.name ?: "Chat ${model.chatDetails?.chatid}",
            status = if (isTyping) "typing..." else "online",
            backIcon = headerIcons.backIcon,
            profileIcon = profilePicPainter,
            icon1 = headerIcons.icon1,
            icon2 = headerIcons.icon2,
            icon3 = headerIcons.icon3,
            icon4 = headerIcons.icon4,
            style = headerStyle,
            isDarkTheme = isDarkTheme,
            onBackClick = {
                navController.popBackStack()
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 5.dp)
                    .alpha(if (isLoading) 0f else 1f)
            ) {
                itemsIndexed(messages, key = { _, m -> m.messageId }) { i, m ->
                    if (model.currMsg >= m.messageId)
                        when {
                            m.messageType == MESSAGETYPE.NOTE -> ChatNote(
                                m.message.toString(),
                                color = themeBodyColors.dateBubble,
                                textColor = themeBodyColors.textSecondary
                            )

                            m.userid == (currentUserId) -> SenderMessage(
                                text = m.message,
                                sentTime = m.time.toString(),
                                date = if (i == 0 || messages[i - 1].date != m.date) {
                                    {
                                        ChatNote(
                                            m.date.toString(),
                                            color = themeBodyColors.dateBubble,
                                            textColor = themeBodyColors.textSecondary
                                        )
                                    }
                                } else null,
                                ticksIcon = blueTicksIcon,
                                bubbleStyle = bodyStyle.bubble_style,
                                bubbleRadius = bodyStyle.bubble_radius,
                                bubbleTipRadius = bodyStyle.bubble_tip_radius,
                                showTime = bodyStyle.show_time,
                                showTicks = bodyStyle.showticks,
                                isFirst = (i == 0 || messages[i - 1].userid != m.userid || messages[i - 1].date != m.date),
                                color = themeBodyColors.senderBubble,
                                textColor = themeBodyColors.textPrimary,
                                textColorSecondary = themeBodyColors.textSecondary,
                                file = chatMediaViewModel.mediaMap[m.fileId],
                                screenWidthDp = screenWidthDp,
                                screenWidth = screenWidthForMedia,
                                imageLoader = imageLoader,
                                onMediaClick = {},
                                onCopy = {},
                                onClick = {}
                            )

                            else -> Message(
                                text = m.message,
                                senderName = null,
                                sentTime = m.time.toString(),
                                senderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                date = if (i == 0 || messages[i - 1].date != m.date) {
                                    {
                                        ChatNote(
                                            m.date.toString(),
                                            color = themeBodyColors.dateBubble,
                                            textColor = themeBodyColors.textSecondary
                                        )
                                    }
                                } else null,
                                bubbleStyle = bodyStyle.bubble_style,
                                bubbleRadius = bodyStyle.bubble_radius,
                                bubbleTipRadius = bodyStyle.bubble_tip_radius,
                                showTime = bodyStyle.show_time,
                                isFirst = (i == 0 || messages[i - 1].userid != m.userid || messages[i - 1].date != m.date),
                                color = themeBodyColors.receiverBubble,
                                textColor = themeBodyColors.textPrimary,
                                textColorSecondary = themeBodyColors.textSecondary,
                                file = chatMediaViewModel.mediaMap[m.fileId],
                                screenWidthDp = screenWidthDp,
                                screenWidth = screenWidthForMedia,
                                imageLoader = imageLoader,
                                onMediaClick = {},
                                onCopy = {},
                                onClick = {}
                            )
                        }
                }
            }
        }

        ChatMessageBar(
            style = messageBarStyle,
            value = model.currMsgInputBar,
            isDarkTheme = isDarkTheme,
            readOnly = true,
            outerIcon = messageBarIcons.outerIcon,
            leftInnerIcon = messageBarIcons.leftInnerIcon,
            rightInnerIcon = messageBarIcons.rightInnerIcon,
            icon1 = messageBarIcons.icon1,
            icon2 = messageBarIcons.icon2,
            icon3 = messageBarIcons.icon3
        )
    }
}