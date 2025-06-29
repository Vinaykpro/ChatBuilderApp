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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ColorPicker
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.ParsedMessageBarStyle
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
fun MessageBarStyleScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false,
    themeViewModel: ThemeViewModel
) {
    var isDark by remember { mutableStateOf(isDarkTheme) }

    val theme = LocalThemeEntity.current
    val themeStyle = remember(theme.messagebarstyle) {
        try {
            Json.decodeFromString<MessageBarStyle>(theme.messagebarstyle)
        } catch (_: Exception) {
            MessageBarStyle()
        }
    }

    val originalColors = remember(themeStyle) {
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

    var colors = remember(themeStyle) {
        mutableStateListOf(*originalColors.toTypedArray())
    }

    val previewColors by remember {
        derivedStateOf {
            ParsedMessageBarStyle(
                widgetBackground = colors[if (isDark) 11 else 0],
                barBackground = colors[if (isDark) 12 else 1],
                outerButton = colors[if (isDark) 13 else 2],
                outerButtonIcon = colors[if (isDark) 14 else 3],
                rightInnerButton = colors[if (isDark) 15 else 4],
                rightInnerButtonIcon = colors[if (isDark) 16 else 5],
                leftInnerButton = colors[if (isDark) 17 else 6],
                leftInnerButtonIcon = colors[if (isDark) 18 else 7],
                colorIcons = colors[if (isDark) 19 else 8],
                inputText = colors[if (isDark) 20 else 9],
                hintText = colors[if (isDark) 21 else 10]
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

    var loadPicker by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var pickedColorIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        BasicToolbar(
            name = "Message Bar Style", color = MaterialTheme.colorScheme.primary,
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
                        messagebarstyle = Json.encodeToString(
                            previewAttrs.copy(
                                color_widgetbackground = colorToHex(colors[0]),
                                color_barbackground = colorToHex(colors[1]),
                                color_outerbutton = colorToHex(colors[2]),
                                color_outerbutton_icon = colorToHex(colors[3]),
                                color_rightinnerbutton = colorToHex(colors[4]),
                                color_rightinnerbutton_icon = colorToHex(colors[5]),
                                color_leftinnerbutton = colorToHex(colors[6]),
                                color_leftinnerbutton_icon = colorToHex(colors[7]),
                                color_icons = colorToHex(colors[8]),
                                color_inputtext = colorToHex(colors[9]),
                                color_hinttext = colorToHex(colors[10]),
                                color_widgetbackground_dark = colorToHex(colors[11]),
                                color_barbackground_dark = colorToHex(colors[12]),
                                color_outerbutton_dark = colorToHex(colors[13]),
                                color_outerbutton_icon_dark = colorToHex(colors[14]),
                                color_rightinnerbutton_dark = colorToHex(colors[15]),
                                color_rightinnerbutton_icon_dark = colorToHex(colors[16]),
                                color_leftinnerbutton_dark = colorToHex(colors[17]),
                                color_leftinnerbutton_icon_dark = colorToHex(colors[18]),
                                color_icons_dark = colorToHex(colors[19]),
                                color_inputtext_dark = colorToHex(colors[20]),
                                color_hinttext_dark = colorToHex(colors[21]),
                            )
                        )
                    )
                )
            })

        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 10.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
                .background(Color(0xFFAA6D52))
                .padding(vertical = 5.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            ChatMessageBar(
                preview = true,
                previewColors = previewColors,
                previewAttrs = previewAttrs
            )
        }

        Column(
            Modifier
                .padding(start = 18.dp, end = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectModeWidget(isDark = isDark, onUpdate = { isDark = it })


            Text(
                text = "Colors:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )

            headerColorNames.forEachIndexed { i, name ->
                ColorSelectionItem(
                    name = name, color = colors[if (isDark) 11 + i else i],
                    onClick = {
                        selectedColor = colors[if (isDark) 11 + i else i]
                        loadPicker = true
                        showColorPicker = true
                        pickedColorIndex = if (isDark) 11 + i else i
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
                name = "Show outer action button",
                context = "Show/hide outer action button",
                checked = previewAttrs.showouterbutton, onCheckChange = {
                    previewAttrs = previewAttrs.copy(showouterbutton = it)
                }
            )
            if (previewAttrs.showouterbutton) {
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
                            icon = painterResource(R.drawable.ic_videocall)
                        )
                    }
                }
            }
            SwitchItem(
                name = "Show left inner action button",
                context = "Show/hide left inner button",
                checked = previewAttrs.showleftinnerbutton, onCheckChange = {
                    previewAttrs = previewAttrs.copy(showleftinnerbutton = it)
                }
            )
            if (previewAttrs.showleftinnerbutton) {
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
                            icon = painterResource(R.drawable.ic_call)
                        )
                    }
                }
            }
            SwitchItem(
                name = "Show right inner action button",
                context = "Show/hide ticks inside message bubble",
                checked = previewAttrs.showrightinnerbutton, onCheckChange = {
                    previewAttrs = previewAttrs.copy(showrightinnerbutton = it)
                }
            )
            if (previewAttrs.showrightinnerbutton) {
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
                            icon = painterResource(R.drawable.doubleticks)
                        )
                    }
                }
            }
        }

    }
    if (loadPicker) {
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

fun MessageBarStyle.isSameAttrAs(other: MessageBarStyle): Boolean {
    return showleftinnerbutton == other.showleftinnerbutton &&
            showrightinnerbutton == other.showrightinnerbutton &&
            showouterbutton == other.showouterbutton &&
            leftinnerbutton_icon == other.leftinnerbutton_icon &&
            rightinnerbutton_icon == other.rightinnerbutton_icon &&
            outerbutton_icon == other.outerbutton_icon &&
            actionicons_order == other.actionicons_order &&
            is_icon1_visible == other.is_icon1_visible &&
            is_icon2_visible == other.is_icon2_visible &&
            is_icon3_visible == other.is_icon3_visible &&
            icon1 == other.icon1 &&
            icon2 == other.icon2 &&
            icon3 == other.icon3
}