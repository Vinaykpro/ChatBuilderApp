package com.vinaykpro.chatbuilder.ui.screens.chat

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.vinaykpro.chatbuilder.ui.components.ExportChatWidget
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SearchBar
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.components.SwapSenderWidget
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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

    var currDate by remember { mutableStateOf("") }
    var showDate by remember { mutableStateOf(false) }

    var showReceiverName by remember { mutableStateOf(false) }

    val messageBarUserIndex by model.messageBarSenderIndex.collectAsState()

    var searchVisible by remember { mutableStateOf(false) }
    var swapUsersVisible by remember { mutableStateOf(false) }
    var dateNavigatorVisible by remember { mutableStateOf(false) }
    var clearChatVisible by remember { mutableStateOf(false) }
    var addUserVisible by remember { mutableStateOf(false) }
    var exportChatVisible by remember { mutableStateOf(false) }
    var exportChatStep by remember { mutableIntStateOf(0) }
    var exportProgress by remember { mutableFloatStateOf(-1f) }

    LaunchedEffect(Unit) {
        delay(50)
        chatDetails.let {
            currentUserId = chatDetails?.senderId ?: -1
            showReceiverName = chatDetails?.showReceiverName == true
            model.loadUserList(isDarkTheme)
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
        launch {
            snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.totalItemsCount }
                .distinctUntilChanged()
                .collect { (firstIndex, totalCount) ->
                    if (totalCount == 0) return@collect
                    val date = messages[firstIndex].date
                    if (date != null && date != currDate) currDate = date

                    if (firstIndex < 15) {
                        model.loadPrevPage()
                    }

                    if (totalCount - firstIndex < 25) {
                        model.loadNextPage()
                    }
                }
        }
        launch {
            snapshotFlow { listState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { scrolling ->
                    Log.i("vkpro", "Scroll state change $scrolling")
                    if (scrolling) {
                        showDate = true
                    } else {
                        delay(1000)
                        showDate = false
                    }
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
                                    model.loadUserList(isDarkTheme)
                                }
                                swapUsersVisible = true
                            }

                            4 -> {
                                if (model.datesList.isEmpty()) {
                                    model.loadDatesList()
                                }
                                dateNavigatorVisible = true
                            }

                            5 -> {
                                exportChatVisible = true
                            }

                            6 -> {
                                model.hideUnhideChat(hidden, onDone = {
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
                    DebounceClickHandler.run { navController.navigate("chatprofile") }
                },
                onBackClick = {
                    navController.popBackStack()
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
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    Toast.makeText(
                                        context,
                                        "Copied to clipboard",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )

                        else -> Message(
                            text = m.message,
                            sentTime = m.time.toString(),
                            senderName = if (showReceiverName) m.username else null,
                            senderColor = if (showReceiverName) model.userColorMap[m.userid]
                                ?: MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onPrimaryContainer,
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
                                Toast.makeText(
                                    context,
                                    "Copied to clipboard",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showDate,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                ChatNote(
                    note = currDate,
                    color = if (isDarkTheme) Color(0xFF2a2a2a) else Color(0xFFdddddd),
                    textColor = themeBodyColors.textSecondary
                )
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
                model.addNewMessage(it, model.userList[messageBarUserIndex])
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
            showReceiverName = showReceiverName,
            onSenderChange = {
                currentUserId = it
            },
            receiverNameStatusChange = {
                model.updateReceiverNameVisibility(it)
                showReceiverName = it
            },
            onClose = {
                model.updateSenderId(currentUserId)
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
                currentDate = currDate,
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

    AnimatedVisibility(
        visible = exportChatVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ExportChatWidget(
            model.fullMessageList,
            step = exportChatStep,
            progress = exportProgress,
            onClose = { exportChatVisible = false },
            onWatchAdAction = {
                exportChatStep = 1
                model.loadAndShowAd(
                    context,
                    onAdFinished = {
                        exportChatStep = 2
                        exportProgress = 0f
                        if (it) model.loadAndExport(
                            context,
                            chatDetails?.name ?: "",
                            currentUserId,
                            onUpdate = {
                                exportProgress = it / 100.toFloat()
                            },
                            onDone = {
                                exportChatVisible = false
                                exportProgress = -1f
                                if (it == null) {
                                    model.toast = "Unable to save/share the file"
                                    model.showToast = true
                                    return@loadAndExport
                                }
                                model.toast = "File saved to Downloads/ChatBuilder"
                                model.showToast = true
                                val uri = it
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Share chat as Pdf"
                                    )
                                )
                            }
                        )
                        else model.loadAndExportToHTML(
                            context,
                            chatDetails?.name ?: "",
                            currentUserId,
                            onUpdate = {
                                exportProgress = it / 100.toFloat()
                            },
                            onDone = {
                                exportChatVisible = false
                                exportProgress = -1f
                                if (it == null) {
                                    model.toast = "Unable to save/share the file"
                                    model.showToast = true
                                    return@loadAndExportToHTML
                                }
                                model.toast = "File saved to Downloads/ChatBuilder"
                                model.showToast = true
                                val uri = it
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/html"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        "Share chat as Html"
                                    )
                                )
                            }
                        )
                    },
                    onFailed = {
                        exportChatStep = 0
                        Toast.makeText(
                            context,
                            "Failed to load Ad! Please connect to internet and disable ad blockers",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
    }

    BackHandler {
        if (swapUsersVisible || dateNavigatorVisible || clearChatVisible || addUserVisible || exportChatVisible) {
            swapUsersVisible = false
            dateNavigatorVisible = false
            clearChatVisible = false
            addUserVisible = false
            if (exportChatStep == 0) exportChatVisible = false
        } else {
            navController.popBackStack()
        }
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













