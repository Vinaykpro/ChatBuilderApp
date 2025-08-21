package com.vinaykpro.chatbuilder.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.IMPORTSTATE
import com.vinaykpro.chatbuilder.data.local.MyConstants
import com.vinaykpro.chatbuilder.data.local.MyConstants.appUrl
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.BannerAdView
import com.vinaykpro.chatbuilder.ui.components.ChatListItem
import com.vinaykpro.chatbuilder.ui.components.CircularRevealWrapper
import com.vinaykpro.chatbuilder.ui.components.FloatingMenu
import com.vinaykpro.chatbuilder.ui.components.ImportChatWidget
import com.vinaykpro.chatbuilder.ui.components.SettingsItem
import com.vinaykpro.chatbuilder.ui.components.ThemeItem
import com.vinaykpro.chatbuilder.ui.theme.DarkColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@SuppressLint("RememberReturnType", "UseKtx")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isDarkTheme: MutableState<Boolean> = mutableStateOf(false),
    themeViewModel: ThemeViewModel,
    prefs: SharedPreferences,
    sharedFileUri: Uri?
) {
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )
    val chats by homeViewModel.chatsList.collectAsState()

    val themes by themeViewModel.themes.collectAsState()
    val selectedTheme by themeViewModel.selectedThemeId.collectAsState()

    val colors = MaterialTheme.colorScheme

    val view = LocalView.current
    val activity = LocalContext.current as Activity
    val useDarkIcons = colors.primary.luminance() > 0.5f
    SideEffect {
        val window = activity.window
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = useDarkIcons
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                "Storage access denied! It is needed for saving chats into PDF/HTML Format!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    // Taking write access for A10 below
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val scope = rememberCoroutineScope()

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val toolbarState = rememberCollapsingToolbarScaffoldState()

    val pagerState = rememberPagerState(pageCount = { HomeTabs.entries.size })
    val selectedTabIndex =
        remember(pagerState.currentPage) { derivedStateOf { pagerState.currentPage } }

    var menuExpanded by remember { mutableStateOf(false) }

    // dark/light mode toggle
    var iconCenter by remember(Offset.Zero) { mutableStateOf(Offset.Zero) }
    var switchThemeAnim by remember { mutableStateOf(false) }
    var triggerAnimation by remember { mutableStateOf(false) }

    var blockTouches by remember { mutableStateOf(false) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            homeViewModel.importChatFromFile(context, uri)
        }
    }

    val pickFileLauncherForTheme = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            themeViewModel.importTheme(context, uri)
        }
    }

    val localUri = rememberSaveable { mutableStateOf(sharedFileUri?.toString()) }
    LaunchedEffect(localUri.value) {
        if (localUri.value != null) {
            val uri = localUri.value!!.toUri()
            homeViewModel.importChatFromFile(context, uri)
            (context as? Activity)?.intent = Intent()
            localUri.value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // real screen
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary)
                    .fillMaxWidth()
                    .padding(top = topPadding)
            )
            CollapsingToolbarScaffold(
                modifier = Modifier.weight(1f),
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
                            .background(color = MaterialTheme.colorScheme.primary)
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
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)
                            ),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val scope = rememberCoroutineScope()

                        IconButton(
                            onClick = {
                                DebounceClickHandler.run { navController.navigate("search") }
                            }
                        ) {
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
                            painter = painterResource(if (isDarkTheme.value) R.drawable.ic_lightmode else R.drawable.ic_darkmode),
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        if (!isDarkTheme.value) {
                                            switchThemeAnim = true
                                            blockTouches = true
                                            triggerAnimation = true
                                        } else {
                                            scope.launch {
                                                switchThemeAnim = true
                                                blockTouches = true
                                                triggerAnimation = true
                                                delay(50)
                                                isDarkTheme.value = false
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

                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = menuExpanded
                        ) {
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                containerColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                MyConstants.homeMenuList.forEachIndexed { index, item ->
                                    Text(
                                        text = item,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                menuExpanded = false
                                                when (item) {
                                                    "Hidden chats" -> {
                                                        navController.navigate("hiddenchats")
                                                    }

                                                    "Themes" -> {
                                                        scope.launch { pagerState.scrollToPage(1) }
                                                    }

                                                    "Settings" -> {
                                                        scope.launch { pagerState.scrollToPage(2) }
                                                    }

                                                    "Rate this app" -> {
                                                        val intent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            appUrl.toUri()
                                                        )
                                                        context.startActivity(intent)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 18.dp, vertical = 12.dp)
                                    )
                                }
                            }
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
                            )
                        )
                    }
                }
            ) {
                Column {
                    BannerAdView(adId = "ca-app-pub-2813592783630195/7066679569")
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) { page ->
                        when (page) {
                            0 -> Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
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
                                                lastMessage = chat.lastmsg,
                                                lastSeen = chat.lastmsgtime,
                                                onClick = {
                                                    DebounceClickHandler.run {
                                                        navController.navigate(
                                                            "chat/${chat.chatid}?messageId=${chat.lastOpenedMsgId ?: -1}"
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                            }

                            1 -> LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                items(themes) { i ->
                                    ThemeItem(
                                        selected = i.id == selectedTheme,
                                        id = i.id,
                                        name = i.name,
                                        author = i.author,
                                        iconColor = Color(i.appcolor.toColorInt()),
                                        onClick = {
                                            themeViewModel.changeTheme(i.id)
                                            prefs.edit().putInt("themeId", i.id).apply()
                                        },
                                        onNextClick = {
                                            DebounceClickHandler.run { navController.navigate("theme/${i.name}") }
                                        })
                                }
                            }

                            2 -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                SettingsItem(
                                    icon = painterResource(R.drawable.ic_theme),
                                    name = "Chat theme",
                                    context = "Create, import or customize the current chat theme",
                                    onClick = {
                                        navController.navigate("themes")
                                    }
                                )
                                SettingsItem(
                                    icon = painterResource(R.drawable.ic_animate),
                                    name = "Animate a chat",
                                    context = "Play any chat realtime",
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Update coming soon",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                SettingsItem(
                                    icon = painterResource(R.drawable.ic_lockedchats),
                                    name = "Locked chats",
                                    context = "View hidden chats",
                                    onClick = {
                                        navController.navigate("hiddenchats")
                                    }
                                )
                                SettingsItem(
                                    icon = painterResource(R.drawable.ic_starredmessages),
                                    name = "Starred messages",
                                    context = "See all the starred messages",
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Update coming soon",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { scope.launch { pagerState.animateScrollToPage(0) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (selectedTabIndex.value == 0) R.drawable.ic_chats_selected else R.drawable.ic_chats_unselected),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { scope.launch { pagerState.animateScrollToPage(1) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (selectedTabIndex.value == 1) R.drawable.ic_theme_selected else R.drawable.ic_theme_unselected),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { scope.launch { pagerState.animateScrollToPage(2) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (selectedTabIndex.value == 2) R.drawable.ic_settings_selected else R.drawable.ic_settings_unselected),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
            )
        }
    }

    // Real FAB Button
    AnimatedVisibility(
        visible = pagerState.currentPage != 2,
        enter = scaleIn(),
        exit = fadeOut()
    ) {
        FloatingMenu(
            color = MaterialTheme.colorScheme.primary,
            page = pagerState.currentPage,
            onClick1 = {
                if (pagerState.currentPage == 0) {
                    homeViewModel.addChat()
                } else {
                    themeViewModel.addBlankTheme()
                }
            },
            onClick2 = {
                if (pagerState.currentPage == 0) {
                    pickFileLauncher.launch(arrayOf("application/zip", "text/plain"))
                } else {
                    pickFileLauncherForTheme.launch(arrayOf("application/zip"))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = homeViewModel.importState != IMPORTSTATE.NONE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ImportChatWidget(
            step = homeViewModel.importState,
            files = homeViewModel.importedFileList,
            onUpdate = { homeViewModel.importedFileList = it },
            onMediaSave = {
                if (homeViewModel.importState == IMPORTSTATE.MEDIASELECTION) {
                    homeViewModel.importMedia = it
                    homeViewModel.continueImport(true)
                }
            },
            onWatchAdAction = {
                homeViewModel.startRewardAd(context)
            },
            onClose = { homeViewModel.closeImport() }
        )
    }

    // fake screen for light/dark mode switch
    if (switchThemeAnim) {
        val isDark = remember { mutableStateOf(isDarkTheme.value) }
        val centerX by remember { mutableIntStateOf(iconCenter.x.toInt()) }
        val centerY by remember { mutableIntStateOf(iconCenter.y.toInt()) }
        CircularRevealWrapper(
            modifier = Modifier.fillMaxSize(),
            centerX = centerX,
            centerY = centerY,
            triggerAnimation = triggerAnimation,
            isDark = isDark.value,
            onAnimationStart = {
                isDarkTheme.value = !isDarkTheme.value
            },
            onAnimationEnd = {
                triggerAnimation = false
                switchThemeAnim = false
                blockTouches = false
                prefs.edit { putBoolean("isDarkEnabled", !isDark.value) }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // header
                    Spacer(
                        modifier = Modifier
                            .background(color = DarkColorScheme.primary)
                            .fillMaxWidth()
                            .padding(top = topPadding)
                    )
                    CollapsingToolbarScaffold(
                        modifier = Modifier.weight(1f),
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
                                    .background(color = DarkColorScheme.primary)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(color = Color.Black)
                                    .road(
                                        whenCollapsed = Alignment.BottomCenter,
                                        whenExpanded = Alignment.BottomCenter
                                    )
                                    .background(
                                        color = DarkColorScheme.primary,
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
                                        "sun_moon_anim.json"
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
                                    )
                                )
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            when (pagerState.currentPage) {
                                0 -> if (chats.isEmpty())
                                    Text(
                                        text = "No chats available, You can create new ones or import from .zip or .txt files by pressing the button below",
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
                                        color = DarkColorScheme.onSecondaryContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 30.dp)
                                    )
                                else
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(chats, key = { chat -> chat.chatid }) { chat ->
                                            ChatListItem(
                                                id = chat.chatid,
                                                name = chat.name,
                                                lastMessage = chat.lastmsg,
                                                lastSeen = chat.lastmsgtime,
                                                isForceDark = true
                                            )
                                        }
                                    }

                                1 -> LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    items(themes) { i ->
                                        ThemeItem(
                                            selected = i.id == selectedTheme,
                                            id = i.id,
                                            name = i.name,
                                            author = i.author,
                                            iconColor = Color(i.appcolor.toColorInt()),
                                            forceDark = true
                                        )
                                    }
                                }

                                2 -> Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    SettingsItem(
                                        icon = painterResource(R.drawable.ic_theme),
                                        name = "Chat theme",
                                        context = "Create, import or customize the current chat theme",
                                        isForceDark = true
                                    )
                                    SettingsItem(
                                        icon = painterResource(R.drawable.ic_animate),
                                        name = "Animate a chat",
                                        context = "Play any chat realtime",
                                        isForceDark = true
                                    )
                                    SettingsItem(
                                        icon = painterResource(R.drawable.ic_lockedchats),
                                        name = "Locked chats",
                                        context = "View hidden chats",
                                        isForceDark = true
                                    )
                                    SettingsItem(
                                        icon = painterResource(R.drawable.ic_starredmessages),
                                        name = "Starred messages",
                                        context = "See all the starred messages",
                                        isForceDark = true
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color.Black)
                            .background(
                                color = DarkColorScheme.primary,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { scope.launch { pagerState.animateScrollToPage(0) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(if (selectedTabIndex.value == 0) R.drawable.ic_chats_selected else R.drawable.ic_chats_unselected),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { scope.launch { pagerState.animateScrollToPage(1) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(if (selectedTabIndex.value == 1) R.drawable.ic_theme_selected else R.drawable.ic_theme_unselected),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { scope.launch { pagerState.animateScrollToPage(2) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(if (selectedTabIndex.value == 2) R.drawable.ic_settings_selected else R.drawable.ic_settings_unselected),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = DarkColorScheme.primary)
                            .padding(bottom = bottomPadding)
                    )
                }

                if (pagerState.currentPage < 2)
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = bottomPadding + 90.dp, end = 24.dp),
                        containerColor = DarkColorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(100.dp),
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(
                                if (pagerState.currentPage == 0) R.drawable.ic_newchat
                                else R.drawable.ic_newtheme
                            ),
                            contentDescription = "Menu"
                        )
                    }
            }
        }
    }

    // block touches on screen
    if (blockTouches) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {})
        )
    }

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch {
            pagerState.animateScrollToPage(0)
        }
    }
}

enum class HomeTabs(
    val text: String
) {
    Chats(
        text = "Chats"
    ),
    Themes(
        text = "Themes"
    ),
    Settings(
        text = "Settings"
    )
}
