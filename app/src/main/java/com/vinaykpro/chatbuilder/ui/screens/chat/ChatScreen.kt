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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.VideoFrameDecoder
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MESSAGETYPE
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.data.models.ChatMediaViewModel
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.AddUserWidget
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatNote
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.ClearChatWidget
import com.vinaykpro.chatbuilder.ui.components.DateNavigationWidget
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SearchBar
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.components.SwapSenderWidget
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.Json
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalCoilApi::class)
//@Preview(showBackground = true)
@Composable
fun SharedTransitionScope.ChatScreen(
    chatId: Int = 1,
    messageId: Int = -1,
    hidden: Int = 0,
    isDarkTheme: Boolean = false,
    navController: NavHostController = rememberNavController(),
    animatedVisibilityScope: AnimatedVisibilityScope,
    chatMediaViewModel: ChatMediaViewModel,
) {
    val theme = LocalThemeEntity.current

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

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

    var refreshKey by remember { mutableIntStateOf(0) }

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = chatMediaViewModel.currentChat?.chatid,
        refreshKey = refreshKey,
        fallback = R.drawable.user
    )

    val listState = rememberLazyListState()
    val chatDetails by model.chatDetails.collectAsState()
    val messages by model.messages.collectAsState(initial = emptyList())
    val isLoading by model.isLoading.collectAsState(initial = true)

    var currentUserId by remember { mutableIntStateOf(chatDetails?.senderId ?: -1) }
    var currentDateId by remember { mutableIntStateOf(chatDetails?.senderId ?: -1) }

    val messageBarUserIndex by model.messageBarSenderIndex.collectAsState()

    var searchVisible by remember { mutableStateOf(false) }
    var swapUsersVisible by remember { mutableStateOf(false) }
    var dateNavigatorVisible by remember { mutableStateOf(false) }
    var clearChatVisible by remember { mutableStateOf(false) }

    var addUserVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        chatDetails.let {
            currentUserId = chatDetails?.senderId ?: -1
            model.loadUserList(chatId, isDarkTheme)
        }
        delay(250)
        refreshKey++
        chatMediaViewModel.load(chatId)
        chatDetails?.let { model.initialLoad(if (messageId >= 0) messageId else it.lastOpenedMsgId) }
    }

    LaunchedEffect(chatMediaViewModel.showInChat) {
        if (chatMediaViewModel.showInChat != null) {
            model.loadMessagesAtId(chatMediaViewModel.showInChat!!)
            chatMediaViewModel.showInChat = null
        }
    }

    LaunchedEffect(messages) {
        Log.i("vkpro", "Tried scrolling to index: ${model.scrollIndex}")
        if (model.needScroll && model.scrollIndex != null) {
            model.needScroll = false
            Log.i(
                "vkpro",
                "started scrolling to id;" + chatDetails?.lastOpenedMsgId + "index: ${model.scrollIndex}"
            )
            if (model.scrollIndex!! < messages.size && model.scrollIndex!! >= 0)
                listState.scrollToItem(model.scrollIndex!!)
            else if (model.scrollIndex!! == -1) {
                listState.scrollToItem(messages.size - 1)
            }
            model.scrollIndex = null
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
                    DebounceClickHandler.run {
                        when (it) {
                            0 -> navController.navigate("chatprofile")
                            1 -> searchVisible = true
                            2 -> navController.navigate("theme/${theme.id}")
                            3 -> {
                                if (model.userList.isEmpty()) {
                                    model.loadUserList(chatId, isDarkTheme)
                                }
                                swapUsersVisible = true
                            }

                            4 -> {
                                if (model.datesList.isEmpty()) {
                                    model.loadDatesList(chatId)
                                }
                                dateNavigatorVisible = true
                            }

                            5 -> {
                                Toast.makeText(context, "Update coming soon", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            6 -> {
                                model.hideUnhideChat(chatId, hidden, onDone = {
                                    if (hidden == 0) Toast.makeText(
                                        context,
                                        "Chat hidden, view hidden chats from settings or from home menu",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    else Toast.makeText(
                                        context,
                                        "Chat moved to home screen",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                })
                            }

                            7 -> {
                                clearChatVisible = true
                            }
                        }
                    }
                },
                onProfileClick = {
                    DebounceClickHandler.run(1200) { navController.navigate("chatprofile") }
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        AnimatedVisibility(
            visible = !isLoading,
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
                            bubbleStyle = 1,
                            bubbleRadius = bodyStyle.bubble_radius,
                            bubbleTipRadius = bodyStyle.bubble_tip_radius,
                            isFirst = (i == 0 || messages[i - 1].userid != m.userid || messages[i - 1].date != m.date),
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
                                DebounceClickHandler.run { navController.navigate("mediapreview/$it") }
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(it))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )

                        else -> Message(
                            text = m.message,
                            sentTime = m.time.toString(),
                            senderName = m.username,
                            senderColor = model.userColorMap[m.userid]
                                ?: MaterialTheme.colorScheme.onPrimaryContainer,
                            date = if (i == 0 || messages[i - 1].date != m.date) {
                                {
                                    ChatNote(
                                        m.date.toString(),
                                        color = themeBodyColors.dateBubble,
                                        textColor = themeBodyColors.textSecondary
                                    )
                                }
                            } else null,
                            bubbleStyle = 1,
                            bubbleRadius = bodyStyle.bubble_radius,
                            bubbleTipRadius = bodyStyle.bubble_tip_radius,
                            isFirst = (i == 0 || messages[i - 1].userid != m.userid || messages[i - 1].date != m.date),
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
                                DebounceClickHandler.run { navController.navigate("mediapreview/$it") }
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(it))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    }
                }
            }
        }

        //input
        ChatMessageBar(
            user =
                if (messageBarUserIndex > 0 && messageBarUserIndex < model.userList.size)
                    model.userList[messageBarUserIndex]
                else null,
            onUserChange = { model.updateMessageBarUserIndex(it) },
            onAddUser = { addUserVisible = true },
            onSend = {
                model.addNewMessage(chatId, it, model.userList[messageBarUserIndex])
            },
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

    AnimatedVisibility(
        visible = swapUsersVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        SwapSenderWidget(
            users = model.userList,
            currentId = currentUserId,
            onSenderChange = {
                currentUserId = it
            },
            onClose = {
                model.updateSenderId(chatId, currentUserId)
                swapUsersVisible = false
            },
        )
    }

    AnimatedVisibility(
        visible = dateNavigatorVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (model.datesList.isEmpty())
            CircularProgressIndicator()
        else
            DateNavigationWidget(
                dates = model.datesList,
                currentId = currentDateId,
                onNavigation = {
                    model.navigateToDate(it)
                    currentDateId = it
                },
                onClose = {
                    dateNavigatorVisible = false
                },
            )
    }

    AnimatedVisibility(
        visible = clearChatVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ClearChatWidget(
            onCancel = {
                clearChatVisible = false
            },
            onClear = {
                chatMediaViewModel.clearChat(chatId, onDone = {
                    imageLoader.diskCache?.clear()
                    navController.popBackStack()
                })
            }
        )
    }

    AnimatedVisibility(
        visible = addUserVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AddUserWidget(
            onClose = { addUserVisible = false },
            onAdd = {
                model.addUser(it)
                addUserVisible = false
            }
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













