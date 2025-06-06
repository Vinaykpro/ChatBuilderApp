package com.vinaykpro.chatbuilder.ui.screens.home

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.openFile
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import kotlin.math.hypot
import kotlin.math.max

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview
@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    val state = rememberCollapsingToolbarScaffoldState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { HomeTabs.entries.size })
    val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }
    val context = LocalContext.current;
    val fileSearch=
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            openFile(it, context)
        }

    // dark mode animation
    // Track icon's center position in the layout coordinate system

    var iconCenter by remember { mutableStateOf(Offset.Zero) }
    var layoutSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Animation progress: radius of the reveal circle
    val radius = remember { Animatable(0f) }

    // Whether to show the layout or not (start invisible)
    var revealed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { c -> layoutSize = c.size.toSize() }) {

        // real screen
        Column(modifier = Modifier.fillMaxSize()) {
            CollapsingToolbarScaffold(modifier = Modifier.weight(1f),
                state = state,
                scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
                toolbar = {

                    val textSize = (20 + 18 * state.toolbarState.progress).sp
                    val iconSize = (32 + 18 * state.toolbarState.progress).dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .pin()
                            .background(color = Color(0xFF3DBFDC))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(color = Color.White)
                            .road(
                                whenCollapsed = Alignment.BottomCenter,
                                whenExpanded = Alignment.BottomCenter
                            )
                            .background(
                                color = Color(0xFF3DBFDC),
                                shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)
                            ),
                        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            fileSearch.launch("*/*")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* Handle navigation click */ }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle ,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        // Start animation when clicked
                                        revealed = true
                                        scope.launch {
                                            // Calculate max radius needed to cover whole layout from iconCenter
                                            val maxRadius = max(
                                                hypot(layoutSize.width, layoutSize.height),
                                                hypot(iconCenter.x, iconCenter.y)
                                            )
                                            radius.snapTo(0f)
                                            radius.animateTo(
                                                targetValue = maxRadius,
                                                animationSpec = tween(durationMillis = 3000)
                                            )
                                        }
                                    }
                                    // Capture icon center coordinates relative to parent layout
                                    .onGloballyPositioned { coordinates ->
                                        val position = coordinates.positionInWindow()
                                        val size = coordinates.size
                                        iconCenter = Offset(
                                            x = position.x + size.width / 2,
                                            y = position.y + size.height / 2
                                        )
                                    }
                            )
                        }
                        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("thememodeanim.json"))
                        val progress = remember { Animatable(0f) }

                        LaunchedEffect(composition) {
                            if (composition != null) {
                                progress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = composition!!.duration.toInt())
                                )
                                // Reset to first frame
                            }
                        }

                        LottieAnimation(
                            composition = composition,
                            progress = { progress.value },
                            modifier = Modifier.size(24.dp)
                        )
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
                            .padding(16.dp)
                            .road(whenCollapsed = Alignment.TopStart, whenExpanded = Alignment.Center),
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
                HorizontalPager(state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(5) {
                                ChatListItem(
                                    icon = R.drawable.iconalpha,
                                    name = "Vinaykpro $it",
                                    lastMessage = "Bye ra",
                                    lastSeen = "9:13 PM",
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }

            Row (modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(
                    color = Color(0xFF3DBFDC),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )) {
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


        }
    }
    // fake screen for dark mode
    if(revealed) {
        // The layout to reveal, clipped by a circle expanding from iconCenter
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = object : Shape {
                        override fun createOutline(
                            size: androidx.compose.ui.geometry.Size,
                            layoutDirection: LayoutDirection,
                            density: Density
                        ): Outline {
                            val path = Path().apply {
                                addOval(
                                    Rect(
                                        center = iconCenter,
                                        radius = radius.value
                                    )
                                )
                            }
                            return Outline.Generic(path)
                        }
                    }
                }
                .background(Color(0xFF000000))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // header
                CollapsingToolbarScaffold(modifier = Modifier.weight(1f),
                    state = state,
                    scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
                    toolbar = {

                        val textSize = (20 + 18 * state.toolbarState.progress).sp
                        val iconSize = (32 + 18 * state.toolbarState.progress).dp
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .pin()
                                .background(color = Color(0xFF1C1C1C))
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
                                    color = Color(0xFF1C1C1C),
                                    shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp)
                                ),
                            horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                fileSearch.launch("*/*")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }
                            IconButton(onClick = { /* Handle navigation click */ }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle ,
                                    contentDescription = "Search",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }
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
                                .padding(16.dp)
                                .road(whenCollapsed = Alignment.TopStart, whenExpanded = Alignment.Center),
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
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(5) {
                                ChatListItem(
                                    icon = R.drawable.iconalpha,
                                    name = "Vinaykpro $it",
                                    lastMessage = "Bye ra",
                                    lastSeen = "9:13 PM",
                                    navController = navController,
                                    isDark = true
                                )
                            }
                        }
                    }
                }

                Row (modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(
                        color = Color(0xFF1C1C1C),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = rememberRipple(
                                bounded = true,
                                color = Color(0xFF2D2D2D),
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
                                color = Color(0xFF1C1C1C),
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
            }
        }
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

@Composable
fun ChatListItem(
    icon: Int,
    name: String,
    lastMessage: String,
    lastSeen: String,
    navController: NavController,
    isDark: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = { navController.navigate("chat") })
            .padding(10.dp)
    ) {
        Image(painter = painterResource(id = R.drawable.user), contentDescription = "icon", modifier = Modifier.size(42.dp))
        Column(modifier = Modifier
            .padding(start = 10.dp)
            .weight(1f)) {
            Text(text = name, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(500), color = if(isDark) { Color.White } else { Color.Black} ))
            Text(text = lastMessage, style = TextStyle(fontSize = 13.sp, color = if(isDark) { Color.LightGray } else { Color.Gray}))
        }
        Text(text = lastSeen, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight(500), color = if(isDark) { Color.LightGray } else { Color.Gray}))
    }
}
