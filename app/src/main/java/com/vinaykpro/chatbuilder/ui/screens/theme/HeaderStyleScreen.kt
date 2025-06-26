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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.ThemeEntity
import com.vinaykpro.chatbuilder.ui.components.ActionIcons
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
import kotlinx.serialization.json.Json

@Composable
fun HeaderStyleScreen() {
    var isDark by remember { mutableStateOf(false) }
    val theme = LocalThemeEntity.current
    val themeStyle = remember(theme.headerstyle) {
        try { Json.decodeFromString<HeaderStyle>(theme.headerstyle) }
            catch(_: Exception) { HeaderStyle() }
    }
    val appColor = remember(theme.appcolor) {
        Color(theme.appcolor.toColorInt())
    }

    val colors = remember(themeStyle) {
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


    var loadPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var pickedColorIndex by remember { mutableIntStateOf(0) }

    Box {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            BasicToolbar(name = "Header Style", color = appColor)
            Column(
                modifier = Modifier.padding(top = 12.dp).padding(horizontal = 10.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
                    .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
            ) {
                ChatToolbar(preview = true, previewColors = previewColors, previewAttrs = previewAttrs)
                Spacer(modifier = Modifier.height(60.dp))
            }
            Column(
                Modifier.padding(start = 18.dp, end = 10.dp).verticalScroll(rememberScrollState())
            ) {
                SelectModeWidget(onUpdate = { isDark = it })

                Text(
                    text = "Colors:",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                headerColorNames.forEachIndexed { i, name ->
                    ColorSelectionItem(name = name, color = colors[if(isDark) 4+i else i],
                        onClick = {
                            selectedColor = colors[if(isDark) 4+i else i]
                            loadPicker = true
                            showColorPicker = true
                            pickedColorIndex = if(isDark) 4+i else i
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
                val backBtnState = remember { mutableStateOf(true) }
                val profilePicState = remember { mutableStateOf(true) }
                val showStatusState = remember { mutableStateOf(true) }

                SwitchItem(state = backBtnState)
                previewAttrs = previewAttrs.copy(showbackbtn = backBtnState.value)
                if (backBtnState.value) {
                    Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                        Spacer(
                            modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            ProgressItem(name = "Icon size", value = themeStyle.backbtn_size.toFloat(), min = 10f, max = 40f,
                                onChange = {
                                previewAttrs = previewAttrs.copy(backbtn_size = it)
                            })
                            ProgressItem(name = "Left gap", value = themeStyle.backbtn_gap.toFloat(), min = 0f, max = 15f,
                                onChange = {
                                previewAttrs = previewAttrs.copy(backbtn_gap = it)
                            })
                            EditIcon(name = "Back button icon")
                        }
                    }
                }

                SwitchItem(
                    state = profilePicState,
                    name = "Show profile pic",
                    context = "Show/hide the profile picture and customize."
                )
                previewAttrs = previewAttrs.copy(showprofilepic = profilePicState.value)
                if (profilePicState.value) {
                    Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                        Spacer(
                            modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            ProgressItem(name = "Icon size", value = themeStyle.profilepic_size.toFloat(), min = 20f, max = 50f,
                                onChange = {
                                    previewAttrs = previewAttrs.copy(profilepic_size = it)
                                })
                            ProgressItem(name = "Horizontal gap", value = themeStyle.profilepic_gap_sides.toFloat(), min = 0f, max = 10f,
                                onChange = {
                                    previewAttrs = previewAttrs.copy(profilepic_gap_sides = it)
                                })
                            EditIcon(name = "Three dots icon")
                        }
                    }
                }

                SwitchItem(
                    state = showStatusState,
                    name = "Show status/username",
                    context = "Show/hide the status as shown in the preview."
                )
                previewAttrs = previewAttrs.copy(showstatus = showStatusState.value)

                SwitchItem(
                    enabled = false,
                    name = "Show three dots",
                    context = "You can only customize the icon."
                )
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Default profile icon")
                    }
                }

                Text(
                    text = "Action icons:",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ActionIcons()
                        ProgressItem(name = "Icon size", value = themeStyle.actionicons_size.toFloat(), min = 15f, max = 35f,
                            onChange = {
                                previewAttrs = previewAttrs.copy(actionicons_size = it)
                            })
                        ProgressItem(name = "Horizontal gap", value = themeStyle.actionicons_gap.toFloat(), min = 0f, max = 15f,
                            onChange = {
                                previewAttrs = previewAttrs.copy(actionicons_gap = it)
                            })
                    }
                }
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

private val headerColorNames = listOf(
    "Navbar Color",
    "NavIcons Color",
    "Name Text Color",
    "Status Text Color"
)

@Preview
@Composable
fun MyScreenPreview() {
    val theme = ThemeEntity()

    CompositionLocalProvider(LocalThemeEntity provides theme) {
        HeaderStyleScreen()
    }
}