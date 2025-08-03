package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
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
import com.vinaykpro.chatbuilder.ui.components.ActionIconItem
import com.vinaykpro.chatbuilder.ui.components.BannerAdView
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ColorPicker
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.ParsedMessageBarStyle
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
fun MessageBarStyleScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    val iconNames = listOf(
        "ic_outer_icon.png",
        "ic_left_inner_icon.png",
        "ic_right_inner_icon.png",
        "ic_bottom_nav1.png",
        "ic_bottom_nav2.png",
        "ic_bottom_nav3.png"
    )
    val refreshKeys = remember {
        mutableStateListOf(*IntArray(iconNames.size) { 0 }.toTypedArray())
    }
    var pickedIcon by remember { mutableIntStateOf(0) }
    val outerIconPainter =
        rememberCustomIconPainter(theme.id, iconNames[0], refreshKeys[0], R.drawable.ic_send)
    val leftInnerIconPainter =
        rememberCustomIconPainter(theme.id, iconNames[1], refreshKeys[1], R.drawable.ic_emoji)
    val rightInnerIconPainter =
        rememberCustomIconPainter(theme.id, iconNames[2], refreshKeys[2], R.drawable.ic_send)
    val navIconPainters = listOf(
        rememberCustomIconPainter(theme.id, iconNames[3], refreshKeys[3], R.drawable.ic_file),
        rememberCustomIconPainter(theme.id, iconNames[4], refreshKeys[4], R.drawable.ic_camera),
        rememberCustomIconPainter(theme.id, iconNames[5], refreshKeys[5], R.drawable.ic_animate)
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    themeViewModel.saveCustomIcon(
                        uri,
                        context,
                        theme.id,
                        iconNames[pickedIcon],
                        onDone = {
                            refreshKeys[pickedIcon]++
                        })
                }
            }
        }
    )

    Column(
        modifier = Modifier.padding(
            bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .weight(1f)
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
                    .background(Color(0xFFAA6D52))
                    .padding(vertical = 5.dp)
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                ChatMessageBar(
                    preview = true,
                    previewColors = previewColors,
                    previewAttrs = previewAttrs,
                    outerIcon = outerIconPainter,
                    leftInnerIcon = leftInnerIconPainter,
                    rightInnerIcon = rightInnerIconPainter,
                    icon1 = navIconPainters[0],
                    icon2 = navIconPainters[1],
                    icon3 = navIconPainters[2]
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
                                name = "Outer button icon",
                                icon = outerIconPainter,
                                iconSize = 24,
                                onClick = {
                                    pickedIcon = 0
                                    imagePicker.launch("image/*")
                                }
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
                                name = "Left inner button icon",
                                icon = leftInnerIconPainter,
                                iconSize = 24,
                                onClick = {
                                    pickedIcon = 1
                                    imagePicker.launch("image/*")
                                }
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
                                name = "Right inner button icon",
                                icon = rightInnerIconPainter,
                                iconSize = 22,
                                onClick = {
                                    pickedIcon = 2
                                    imagePicker.launch("image/*")
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Action icons:",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
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
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ActionIconItem(
                            "Icon 1",
                            navIconPainters[0],
                            iconSize = 22,
                            previewAttrs.is_icon1_visible,
                            {
                                pickedIcon = 3
                                imagePicker.launch("image/*")
                            },
                            {
                                previewAttrs = previewAttrs.copy(is_icon1_visible = it)
                            }
                        )
                        ActionIconItem(
                            "Icon 2",
                            navIconPainters[1],
                            iconSize = 22,
                            previewAttrs.is_icon2_visible,
                            {
                                pickedIcon = 4
                                imagePicker.launch("image/*")
                            },
                            {
                                previewAttrs = previewAttrs.copy(is_icon2_visible = it)
                            }
                        )
                        ActionIconItem(
                            "Icon 3",
                            navIconPainters[2],
                            iconSize = 22,
                            previewAttrs.is_icon3_visible,
                            {
                                pickedIcon = 5
                                imagePicker.launch("image/*")
                            },
                            {
                                previewAttrs = previewAttrs.copy(is_icon3_visible = it)
                            }
                        )
                        /*ProgressItem(
                        name = "Icon size",
                        value = previewAttrs.actionicons_size.toFloat(),
                        min = 15f,
                        max = 35f,
                        onChange = {
                            previewAttrs = previewAttrs.copy(actionicons_size = it)
                        })
                    ProgressItem(
                        name = "Horizontal gap",
                        value = previewAttrs.actionicons_gap.toFloat(),
                        min = 0f,
                        max = 15f,
                        onChange = {
                            previewAttrs = previewAttrs.copy(actionicons_gap = it)
                        })*/
                    }
                }

            }

        }
        BannerAdView(adId = "ca-app-pub-2813592783630195/8283590134")
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
            is_icon1_visible == other.is_icon1_visible &&
            is_icon2_visible == other.is_icon2_visible &&
            is_icon3_visible == other.is_icon3_visible
}

fun copyPainter(original: Painter): Painter = object : Painter() {
    override val intrinsicSize = Size.Unspecified
    override fun DrawScope.onDraw() {
        with(original) { draw(size = this@onDraw.size) }
    }
}