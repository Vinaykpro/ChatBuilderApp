package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
import com.vinaykpro.chatbuilder.ui.components.SearchBar
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.Json
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class)
//@Preview(showBackground = true)
@Composable
fun SharedTransitionScope.ChatScreen(
    chatId: Int = 1,
    isDarkTheme: Boolean = false,
    navController: NavHostController = rememberNavController(),
    animatedVisibilityScope: AnimatedVisibilityScope,
    chatMediaViewModel: ChatMediaViewModel,
) {
    val theme = LocalThemeEntity.current

    val context = LocalContext.current
    val model: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(context.applicationContext as Application, chatId)
    )
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthForMedia =
        min(LocalConfiguration.current.screenWidthDp, LocalConfiguration.current.screenHeightDp)

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

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = chatMediaViewModel.currentChat?.chatid ?: 0,
        refreshKey = 0,
        fallback = headerIcons.profileIcon
    )

    val listState = rememberLazyListState()
    val chatDetails by model.chatDetails.collectAsState()
    val messages by model.messages.collectAsState(initial = emptyList())
    LaunchedEffect(chatDetails?.lastOpenedMsgId) {
        delay(300)
        chatDetails?.let { model.initialLoad(it.lastOpenedMsgId) }
        chatMediaViewModel.load(chatDetails?.chatid ?: -1)
    }
    LaunchedEffect(messages) {
        Log.i("vkpro", "Tried scrolling to id;" + chatDetails?.lastOpenedMsgId)
        if (model.needScroll && chatDetails?.lastOpenedMsgId != null) {
            model.needScroll = false
            val index = if (model.searchedResults.isNotEmpty()) {
                messages.indexOfFirst {
                    it.messageId == model.searchedResults[model.currentSearchIndex]
                }
            } else {
                messages.indexOfFirst {
                    it.messageId == chatDetails!!.lastOpenedMsgId
                }
            }
            Log.i(
                "vkpro",
                "started scrolling to id;" + chatDetails?.lastOpenedMsgId + "index: $index"
            )
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.totalItemsCount }
            .distinctUntilChanged()
            .collect { (firstIndex, totalCount) ->
                if (totalCount == 0) return@collect

                if (firstIndex < 15) {
                    model.loadPrevPage()
                }

                if (totalCount - firstIndex < 25) {
                    model.loadNextPage()
                }
            }
    }


    LaunchedEffect(model.showToast) {
        if (model.showToast && model.toast != null) {
            model.showToast = false
            Toast.makeText(context, model.toast, Toast.LENGTH_SHORT).show()
        }
    }

    var searchVisible by remember { mutableStateOf(false) }


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
        Box {
            ChatToolbar(
                name = chatDetails?.name ?: "Chat $chatId",
                status = chatDetails?.status ?: "Tap to edit",
                scope = animatedVisibilityScope,
                backIcon = headerIcons.backIcon,
                profileIcon = profilePicPainter,
                icon1 = headerIcons.icon1,
                icon2 = headerIcons.icon2,
                icon3 = headerIcons.icon3,
                icon4 = headerIcons.icon4,
                style = headerStyle,
                isDarkTheme = isDarkTheme,
                onMenuClick = {
                    when (it) {
                        1 -> searchVisible = true
                        2 -> navController.navigate("theme/${theme.id}")
                    }
                },
                onProfileClick = {
                    navController.navigate("chatprofile")
                }
            )
            if (searchVisible)
                SearchBar(
                    backgroundColor = remember(isDarkTheme) {
                        if (isDarkTheme) Color(headerStyle.color_navbar_dark.toColorInt())
                        else Color(headerStyle.color_navbar.toColorInt())
                    },
                    color = remember(isDarkTheme) {
                        if (isDarkTheme) Color(headerStyle.color_navicons_dark.toColorInt())
                        else Color(headerStyle.color_navicons.toColorInt())
                    },
                    showArrows = true,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onExit = {
                        searchVisible = false
                        model.resetSearch()
                    },
                    onSearch = {
                        Log.i(
                            "vkpro",
                            "PARAM index = ${listState.firstVisibleItemIndex} ; id = ${messages[listState.firstVisibleItemIndex].messageId}"
                        )
                        model.search(it, messages[listState.firstVisibleItemIndex].messageId)
                    },
                    resultsLength = model.searchedResults.size,
                    currentResultIndex = model.currentSearchIndex,
                    onNext = { model.navigateSearchedItems(1) },
                    onPrev = { model.navigateSearchedItems(-1) }
                )
        }

        //body
        if (model.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
//        if (model.isLoadingPrev && !model.isLoading) {
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
        AnimatedVisibility(
            visible = !model.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
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
                            ticksIcon = blueTicksIcon,
                            bubbleStyle = 1,
                            bubbleRadius = bodyStyle.bubble_radius,
                            bubbleTipRadius = bodyStyle.bubble_tip_radius,
                            isFirst = i == 0 || messages[i - 1].userid != m.userid,
                            color = themeBodyColors.senderBubble,
                            textColor = themeBodyColors.textPrimary,
                            textColorSecondary = themeBodyColors.textSecondary,
                            searchedString = if (m.messageId in model.searchedItemsSet) model.searchTerm else null,
                            file = chatMediaViewModel.mediaMap[m.fileId],
                            screenWidthDp = screenWidthDp,
                            screenWidth = screenWidthForMedia,
                            imageLoader = imageLoader,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onMediaClick = {
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
                            searchedString = if (m.messageId in model.searchedItemsSet) model.searchTerm else null,
                            file = chatMediaViewModel.mediaMap[m.fileId],
                            screenWidthDp = screenWidthDp,
                            screenWidth = screenWidthForMedia,
                            imageLoader = imageLoader,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onMediaClick = {
                                navController.navigate("mediapreview/$it")
                            }
                        )
                    }
                }
            }
        }
