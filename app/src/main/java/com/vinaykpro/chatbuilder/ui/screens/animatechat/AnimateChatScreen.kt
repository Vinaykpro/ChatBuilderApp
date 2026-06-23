package com.vinaykpro.chatbuilder.ui.screens.animatechat

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
import com.vinaykpro.chatbuilder.data.local.toInt
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatListItem
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SelectChatWidget
import com.vinaykpro.chatbuilder.ui.components.SelectMessageRangeWidget
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.screens.chat.HeaderIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.MessageBarIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.getHeaderIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.getMessageBarIcons
import com.vinaykpro.chatbuilder.ui.screens.chat.toParsed
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.serialization.json.Json

//class AnimateChatViewModel(application: Application) : AndroidViewModel(application) {
//
//}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AnimateChatScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current
    val theme = LocalThemeEntity.current

    val headerStyle = remember(theme.headerstyle) {
        try {
            Json.decodeFromString<HeaderStyle>(theme.headerstyle)
        } catch (_: Exception) {
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

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    val model: AnimateChatViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    model.initialLoad()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val rangeStr by savedStateHandle
        ?.getStateFlow<String?>("range", null)
        ?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    LaunchedEffect(rangeStr) {
        if (!rangeStr.isNullOrEmpty()) {
            Log.d("vkpro3", "Pop back caught data: $rangeStr")
            if (!rangeStr.isNullOrEmpty()) {
                val pieces = rangeStr!!.split(",")
                if (pieces.size == 2) {
                    model.rangeStart = Integer.parseInt(pieces[0])
                    model.rangeEnd = Integer.parseInt(pieces[1])
                }
                savedStateHandle?.set("range", null)
            }
        }
    }

    val chats by model.chatsList.collectAsState()

    var selectChatVisible by remember { mutableStateOf<Boolean>(false) }

    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(model.selectedChat) {
        refreshKey++
    }

    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = if (model.selectedChat != null) model.selectedChat!!.chatid else 0,
        refreshKey = refreshKey,
        fallback = R.drawable.user
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
            )
    ) {
        BasicToolbar("Animate chat", onBackClick = {
            navController.popBackStack()
        })
        Box(Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 10.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(themeBodyColors.chatBackground)
                    .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
            ) {
                ChatToolbar(
                    noTopPadding = true,
                    name = if (model.selectedChat != null) model.selectedChat!!.name else "Vinaykpro",
                    status = if (model.typing) "typing..." else "online",
                    backIcon = headerIcons.backIcon,
                    profileIcon = profilePicPainter,
                    icon1 = headerIcons.icon1,
                    icon2 = headerIcons.icon2,
                    icon3 = headerIcons.icon3,
                    icon4 = headerIcons.icon4,
                    style = headerStyle,
                    isDarkTheme = isDarkTheme
                ) { }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.height(120.dp)
                ) {
                    if (model.currMsg >= 1)
                        SenderMessage(
                            text = "Hi bro! this is how it animates, click start to see your chat play in live",
                            sentTime = "12:00 pm",
                            ticksIcon = blueTicksIcon,
                            bubbleStyle = bodyStyle.bubble_style,
                            bubbleRadius = bodyStyle.bubble_radius,
                            bubbleTipRadius = bodyStyle.bubble_tip_radius,
                            showTime = bodyStyle.show_time,
                            showTicks = bodyStyle.showticks,
                            isFirst = true,
                            color = themeBodyColors.senderBubble,
                            textColor = themeBodyColors.textPrimary,
                            textColorSecondary = themeBodyColors.textSecondary,
                            screenWidthDp = screenWidthDp,
                            imageLoader = imageLoader
                        )
                    Spacer(modifier = Modifier.height(3.dp))
                    if (model.currMsg >= 2)
                        Message(
                            text = "Okay! lemme see...",
                            senderName = null,
                            sentTime = "12:01 pm",
                            bubbleStyle = bodyStyle.bubble_style,
                            bubbleRadius = bodyStyle.bubble_radius,
                            bubbleTipRadius = bodyStyle.bubble_tip_radius,
                            showTime = bodyStyle.show_time,
                            isFirst = true,
                            color = themeBodyColors.receiverBubble,
                            textColor = themeBodyColors.textPrimary,
                            textColorSecondary = themeBodyColors.textSecondary,
                            screenWidthDp = screenWidthDp,
                            imageLoader = imageLoader
                        )
                }
                Spacer(modifier = Modifier.height(3.dp))
                ChatMessageBar(
                    noBottomPadding = true,
                    style = messageBarStyle,
                    isDarkTheme = isDarkTheme,
                    readOnly = true,
                    value = model.currMsgInputBar,
                    outerIcon = messageBarIcons.outerIcon,
                    leftInnerIcon = messageBarIcons.leftInnerIcon,
                    rightInnerIcon = messageBarIcons.rightInnerIcon,
                    icon1 = messageBarIcons.icon1,
                    icon2 = messageBarIcons.icon2,
                    icon3 = messageBarIcons.icon3
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = null, indication = null, onClick = {})
            )
        }

        Text(
            text = "Chat & range selection",
            fontSize = 17.sp,
            fontWeight = FontWeight(500),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 12.dp, top = 18.dp, bottom = 2.dp)
        )

        SelectChatWidget(
            chat = model.selectedChat,
            pic = profilePicPainter,
            onClick = { selectChatVisible = true })
        SelectMessageRangeWidget(
            rStart = model.rangeStart,
            rEnd = model.rangeEnd,
            onClick = {
                if (model.selectedChat != null) {
                    navController.navigate(
                        "chat/${model.selectedChat!!.chatid}?messageId=${model.selectedChat!!.lastOpenedMsgId ?: -1}&range=${true}"
                    )
                } else {
                    Toast.makeText(context, "Select a chat first", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Text(
            text = "Animation settings",
            fontSize = 17.sp,
            fontWeight = FontWeight(500),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 12.dp, top = 18.dp, bottom = 2.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            SwitchItem(
                name = "Show typing indicator",
                context = "Show typing indicator in chat navigation bar",
                checked = model.isTypingEnabled,
                onCheckChange = {
                    model.isTypingEnabled = it
                }
            )
            SwitchItem(
                name = "Animate typing in input bar",
                context = "Show typing text from your side in input bar",
                checked = model.isInputTypingEnabled,
                onCheckChange = {
                    model.isInputTypingEnabled = it
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Start Animation",
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LightColorScheme.primary)
                .clickable {
                    Log.d("vkpro3", "navigating using ${model.rangeStart},${model.rangeEnd}")
                    if (model.selectedChat != null && model.rangeStart != -1 && model.rangeEnd != -1) {
                        val settingsStr =
                            "${model.isTypingEnabled.toInt}${model.isInputTypingEnabled.toInt}"
                        navController.navigate("livechat?chatId=${model.selectedChat?.chatid ?: -1}&rStart=${model.rangeStart}&rEnd=${model.rangeEnd}&settings=${settingsStr}")
                    } else {
                        Toast.makeText(context, "Select a chat & range first", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .padding(12.dp)
        )
    }

    if (selectChatVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(start = 5.dp)
                    .heightIn(max = 500.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Select a chat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        DebounceClickHandler.run {
                            selectChatVisible = false
                        }
                    }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                if (chats.isEmpty())
                    Text(
                        text = "No chats available, You can create new ones or import from .zip or .txt files by pressing the button below",
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                else
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats, key = { chat -> chat.chatid }) { chat ->
                            ChatListItem(
                                id = chat.chatid,
                                name = chat.name,
                                lastMessage = "Tap here to select",
                                lastSeen = "",
                                onClick = {
                                    DebounceClickHandler.run {
                                        if (model.selectedChat != null && model.selectedChat!!.chatid != chat.chatid) {
                                            model.rangeStart = -1
                                            model.rangeEnd = -1
                                        }
                                        model.selectedChat = chat
                                        selectChatVisible = false
                                    }
                                }
                            )
                        }
                    }
            }
        }

    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun AnimateChatScreenPreview() {
    val mockHeaderStyleJson =
        remember { Json.encodeToString(HeaderStyle.serializer(), HeaderStyle()) }
    val mockBodyStyleJson = remember { Json.encodeToString(BodyStyle.serializer(), BodyStyle()) }
    val mockMessageBarStyleJson =
        remember { Json.encodeToString(MessageBarStyle.serializer(), MessageBarStyle()) }

    CompositionLocalProvider(
        LocalThemeEntity provides ThemeEntity(
            id = 1,
            name = "Preview Theme",
            author = "Vinaykpro",
            appcolor = "#FF1283A6",
            appcolordark = "#FF323232",
            headerstyle = mockHeaderStyleJson,
            bodystyle = mockBodyStyleJson,
            messagebarstyle = mockMessageBarStyleJson
        )
    ) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                with(this@SharedTransitionLayout) {
//                    AnimateChatScreen()
                }
            }
        }
    }
}