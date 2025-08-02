package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatNote
import com.vinaykpro.chatbuilder.ui.components.ColorPicker
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.ProgressItem
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.screens.chat.ParsedBodyStyle
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.BodyStyleScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    var isDark by remember { mutableStateOf(isDarkTheme) }
    val theme = LocalThemeEntity.current
    var themeStyle = remember(theme.bodystyle) {
        try {
            Json.decodeFromString<BodyStyle>(theme.bodystyle)
        } catch (_: Exception) {
            BodyStyle()
        }
    }

    val originalColors = remember(themeStyle) {
        mutableStateListOf(
            Color(themeStyle.color_chatbackground.toColorInt()),
            Color(themeStyle.color_senderbubble.toColorInt()),
            Color(themeStyle.color_receiverbubble.toColorInt()),
            Color(themeStyle.color_datebubble.toColorInt()),
            Color(themeStyle.color_text_primary.toColorInt()),
            Color(themeStyle.color_text_secondary.toColorInt()),
            Color(themeStyle.color_chatbackground_dark.toColorInt()),
            Color(themeStyle.color_senderbubble_dark.toColorInt()),
            Color(themeStyle.color_receiverbubble_dark.toColorInt()),
            Color(themeStyle.color_datebubble_dark.toColorInt()),
            Color(themeStyle.color_text_primary_dark.toColorInt()),
            Color(themeStyle.color_text_secondary_dark.toColorInt())
        )
    }

    var colors = remember(themeStyle) {
        mutableStateListOf(*originalColors.toTypedArray())
    }

    val previewColors by remember {
        derivedStateOf {
            ParsedBodyStyle(
                chatBackground = colors[if (!isDark) 0 else 6],
                senderBubble = colors[if (!isDark) 1 else 7],
                receiverBubble = colors[if (!isDark) 2 else 8],
                dateBubble = colors[if (!isDark) 3 else 9],
                textPrimary = colors[if (!isDark) 4 else 10],
                textSecondary = colors[if (!isDark) 5 else 11]
            )
        }
    }

    var previewAttrs by remember {
        mutableStateOf(themeStyle)
    }

    val isAttrsChanged by remember(themeStyle, previewAttrs) {
        derivedStateOf { !themeStyle.isSameAttrAs(previewAttrs) }
    }
    var isColorsChanged by remember { mutableStateOf(false) }


    var loadPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var pickedColorIndex by remember { mutableIntStateOf(0) }

    var refreshKey by remember { mutableIntStateOf(0) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    themeViewModel.saveCustomIcon(
                        uri,
                        context,
                        theme.id,
                        "ic_ticks_seen.png",
                        onDone = {
                            refreshKey++
                        })
                }
            }
        }
    )

    val seenTicksPainter =
        rememberCustomIconPainter(theme.id, "ic_ticks_seen.png", refreshKey, R.drawable.doubleticks)

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        BasicToolbar(
            name = "Body Style", color = MaterialTheme.colorScheme.primary,
            icon1 = if (isAttrsChanged || isColorsChanged) painterResource(R.drawable.ic_close) else null,
            onIcon1Click = {
                // Discard changes
                isColorsChanged = false
                previewAttrs = themeStyle
                originalColors.forEachIndexed { i, orig ->
                    colors[i] = orig
                }
            },
            icon2 = if (isAttrsChanged || isColorsChanged) painterResource(R.drawable.ic_tick) else null,
            onIcon2Click = {
                // Save changes
                isColorsChanged = false
                themeViewModel.updateTheme(
                    theme.copy(
                        bodystyle = Json.encodeToString(
                            previewAttrs.copy(
                                color_chatbackground = colorToHex(colors[0]),
                                color_senderbubble = colorToHex(colors[1]),
                                color_receiverbubble = colorToHex(colors[2]),
                                color_datebubble = colorToHex(colors[3]),
                                color_text_primary = colorToHex(colors[4]),
                                color_text_secondary = colorToHex(colors[5]),
                                color_chatbackground_dark = colorToHex(colors[6]),
                                color_senderbubble_dark = colorToHex(colors[7]),
                                color_receiverbubble_dark = colorToHex(colors[8]),
                                color_datebubble_dark = colorToHex(colors[9]),
                                color_text_primary_dark = colorToHex(colors[10]),
                                color_text_secondary_dark = colorToHex(colors[11]),
                            )
                        )
                    )
                )
            },
            onBackClick = {
                navController.popBackStack()
            })

        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 10.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
                .background(previewColors.chatBackground)
                .padding(vertical = 5.dp, horizontal = 5.dp)
        ) {
            ChatNote(
                "19 June 2025",
                color = previewColors.dateBubble,
                textColor = previewColors.textSecondary
            )
            SenderMessage(
                text = "Hii",
                color = previewColors.senderBubble,
                textColor = previewColors.textPrimary,
                textColorSecondary = previewColors.textSecondary,
                ticksIcon = seenTicksPainter,
                bubbleStyle = previewAttrs.bubble_style,
                bubbleRadius = previewAttrs.bubble_radius.toFloat(),
                bubbleTipRadius = previewAttrs.bubble_tip_radius.toFloat(),
                screenWidthDp = screenWidthDp,
                isFirst = true,
                showTime = previewAttrs.show_time,
                showTicks = previewAttrs.showticks,
                searchedString = null
            )
            SenderMessage(
                text = "Hope you love using our app. Please leave a rating",
                color = previewColors.senderBubble,
                textColor = previewColors.textPrimary,
                textColorSecondary = previewColors.textSecondary,
                ticksIcon = seenTicksPainter,
                bubbleStyle = previewAttrs.bubble_style,
                bubbleRadius = previewAttrs.bubble_radius.toFloat(),
                bubbleTipRadius = previewAttrs.bubble_tip_radius.toFloat(),
                screenWidthDp = screenWidthDp,
                isLast = true,
                showTime = previewAttrs.show_time,
                showTicks = previewAttrs.showticks,
                searchedString = null
            )
            Spacer(modifier = Modifier.size(4.dp))
            Message(
                text = "Yep!",
                color = previewColors.receiverBubble,
                textColor = previewColors.textPrimary,
                textColorSecondary = previewColors.textSecondary,
                bubbleStyle = previewAttrs.bubble_style,
                bubbleRadius = previewAttrs.bubble_radius.toFloat(),
                bubbleTipRadius = previewAttrs.bubble_tip_radius.toFloat(),
                screenWidthDp = screenWidthDp,
                isFirst = true,
                showTime = previewAttrs.show_time,
                searchedString = null
            )
            Message(
                text = "Definitely :-)",
                color = previewColors.receiverBubble,
                textColor = previewColors.textPrimary,
                textColorSecondary = previewColors.textSecondary,
                bubbleStyle = previewAttrs.bubble_style,
                bubbleRadius = previewAttrs.bubble_radius.toFloat(),
                bubbleTipRadius = previewAttrs.bubble_tip_radius.toFloat(),
                screenWidthDp = screenWidthDp,
                isLast = true,
                showTime = previewAttrs.show_time,
                searchedString = null
            )
        }
        Column(
            Modifier
                .padding(start = 18.dp, end = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectModeWidget(isDark = isDark, onUpdate = { isDark = it })

            Text(
                text = "Chat bubble style:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 14.dp)
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                bubbleStyles.forEachIndexed { i, (name, icon) ->
                    BubbleItem(
                        icon = painterResource(icon),
                        name = name,
                        selected = previewAttrs.bubble_style == i,
                        onClick = { previewAttrs = previewAttrs.copy(bubble_style = i) },
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            ProgressItem(
                name = "Bubble radius", max = 30f, value = previewAttrs.bubble_radius,
                onChange = { previewAttrs = previewAttrs.copy(bubble_radius = it.toFloat()) })
            if (previewAttrs.bubble_style == 1)
                ProgressItem(
                    name = "Bubble tip radius",
                    max = 20f,
                    value = previewAttrs.bubble_tip_radius,
                    onChange = {
                        previewAttrs = previewAttrs.copy(bubble_tip_radius = it.toFloat())
                    }
                )


            Text(
                text = "Colors:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )
            bodyColorNames.forEachIndexed { i, name ->
                ColorSelectionItem(
                    name = name, color = colors[if (isDark) 6 + i else i],
                    onClick = {
                        selectedColor = colors[if (isDark) 6 + i else i]
                        loadPicker = true
                        showColorPicker = true
                        pickedColorIndex = if (isDark) 6 + i else i
                    }
                )
            }


            Text(
                text = "Widgets:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            SwitchItem(
                name = "Show time inside message",
                context = "Show/hide time inside message bubble",
                checked = previewAttrs.show_time,
                onCheckChange = {
                    previewAttrs = previewAttrs.copy(show_time = it)
                }
            )
            SwitchItem(
                name = "Use 12hr format",
                context = "Will show time in 12hr throughout the messages",
                checked = previewAttrs.use12hr,
                onCheckChange = {
                    previewAttrs = previewAttrs.copy(use12hr = it)
                }
            )
            SwitchItem(
                name = "Show ticks inside message",
                context = "Show/hide ticks inside message bubble",
                checked = previewAttrs.showticks,
                onCheckChange = {
                    previewAttrs = previewAttrs.copy(showticks = it)
                }
            )
            if (previewAttrs.showticks) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .height(IntrinsicSize.Min)
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                            .width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(
                            name = "Seen ticks icon",
                            icon = seenTicksPainter,
                            filter = null,
                            onClick = {
                                imagePicker.launch("image/*")
                            }
                        )
                    }
                }
            }
            SwitchItem(
                name = "Show receiver pic",
                context = "Show/hide user picture",
                checked = previewAttrs.showreceiverpic,
                onCheckChange = {
                    previewAttrs = previewAttrs.copy(showreceiverpic = it)
                }
            )
        }

    }
    AnimatedVisibility(visible = showColorPicker, enter = fadeIn(), exit = fadeOut()) {
        ColorPicker(
            initialColor = selectedColor,
            onColorPicked = {
                colors[pickedColorIndex] = it
                isColorsChanged = true
            },
            onClose = { showColorPicker = false }
        )
    }
    BackHandler {
        if (showColorPicker) {
            showColorPicker = false
        } else {
            navController.popBackStack()
        }
    }
}