//        if (model.isLoadingNext && !model.isLoading) {
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }

        //input
        ChatMessageBar(
            style = messageBarStyle,
            isDarkTheme = isDarkTheme,
            outerIcon = messageBarIcons.outerIcon,
            leftInnerIcon = messageBarIcons.leftInnerIcon,
            rightInnerIcon = messageBarIcons.rightInnerIcon,
            icon1 = messageBarIcons.icon1,
            icon2 = messageBarIcons.icon2,
            icon3 = messageBarIcons.icon3
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            val index = listState.firstVisibleItemIndex
            val msgId = messages.getOrNull(index)?.messageId
            if (msgId != null) {
                model.saveScrollPosition(msgId)
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

data class HeaderIcons(
    val backIcon: Painter,
    val profileIcon: Painter,
    val icon1: Painter,
    val icon2: Painter,
    val icon3: Painter,
    val icon4: Painter,
)

data class MessageBarIcons(
    val outerIcon: Painter,
    val leftInnerIcon: Painter,
    val rightInnerIcon: Painter,
    val icon1: Painter,
    val icon2: Painter,
    val icon3: Painter,
)

@Composable
fun getHeaderIcons(id: Int): HeaderIcons {
    return HeaderIcons(
        backIcon = rememberCustomIconPainter(id, "ic_back.png", 0, R.drawable.ic_back),
        profileIcon = rememberCustomIconPainter(id, "ic_profile.png", 0, R.drawable.user),
        icon1 = rememberCustomIconPainter(id, "ic_nav1.png", 0, R.drawable.ic_call),
        icon2 = rememberCustomIconPainter(id, "ic_nav2.png", 0, R.drawable.ic_videocall),
        icon3 = rememberCustomIconPainter(id, "ic_nav3.png", 0, R.drawable.ic_animate),
        icon4 = rememberCustomIconPainter(id, "ic_3dots.png", 0, R.drawable.ic_more)
    )
}

@Composable
fun getMessageBarIcons(id: Int): MessageBarIcons {
    return MessageBarIcons(
        outerIcon = rememberCustomIconPainter(id, "ic_outer_icon.png", 0, R.drawable.ic_send),
        leftInnerIcon = rememberCustomIconPainter(
            id,
            "ic_left_inner_icon.png",
            0,
            R.drawable.ic_emoji
        ),
        rightInnerIcon = rememberCustomIconPainter(
            id,
            "ic_right_inner_icon.png",
            0,
            R.drawable.ic_send
        ),
        icon1 = rememberCustomIconPainter(id, "ic_bottom_nav1.png", 0, R.drawable.ic_file),
        icon2 = rememberCustomIconPainter(id, "ic_bottom_nav2.png", 0, R.drawable.ic_camera),
        icon3 = rememberCustomIconPainter(id, "ic_bottom_nav3.png", 0, R.drawable.ic_animate)
    )
}













