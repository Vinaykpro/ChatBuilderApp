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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.components.ActionIconItem
import com.vinaykpro.chatbuilder.ui.components.BannerAdView
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.ColorPicker
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.ParsedHeaderStyle
import com.vinaykpro.chatbuilder.ui.components.ProgressItem
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HeaderStyleScreen(
    navController: NavController = rememberNavController(),
    isDarkTheme: Boolean = false,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDark by remember { mutableStateOf(isDarkTheme) }
    val theme = LocalThemeEntity.current
    var themeStyle = remember(theme.headerstyle) {
        try {
            Json.decodeFromString<HeaderStyle>(theme.headerstyle)
        } catch (_: Exception) {
            HeaderStyle()
        }
    }

    val originalColors = remember(themeStyle) {
        mutableStateListOf(
            Color(themeStyle.color_navbar.toColorInt()),
            Color(themeStyle.color_navicons.toColorInt()),
            Color(themeStyle.color_text_primary.toColorInt()),
            Color(themeStyle.color_text_secondary.toColorInt()),
            Color(themeStyle.color_navbar_dark.toColorInt()),
            Color(themeStyle.color_navicons_dark.toColorInt()),
            Color(themeStyle.color_text_primary_dark.toColorInt()),
            Color(themeStyle.color_text_secondary_dark.toColorInt())
        )
    }

    var colors = remember(themeStyle) {
        mutableStateListOf(*originalColors.toTypedArray())
    }

    val previewColors by remember {
        derivedStateOf {
            ParsedHeaderStyle(
                navBar = colors[if (isDark) 4 else 0],
                navIcons = colors[if (isDark) 5 else 1],
                textPrimary = colors[if (isDark) 6 else 2],
                textSecondary = colors[if (isDark) 7 else 3]
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

    val iconNames = listOf(
        "ic_back.png",
        "ic_profile.png",
        "ic_3dots.png",
        "ic_nav1.png",
        "ic_nav2.png",
        "ic_nav3.png"
    )
    val refreshKeys = remember {
        mutableStateListOf(*IntArray(iconNames.size) { 0 }.toTypedArray())
    }
    var pickedIcon by remember { mutableIntStateOf(0) }
    val backIconPainter =
        rememberCustomIconPainter(theme.id, iconNames[0], refreshKeys[0], R.drawable.ic_back)
    val profilePicPainter =
        rememberCustomIconPainter(theme.id, iconNames[1], refreshKeys[1], R.drawable.user)
    val threeDotsPainter =
        rememberCustomIconPainter(theme.id, iconNames[2], refreshKeys[2], R.drawable.ic_more)
    val navIconPainters = listOf(
        rememberCustomIconPainter(theme.id, iconNames[3], refreshKeys[3], R.drawable.ic_call),
        rememberCustomIconPainter(theme.id, iconNames[4], refreshKeys[4], R.drawable.ic_videocall),
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
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            BasicToolbar(
                name = "Header Style", color = MaterialTheme.colorScheme.primary,
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
                            headerstyle = Json.encodeToString(
                                previewAttrs.copy(
                                    color_navbar = colorToHex(colors[0]),
                                    color_navicons = colorToHex(colors[1]),
                                    color_text_primary = colorToHex(colors[2]),
                                    color_text_secondary = colorToHex(colors[3]),
                                    color_navbar_dark = colorToHex(colors[4]),
                                    color_navicons_dark = colorToHex(colors[5]),
                                    color_text_primary_dark = colorToHex(colors[6]),
                                    color_text_secondary_dark = colorToHex(colors[7])
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
            ) {
                ChatToolbar(
                    preview = true,
                    previewColors = previewColors,
                    previewAttrs = previewAttrs,
                    backIcon = backIconPainter,
                    profileIcon = profilePicPainter,
                    icon1 = navIconPainters[0],
                    icon2 = navIconPainters[1],
                    icon3 = navIconPainters[2],
                    icon4 = threeDotsPainter,
                    onBackClick = {}
                )
                Spacer(modifier = Modifier.height(60.dp))
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
                        name = name, color = colors[if (isDark) 4 + i else i],
                        onClick = {
                            selectedColor = colors[if (isDark) 4 + i else i]
                            loadPicker = true
                            showColorPicker = true
                            pickedColorIndex = if (isDark) 4 + i else i
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

                SwitchItem(checked = previewAttrs.showbackbtn, onCheckChange = {
                    previewAttrs = previewAttrs.copy(showbackbtn = it)
                })
                if (previewAttrs.showbackbtn) {
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
                            ProgressItem(
                                name = "Icon size",
                                value = previewAttrs.backbtn_size.toFloat(),
                                min = 10f,
                                max = 40f,
                                onChange = {
                                    previewAttrs = previewAttrs.copy(backbtn_size = it)
                                })
                            ProgressItem(
                                name = "Left gap",
                                value = previewAttrs.backbtn_gap.toFloat(),
                                min = 0f,
                                max = 15f,
                                onChange = {
                                    previewAttrs = previewAttrs.copy(backbtn_gap = it)
                                })
                            EditIcon(name = "Back button icon", icon = backIconPainter, onClick = {
                                pickedIcon = 0
                                imagePicker.launch("image/*")
                            })
                        }
                    }
                }

                SwitchItem(
                    name = "Show profile pic",
                    context = "Show/hide the profile picture and customize.",
                    checked = previewAttrs.showprofilepic,
                    onCheckChange = {
                        previewAttrs = previewAttrs.copy(showprofilepic = it)
                    }
                )
                if (previewAttrs.showprofilepic) {
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
                            ProgressItem(
                                name = "Icon size",
                                value = previewAttrs.profilepic_size.toFloat(),
                                min = 20f,
                                max = 50f,
                                onChange = {
                                    previewAttrs = previewAttrs.copy(profilepic_size = it)
                                })
                            ProgressItem(
                                name = "Horizontal gap",
                                value = previewAttrs.profilepic_gap_sides.toFloat(),
                                min = 0f,
                                max = 10f,
                                onChange = {
                                    previewAttrs = previewAttrs.copy(profilepic_gap_sides = it)
                                })
//                        EditIcon(
//                            name = "Default profile icon",
//                            icon = profilePicPainter,
//                            filter = null,
//                            onClick = {
//                                pickedIcon = 1
//                                imagePicker.launch("image/*")
//                            })
                        }
                    }
                }

                SwitchItem(
                    name = "Show status/username",
                    context = "Show/hide the status as shown in the preview.",
                    checked = previewAttrs.showstatus,
                    onCheckChange = {
                        previewAttrs = previewAttrs.copy(showstatus = it)
                    }
                )

                SwitchItem(
                    enabled = false,
                    name = "Show three dots",
                    context = "You can only customize the icon."
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
                        EditIcon(name = "Three dots icon", icon = threeDotsPainter, onClick = {
                            pickedIcon = 2
                            imagePicker.launch("image/*")
                        })
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
                            iconSize = previewAttrs.actionicons_size,
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
                            iconSize = previewAttrs.actionicons_size,
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
                            iconSize = previewAttrs.actionicons_size,
                            previewAttrs.is_icon3_visible,
                            {
                                pickedIcon = 5
                                imagePicker.launch("image/*")
                            },
                            {
                                previewAttrs = previewAttrs.copy(is_icon3_visible = it)
                            }
                        )
                        ProgressItem(
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
                            })
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
            onClose = {
                showColorPicker = false
            }
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
    "Navbar Color",
    "NavIcons Color",
    "Name Text Color",
    "Status Text Color"
)

fun HeaderStyle.isSameAttrAs(other: HeaderStyle): Boolean {
    return this.showbackbtn == other.showbackbtn &&
            this.backbtn_size == other.backbtn_size &&
            this.backbtn_gap == other.backbtn_gap &&
            this.showprofilepic == other.showprofilepic &&
            this.profilepic_size == other.profilepic_size &&
            this.profilepic_gap_sides == other.profilepic_gap_sides &&
            this.showstatus == other.showstatus &&
            this.actionicons_gap == other.actionicons_gap &&
            this.actionicons_size == other.actionicons_size &&
            this.is_icon1_visible == other.is_icon1_visible &&
            this.is_icon2_visible == other.is_icon2_visible &&
            this.is_icon3_visible == other.is_icon3_visible
}

fun colorToHex(color: Color): String {
    return String.format("#%08X", color.toArgb())
}

