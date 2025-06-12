package com.vinaykpro.chatbuilder.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.utils.ChatsList
import com.vinaykpro.chatbuilder.ui.components.ChatListItem
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.positionInRoot
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinaykpro.chatbuilder.ui.components.CircularRevealWrapper
import com.vinaykpro.chatbuilder.ui.theme.DarkColorScheme
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import kotlinx.coroutines.delay
import com.vinaykpro.chatbuilder.ui.components.FloatingMenu
import com.vinaykpro.chatbuilder.ui.components.SettingsItem

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: MutableState<Boolean> = mutableStateOf(false),
    viewModel: HomeViewModel = viewModel(
    factory = HomeViewModelFactory(ChatsList()))
) {
    val uiState = viewModel.state.value
    val colors = MaterialTheme.colorScheme
    val fakeColors = if (isDarkTheme.value) LightColorScheme else DarkColorScheme

    val scope = rememberCoroutineScope()
    val context = LocalContext.current;

    val toolbarState = rememberCollapsingToolbarScaffoldState()

    val pagerState = rememberPagerState(pageCount = { HomeTabs.entries.size })
    val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }

    // dark/light mode toggle
    var iconCenter by remember { mutableStateOf(Offset.Zero) }
    var switchThemeAnim by remember { mutableStateOf(false) }
    var triggerAnimation by remember { mutableStateOf(false) }

    var blockTouches by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colors.background)) {
        // real screen
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.background(color = colors.primary).fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
            CollapsingToolbarScaffold(modifier = Modifier.weight(1f),
                state = toolbarState,
                scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
                toolbar = {
                    val textSize = (20 + 18 * toolbarState.toolbarState.progress).sp
                    val iconSize = (42 + 20 * toolbarState.toolbarState.progress).dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .pin()
                            .background(color = colors.primary)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(color = colors.background)
                            .road(
                                whenCollapsed = Alignment.BottomCenter,
                                whenExpanded = Alignment.BottomCenter
                            )
                            .background(
                                color = colors.primary,
                                shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)
                            ),
                        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                    ) {
                        val scope = rememberCoroutineScope()

                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            painter = painterResource(if(isDarkTheme.value) R.drawable.ic_lightmode else R.drawable.ic_darkmode),
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable  (
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        if(!isDarkTheme.value) {
                                            switchThemeAnim = true
                                            blockTouches = true
                                            triggerAnimation = true
                                        } else {
                                            scope.launch {
                                                switchThemeAnim = true
                                                blockTouches = true
                                                triggerAnimation = true
                                                delay(50)
                                                isDarkTheme.value = false;
                                            }
                                        }
                                    }
                                )
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.positionInRoot()
                                    val size = coordinates.size
                                    iconCenter = Offset(
                                        x = position.x + size.width / 2,
                                        y = position.y + size.height / 2
                                    )
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(onClick = { /* Handle navigation click */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .padding(start = 10.dp)
                            .road(
                                whenCollapsed = Alignment.TopStart,
                                whenExpanded = Alignment.Center
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "brand",
                            modifier = Modifier.size(iconSize),
                            tint = Color.White
                        )
                        Text(
                            "ChatBuilder",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = textSize,
                                fontWeight = FontWeight(500)
                            ),
                        )
                    }
                }
            ) {
                HorizontalPager(state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    beyondViewportPageCount = 1
                ) { page -> when(page) {
                        0 -> Box(modifier = Modifier
                            .weight(1f)
                            .background(colors.background), contentAlignment = Alignment.Center) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.chatsList) { chat ->
                                    ChatListItem(
                                        icon = R.drawable.iconalpha,
                                        name = chat.name,
                                        lastMessage = chat.lastMessage,
                                        lastSeen = chat.lastSeen,
                                        navController = navController
                                    )
                                }
                            }
                        }
                        1 -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            SettingsItem(icon = painterResource(R.drawable.ic_theme),
                                name = "Chat theme",
                                context = "Create, import or customize the current chat theme",
                                onClick = {navController.navigate("themes")}
                            )
                            SettingsItem(icon = painterResource(R.drawable.ic_animate),
                                name = "Animate a chat",
                                context = "Play any chat realtime")
                            SettingsItem(icon = painterResource(R.drawable.ic_lockedchats),
                                name = "Locked chats",
                                context = "View hidden chats")
                            SettingsItem(icon = painterResource(R.drawable.ic_starredmessages),
                                name = "Starred messages",
                                context = "See all the starred messages")
                        }
                    }
                }
            }

            Row (modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    color = colors.primary,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = rememberRipple(
                            bounded = true,
                            color = Color(0xFF056175),
                            radius = 80.dp
                        ),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                    )) {
                    Text(text = "Chats", style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight(500)
                    ), modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(if (selectedTabIndex.value == 0) 1f else 0.7f))
                }
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = rememberRipple(
                            bounded = true,
                            color = Color(0xFF056175),
                            radius = 80.dp
                        ),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )) {
                    Text(text = "Settings", style = TextStyle(
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight(500)
                    ), modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(if (selectedTabIndex.value == 1) 1f else 0.7f))
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }

    // Real FAMB Button
    FloatingMenu()

    // fake screen for light/dark mode switch
    if(switchThemeAnim) {
        val isDark = remember  { mutableStateOf(isDarkTheme.value) }
        CircularRevealWrapper(
            modifier = Modifier.fillMaxSize(),
            centerX = iconCenter.x.toInt(),
            centerY = iconCenter.y.toInt(),
            triggerAnimation = triggerAnimation,
            isDark = isDark.value,
            onAnimationStart = {
                isDarkTheme.value = !isDarkTheme.value
            },
            onAnimationEnd = {
                triggerAnimation = false
                switchThemeAnim = false
                blockTouches = false
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000))
                ) {
                    // header
                    CollapsingToolbarScaffold(
                        modifier = Modifier.weight(1f),
                        state = toolbarState,
                        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
                        toolbar = {
                            val textSize = (20 + 18 * toolbarState.toolbarState.progress).sp
                            val iconSize = (32 + 18 * toolbarState.toolbarState.progress).dp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .pin()
                                    .background(color = Color(0xFF323232))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(color = Color(0xFF000000))
                                    .road(
                                        whenCollapsed = Alignment.BottomCenter,
                                        whenExpanded = Alignment.BottomCenter
                                    )
                                    .background(
                                        color = Color(0xFF323232),
                                        shape = RoundedCornerShape(
                                            bottomStart = 15.dp,
                                            bottomEnd = 15.dp
                                        )
                                    ),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                val composition by rememberLottieComposition(
                                    LottieCompositionSpec.Asset(
                                        "sunmoonanim.json"
                                    )
                                )
                                val progress =
                                    remember { Animatable(if (isDark.value) 0.9f else 0f) }

                                LaunchedEffect(composition) {
                                    if (composition != null) {
                                        progress.animateTo(
                                            targetValue = if (isDark.value) 0f else 0.9f,
                                            animationSpec = tween(durationMillis = 800)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (composition != null) {
                                        LottieAnimation(
                                            composition = composition,
                                            progress = { progress.value },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_lightmode),
                                            contentDescription = "Theme toggle",
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.White
                                        )
                                    }
                                }


                                Spacer(modifier = Modifier.width(12.dp))

                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Search",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .road(
                                        whenCollapsed = Alignment.TopStart,
                                        whenExpanded = Alignment.Center
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.iconalpha),
                                    contentDescription = "brand",
                                    modifier = Modifier.size(iconSize)
                                )
                                Text(
                                    "ChatBuilder",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = textSize,
                                        fontWeight = FontWeight(500)
                                    ),
                                    modifier = Modifier
                                )
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.chatsList) { chat ->
                                    ChatListItem(
                                        icon = R.drawable.iconalpha,
                                        name = chat.name,
                                        lastMessage = chat.lastMessage,
                                        lastSeen = chat.lastSeen,
                                        navController = navController,
                                        isForceFake = true
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .background(
                                color = Color(0xFF323232),
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = "Chats", style = TextStyle(
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight(500)
                                ), modifier = Modifier
                                    .align(Alignment.Center)
                                    .alpha(if (selectedTabIndex.value == 0) 1f else 0.7f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = "Settings", style = TextStyle(
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight(500)
                                ), modifier = Modifier
                                    .align(Alignment.Center)
                                    .alpha(if (selectedTabIndex.value == 1) 1f else 0.7f)
                            )
                        }
                    }
                }

                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 90.dp, end = 24.dp),
                    containerColor = Color(0xFF323232),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(100.dp),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Menu"
                    )
                }
            }
        }
    }

    // block touches on screen
    if(blockTouches) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}))
    }
}

enum class HomeTabs (
    val text : String
) {
    Chats(
        text = "Chats"
    ),
    Settings(
        text = "Settings"
    )
}
