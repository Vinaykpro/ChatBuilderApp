package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
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
import kotlinx.serialization.json.Json

@Composable
fun BodyStyleScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false
) {
    var isDark by remember { mutableStateOf(isDarkTheme) }
    val theme = LocalThemeEntity.current
    var themeStyle = remember(theme.bodystyle) {
        try { Json.decodeFromString<BodyStyle>(theme.bodystyle) }
        catch(_: Exception) { BodyStyle() }
    }
    val appColor = remember(theme.appcolor) {
        Color(theme.appcolor.toColorInt())
    }

    val colors = remember(themeStyle) {
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

    val previewColors by remember {
        derivedStateOf {
            ParsedBodyStyle(
                chatBackground = colors[if(!isDark) 0 else 6],
                senderBubble = colors[if(!isDark) 1 else 7],
                receiverBubble = colors[if(!isDark) 2 else 8],
                dateBubble = colors[if(!isDark) 3 else 9],
                textPrimary = colors[if(!isDark) 4 else 10],
                textSecondary = colors[if(!isDark) 5 else 11]
            )
        }
    }

    var previewAttrs by remember {
        mutableStateOf(themeStyle)
    }


    var loadPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var pickedColorIndex by remember { mutableIntStateOf(0) }

    Box {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
            BasicToolbar(name = "Body Style", color = MaterialTheme.colorScheme.primary)

            var selectedBubbleStyle by remember { mutableIntStateOf(previewAttrs.bubble_style) }
            var bubbleRadius = remember { mutableFloatStateOf(previewAttrs.bubble_radius) }
            var bubbleTipRadius = remember { mutableFloatStateOf(previewAttrs.bubble_tip_radius) }

            val showTime = remember { mutableStateOf(previewAttrs.show_time) }
            val use12HrFormat = remember { mutableStateOf(previewAttrs.use12hr) }
            val showTicks = remember { mutableStateOf(previewAttrs.showticks) }
            val showReceiverPic = remember { mutableStateOf(previewAttrs.showreceiverpic) }

            previewAttrs = previewAttrs.copy(
                bubble_style = selectedBubbleStyle,
                bubble_radius = bubbleRadius.floatValue,
                bubble_tip_radius = bubbleTipRadius.floatValue,
                show_time = showTime.value,
                use12hr = use12HrFormat.value,
                showticks = showTicks.value,
                showreceiverpic = showReceiverPic.value
            )

            Column(
                modifier = Modifier.padding(top = 12.dp).padding(horizontal = 10.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
                    .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
                    .background(previewColors.chatBackground).padding(vertical = 5.dp, horizontal = 5.dp)
            ) {
                ChatNote("19 June 2025", color = previewColors.dateBubble, textColor = previewColors.textSecondary)
                SenderMessage(
                    text = "Hii",
                    bubbleStyle = selectedBubbleStyle,
                    bubbleRadius = bubbleRadius.floatValue,
                    bubbleTipRadius = bubbleTipRadius.floatValue,
                    color = previewColors.senderBubble,
                    textColor = previewColors.textPrimary,
                    hintTextColor = previewColors.textSecondary,
                    isFirst = true
                )
                SenderMessage(
                    text = "Hope you love using our app. Please leave a rating",
                    bubbleStyle = selectedBubbleStyle,
                    bubbleRadius = bubbleRadius.floatValue,
                    bubbleTipRadius = bubbleTipRadius.floatValue,
                    color = previewColors.senderBubble,
                    textColor = previewColors.textPrimary,
                    hintTextColor = previewColors.textSecondary,
                    isLast = true
                )
                Spacer(modifier = Modifier.size(4.dp))
                Message(
                    text = "Yep!",
                    bubbleStyle = selectedBubbleStyle,
                    bubbleRadius = bubbleRadius.floatValue,
                    bubbleTipRadius = bubbleTipRadius.floatValue,
                    color = previewColors.receiverBubble,
                    textColor = previewColors.textPrimary,
                    hintTextColor = previewColors.textSecondary,
                    isFirst = true
                )
                Message(
                    text = "Definitely :-)",
                    bubbleStyle = selectedBubbleStyle,
                    bubbleRadius = bubbleRadius.floatValue,
                    bubbleTipRadius = bubbleTipRadius.floatValue,
                    color = previewColors.receiverBubble,
                    textColor = previewColors.textPrimary,
                    hintTextColor = previewColors.textSecondary,
                    isLast = true
                )
            }
            Column(
                Modifier.padding(start = 18.dp, end = 10.dp).verticalScroll(rememberScrollState())
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
                    BubbleItem(
                        selected = selectedBubbleStyle == 0,
                        onClick = { selectedBubbleStyle = 0 },
                        selectedColor = appColor)
                    BubbleItem(
                        painterResource(R.drawable.ic_topbubble),
                        "Start bubble",
                        selected = selectedBubbleStyle == 1,
                        onClick = { selectedBubbleStyle = 1 },
                        selectedColor = appColor)
                    BubbleItem(
                        painterResource(R.drawable.ic_groupbubble),
                        "Group bubble",
                        selected = selectedBubbleStyle == 2,
                        onClick = { selectedBubbleStyle = 2 },
                        selectedColor = appColor)
                    BubbleItem(
                        painterResource(R.drawable.ic_endbubble),
                        "End bubble",
                        selected = selectedBubbleStyle == 3,
                        onClick = { selectedBubbleStyle = 3 },
                        selectedColor = appColor)
                }

                ProgressItem(name = "Bubble radius", max = 30f, progress = bubbleRadius)
                if (selectedBubbleStyle == 1) ProgressItem(
                    name = "Bubble tip radius",
                    max = 20f,
                    progress = bubbleTipRadius
                )

                Text(
                    text = "Colors:",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                bodyColorNames.forEachIndexed { i, name ->
                    ColorSelectionItem(name = name, color = colors[if(isDark) 6+i else i],
                        onClick = {
                            selectedColor = colors[if(isDark) 6+i else i]
                            loadPicker = true
                            showColorPicker = true
                            pickedColorIndex = if(isDark) 6+i else i
                        }
                    )
                }

                ColorSelectionItem(name = "Chat backgrund", color = MaterialTheme.colorScheme.primary)
                ColorSelectionItem(name = "Sender bubble color", color = Color.White)
                ColorSelectionItem(name = "Reciever bubble color", color = Color.Gray)
                ColorSelectionItem(name = "Date bubble color", color = Color.Gray)
                ColorSelectionItem(name = "Primary text color", color = Color.Gray)
                ColorSelectionItem(name = "Secondary text color", color = Color.Gray)


                Text(
                    text = "Widgets:",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )

                SwitchItem(
                    state = showTime,
                    name = "Show time inside message",
                    context = "Show/hide time inside message bubble"
                )
                SwitchItem(
                    state = use12HrFormat,
                    name = "Use 12hr format",
                    context = "Will show time in 12hr throughout the messages"
                )
                SwitchItem(
                    state = showTicks,
                    name = "Show ticks inside message",
                    context = "Show/hide ticks inside message bubble"
                )
                if (showTicks.value) {
                    Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                        Spacer(
                            modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            EditIcon(
                                name = "Seen ticks icon",
                                icon = painterResource(R.drawable.doubleticks)
                            )
                        }
                    }
                }
                SwitchItem(
                    state = showReceiverPic,
                    name = "Show receiver pic",
                    context = "Show/hide user picture"
                )
            }

        }
        if(loadPicker)
            AnimatedVisibility(visible = showColorPicker, enter = fadeIn(), exit = fadeOut()) {
                ColorPicker(
                    initialColor = selectedColor,
                    onColorPicked = {
                        colors[pickedColorIndex] = it
                    },
                    onClose = { showColorPicker = false }
                )
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
        modifier = Modifier.padding(end = 6.dp)
            .clip(shape = RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(if(selected) 2.dp else 1.dp, color = if(selected) selectedColor else MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if(selected) selectedColor else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(bottom = 5.dp).width(70.dp)
        )
        Text(text = name, color = if(selected) LightColorScheme.primary else MaterialTheme.colorScheme.secondaryContainer, fontWeight = FontWeight(if(selected) 500 else 400), fontSize = 13.sp, lineHeight = 14.sp)
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


@Preview
@Composable
fun BodyScreenPreview() {
    val theme = ThemeEntity()

    CompositionLocalProvider(LocalThemeEntity provides theme) {
        BodyStyleScreen()
    }
}