package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatNote
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.serialization.json.Json
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class)
//@Preview(showBackground = true)
@Composable
fun SharedTransitionScope.ChatScreen(
    chatId: Int = 1,
    isDarkTheme: Boolean = false,
    navController: NavHostController = rememberNavController(),
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val theme = LocalThemeEntity.current

    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(context.applicationContext as Application, chatId)
    )
    val screenWidthForMedia =
        min(LocalConfiguration.current.screenWidthDp, LocalConfiguration.current.screenHeightDp)

    val headerStyle = remember(theme.headerstyle) {
        try {
            Json.decodeFromString<HeaderStyle>(theme.headerstyle)
        } catch (e: Exception) {
            HeaderStyle()
        }
    }

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

    val messageBarStyle = remember(theme.messagebarstyle) {
        try {
            Json.decodeFromString<MessageBarStyle>(theme.messagebarstyle)
        } catch (_: Exception) {
            MessageBarStyle()
        }
    }
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }
    val listState = rememberLazyListState()
    val chatDetails by chatViewModel.chatDetails.collectAsState()
    val filesMap by chatViewModel.files.collectAsState()
    val messages by chatViewModel.messages.collectAsState(initial = emptyList())
    LaunchedEffect(chatDetails?.lastOpenedMsgId) {
        chatDetails?.let { chatViewModel.initialLoad(it.lastOpenedMsgId) }
    }
    LaunchedEffect(messages.size, chatDetails?.lastOpenedMsgId) {
        if (chatViewModel.isInitialScroll) {
            val index = messages.indexOfFirst { it.messageId == chatDetails?.lastOpenedMsgId }
            if (index >= 0) {
                listState.scrollToItem(index)
                chatViewModel.isInitialScroll = false
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.totalItemsCount }
            .collect { (firstIndex, totalCount) ->
                if (firstIndex < 10) {
                    chatViewModel.loadPrevPage()
                }

                if (totalCount - firstIndex < 25) {
                    chatViewModel.loadNextPage()
                }
            }
    }


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
            )
    ) {
        ChatToolbar(
            name = chatDetails?.name ?: "Chat $chatId",
            status = chatDetails?.status ?: "Tap to edit",
            style = headerStyle,
            isDarkTheme = isDarkTheme
        )

        //body
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 5.dp)
        ) {
            itemsIndexed(messages, key = { _, m -> m.messageId }) { i, m ->
                when {
                    m.messageType == MESSAGETYPE.NOTE -> ChatNote(
                        m.message.toString(),
                        color = themeBodyColors.dateBubble,
                        textColor = themeBodyColors.textSecondary
                    )

                    m.userid == (chatDetails?.senderId ?: 1) -> SenderMessage(
                        text = m.message,
                        sentTime = m.time.toString(),
                        bubbleStyle = 1,
                        bubbleRadius = bodyStyle.bubble_radius,
                        bubbleTipRadius = bodyStyle.bubble_tip_radius,
                        isFirst = i == 0 || messages[i - 1].userid != m.userid,
                        color = themeBodyColors.senderBubble,
                        textColor = themeBodyColors.textPrimary,
                        textColorSecondary = themeBodyColors.textSecondary,
                        file = filesMap[m.fileId],
                        screenWidth = screenWidthForMedia,
                        imageLoader = imageLoader,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onMediaClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "mediaMetaMap",
                                filesMap
                            )
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "mediaMessages",
                                chatViewModel.mediaMessages
                            )
                            navController.navigate("mediapreview/$it")
                        }
                    )

                    else -> Message(
                        text = m.message,
                        sentTime = m.time.toString(),
                        bubbleStyle = 1,
                        bubbleRadius = bodyStyle.bubble_radius,
                        bubbleTipRadius = bodyStyle.bubble_tip_radius,
                        isFirst = i == 0 || messages[i - 1].userid != m.userid,
                        color = themeBodyColors.receiverBubble,
                        textColor = themeBodyColors.textPrimary,
                        textColorSecondary = themeBodyColors.textSecondary,
                        file = filesMap[m.fileId],
                        screenWidth = screenWidthForMedia,
                        imageLoader = imageLoader,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onMediaClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "mediaMetaMap",
                                filesMap
                            )
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "mediaMessages",
                                chatViewModel.mediaMessages
                            )
                            navController.navigate("mediapreview/$it")
                        }
                    )
                }
            }
        }

        //input
        ChatMessageBar(style = messageBarStyle, isDarkTheme = isDarkTheme)
    }
    DisposableEffect(Unit) {
        onDispose {
            val index = listState.firstVisibleItemIndex
            val msgId = messages.getOrNull(index)?.messageId
            if (msgId != null) {
                chatViewModel.saveScrollPosition(msgId)
            }
        }
    }
}


fun BodyStyle.toParsed(isDarkTheme: Boolean): ParsedBodyStyle {
    fun parse(hex: String): Color = Color(hex.toColorInt())

    return ParsedBodyStyle(
        chatBackground = parse(if (isDarkTheme) color_chatbackground_dark else color_chatbackground),
        senderBubble = parse(if (isDarkTheme) color_senderbubble_dark else color_senderbubble),
        receiverBubble = parse(if (isDarkTheme) color_receiverbubble_dark else color_receiverbubble),
        dateBubble = parse(if (isDarkTheme) color_datebubble_dark else color_datebubble),
        textPrimary = parse(if (isDarkTheme) color_text_primary_dark else color_text_primary),
        textSecondary = parse(if (isDarkTheme) color_text_secondary_dark else color_text_secondary)
    )
}

data class ParsedBodyStyle(
    val chatBackground: Color,
    val senderBubble: Color,
    val receiverBubble: Color,
    val dateBubble: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)