@Composable
fun BubbleItem(
    icon: Painter = painterResource(R.drawable.ic_nobubble),
    name: String = "No bubble",
    selected: Boolean = false,
    selectedColor: Color = LightColorScheme.primary,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(end = 6.dp)
            .clip(shape = RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(
                if (selected) 2.dp else 1.dp,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if (selected) selectedColor else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(bottom = 5.dp)
                .width(70.dp)
        )
        Text(
            text = name,
            color = if (selected) LightColorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            fontWeight = FontWeight(if (selected) 500 else 400),
            fontSize = 13.sp,
            lineHeight = 14.sp
        )
    }
}

private val bodyColorNames = listOf(
    "Chat background",
    "Sender bubble color",
    "Reciever bubble color",
    "Date bubble color",
    "Primary text color",
    "Secondary text color"
)

fun BodyStyle.isSameAttrAs(other: BodyStyle): Boolean {
    return this.bubble_style == other.bubble_style &&
            this.bubble_radius == other.bubble_radius &&
            this.bubble_tip_radius == other.bubble_tip_radius &&
            this.show_time == other.show_time &&
            this.use12hr == other.use12hr &&
            this.showticks == other.showticks &&
            this.showreceiverpic == other.showreceiverpic
}

private val bubbleStyles = listOf(
    "No bubble" to R.drawable.ic_nobubble,
    "Start bubble" to R.drawable.ic_topbubble,
    "Group bubble" to R.drawable.ic_groupbubble,
    "End bubble" to R.drawable.ic_endbubble,
)