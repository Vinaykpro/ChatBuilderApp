package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ColorPicker
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.ParsedMessageBarStyle
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.serialization.json.Json

@Preview
@Composable
fun MessageBarStyleScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false
) {
    var isDark by remember { mutableStateOf(isDarkTheme) }

    val theme = LocalThemeEntity.current
    val themeStyle = remember(theme.messagebarstyle) {
        try { Json.decodeFromString<MessageBarStyle>(theme.messagebarstyle) }
        catch(_: Exception) { MessageBarStyle() }
    }

    val colors = remember(themeStyle) {
        mutableStateListOf(
            Color(themeStyle.color_widgetbackground.toColorInt()),
            Color(themeStyle.color_barbackground.toColorInt()),
            Color(themeStyle.color_outerbutton.toColorInt()),
            Color(themeStyle.color_outerbutton_icon.toColorInt()),
            Color(themeStyle.color_rightinnerbutton.toColorInt()),
            Color(themeStyle.color_rightinnerbutton_icon.toColorInt()),
            Color(themeStyle.color_leftinnerbutton.toColorInt()),
            Color(themeStyle.color_leftinnerbutton_icon.toColorInt()),
            Color(themeStyle.color_icons.toColorInt()),
            Color(themeStyle.color_inputtext.toColorInt()),
            Color(themeStyle.color_hinttext.toColorInt()),

            Color(themeStyle.color_widgetbackground_dark.toColorInt()),
            Color(themeStyle.color_barbackground_dark.toColorInt()),
            Color(themeStyle.color_outerbutton_dark.toColorInt()),
            Color(themeStyle.color_outerbutton_icon_dark.toColorInt()),
            Color(themeStyle.color_rightinnerbutton_dark.toColorInt()),
            Color(themeStyle.color_rightinnerbutton_icon_dark.toColorInt()),
            Color(themeStyle.color_leftinnerbutton_dark.toColorInt()),
            Color(themeStyle.color_leftinnerbutton_icon_dark.toColorInt()),
            Color(themeStyle.color_icons_dark.toColorInt()),
            Color(themeStyle.color_inputtext_dark.toColorInt()),
            Color(themeStyle.color_hinttext_dark.toColorInt()),
        )
    }

    val previewColors by remember {
        derivedStateOf {
            ParsedMessageBarStyle(
                widgetBackground = colors[if(isDark) 11 else 0],
                barBackground = colors[if(isDark) 12 else 1],
                outerButton = colors[if(isDark) 13 else 2],
                outerButtonIcon = colors[if(isDark) 14 else 3],
                rightInnerButton = colors[if(isDark) 15 else 4],
                rightInnerButtonIcon = colors[if(isDark) 16 else 5],
                leftInnerButton = colors[if(isDark) 17 else 6],
                leftInnerButtonIcon = colors[if(isDark) 18 else 7],
                colorIcons = colors[if(isDark) 19 else 8],
                inputText = colors[if(isDark) 20 else 9],
                hintText = colors[if(isDark) 21 else 10]
            )
        }
    }

    var previewAttrs by remember {
        mutableStateOf(themeStyle)
    }

    var loadPicker by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var pickedColorIndex by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
        BasicToolbar(name = "Message Bar Style")

        Column (modifier = Modifier.padding(top = 12.dp).padding(horizontal = 10.dp).clip(shape = RoundedCornerShape(12.dp))
            .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
            .background(Color(0xFFAA6D52))
            .padding(vertical = 5.dp)) {
            Spacer(modifier = Modifier.height(60.dp))
            ChatMessageBar(preview = true, previewColors = previewColors, previewAttrs = previewAttrs)
        }

        Column(Modifier.padding(start = 18.dp, end = 10.dp).verticalScroll(rememberScrollState())) {
            SelectModeWidget(isDark = isDark, onUpdate = { isDark = it })


            Text(text = "Colors:",fontSize = 17.sp,fontWeight = FontWeight(500),color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))

            headerColorNames.forEachIndexed { i, name ->
                ColorSelectionItem(name = name, color = colors[if(isDark) 11+i else i],
                    onClick = {
                        selectedColor = colors[if(isDark) 11+i else i]
                        loadPicker = true
                        showColorPicker = true
                        pickedColorIndex = if(isDark) 11+i else i
                    }
                )
            }

            Text(text = "Widgets:",fontSize = 17.sp,fontWeight = FontWeight(500),color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            val showOuterBtn = remember { mutableStateOf(true) }
            val showLeftInnerBtn = remember { mutableStateOf(true) }
            val showRightInnerBtn = remember { mutableStateOf(false) }

            previewAttrs = previewAttrs.copy(showouterbutton = showOuterBtn.value, showleftinnerbutton = showLeftInnerBtn.value, showrightinnerbutton = showRightInnerBtn.value)

            SwitchItem(state = showOuterBtn, name = "Show outer action button",  context = "Show/hide time inside message bubble")
            if (showOuterBtn.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Seen ticks icon", icon = painterResource(R.drawable.ic_videocall))
                    }
                }
            }
            SwitchItem(state = showLeftInnerBtn, name = "Show left inner action button",  context = "Will show time in 12hr throughout the messages")
            if (showLeftInnerBtn.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Seen ticks icon", icon = painterResource(R.drawable.ic_call))
                    }
                }
            }
            SwitchItem(state = showRightInnerBtn, name = "Show right inner action button",  context = "Show/hide ticks inside message bubble")
            if (showRightInnerBtn.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Seen ticks icon", icon = painterResource(R.drawable.doubleticks))
                    }
                }
            }
        }

    }
    if(loadPicker) {
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

private val headerColorNames = listOf(
    "Widget background",
    "Message bar background",
    "Outer button background",
    "Outer button icon",
    "Right inner button background",
    "Right inner button icon",
    "Left inner button background",
    "Left inner button icon",
    "Action Icons color",
    "Input text color",
    "Hint text color",
)