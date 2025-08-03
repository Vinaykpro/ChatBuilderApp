package com.vinaykpro.chatbuilder.ui.screens.theme

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler
import com.vinaykpro.chatbuilder.ui.components.BannerAdView
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ColorPicker
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.Input
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun EditThemeScreen(
    themename: String = "Default theme",
    navController: NavController = rememberNavController(),
    themeViewModel: ThemeViewModel,
    isDark: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var theme = LocalThemeEntity.current
    var appColor by remember(theme.appcolor) { mutableStateOf(Color(theme.appcolor.toColorInt())) }
    var appColorDark by remember(theme.appcolordark) { mutableStateOf(Color(theme.appcolordark.toColorInt())) }

    var loadPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(appColor) }
    var isPickingColorDark by remember { mutableStateOf(false) }

    var refreshIconKey by remember { mutableIntStateOf(0) }
    val themeIcon = rememberCustomIconPainter(theme.id, "icon.png", refreshIconKey, R.drawable.logo)
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    themeViewModel.saveCustomIcon(uri, context, theme.id, "icon.png", onDone = {
                        refreshIconKey++
                    })
                }
            }
        }
    )
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            BasicToolbar(
                name = theme.name,
                color = if (isDark) appColorDark else appColor,
                icon1 = if (theme.id != 1) painterResource(R.drawable.ic_delete) else null,
                icon2 = painterResource(R.drawable.ic_export),
                onIcon1Click = {
                    themeViewModel.deleteTheme(context, onDone = {
                        navController.popBackStack()
                    })
                },
                onIcon2Click = {
                    themeViewModel.exportTheme(context, onDone = {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            it
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share theme zip"))
                    })
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp)
            ) {
                Text(
                    text = "Theme properties",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 18.dp)
                )
                Row(modifier = Modifier.padding(start = 6.dp)) {
                    EditIcon(
                        size = 80, color = appColor,
                        iconSize = 60,
                        icon = themeIcon,
                        onClick = {
                            imagePicker.launch("image/*")
                        }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Input(value = theme.name, onUpdate = {
                            if (theme.name != it) {
                                themeViewModel.updateTheme(theme.copy(name = it))
                            }
                        })
                        Input(name = "Made by:", value = theme.author, onUpdate = {
                            if (theme.author != it) {
                                themeViewModel.updateTheme(theme.copy(name = it))
                            }
                        })
                    }
                }
                Text(
                    text = "App Style",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
                )
                ColorSelectionItem(
                    name = "App Primary Color", color = appColor,
                    onClick = {
                        selectedColor = appColor
                        isPickingColorDark = false
                        loadPicker = true
                        showColorPicker = true
                    })
                ColorSelectionItem(
                    name = "App Primary Color (Dark)", color = appColorDark,
                    onClick = {
                        selectedColor = appColorDark
                        isPickingColorDark = true
                        loadPicker = true
                        showColorPicker = true
                    })
                Text(
                    text = "Chat Screen Styles",
                    fontSize = 17.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 18.dp, bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 18.dp)
                        .clip(shape = RoundedCornerShape(15.dp))
                        .background(if (isDark) Color(0x0FFFFFFF) else Color(0x0F7B7B7B))
                        .padding(vertical = 10.dp)
                ) {
                    StyleItem(onClick = { DebounceClickHandler.run { navController.navigate("headerstyle") } })
                    StyleItem(
                        icon = painterResource(R.drawable.ic_body),
                        name = "Body",
                        context = "Chat bubbles, background, widgets, etc.,",
                        onClick = {
                            DebounceClickHandler.run {
                                navController.navigate("bodystyle")
                            }
                        })
                    StyleItem(
                        icon = painterResource(R.drawable.ic_msgbar),
                        name = "Message Bar",
                        context = "Chat input, buttons, actions, etc.,",
                        onClick = {
                            DebounceClickHandler.run {
                                navController.navigate("barstyle")
                            }
                        })
                }
            }
            BannerAdView(adId = "ca-app-pub-2813592783630195/8283590134")
        }

        AnimatedVisibility(visible = showColorPicker, enter = fadeIn(), exit = fadeOut()) {
            ColorPicker(
                initialColor = selectedColor,
                onColorPicked = {
                    if (isPickingColorDark)
                        appColorDark = it
                    else
                        appColor = it
                },
                onClose = {
                    showColorPicker = false
                    themeViewModel.updateTheme(
                        theme.copy(
                            appcolor = String.format("#%08X", appColor.toArgb()),
                            appcolordark = String.format("#%08X", appColorDark.toArgb())
                        )
                    )
                }
            )
        }
    }

    BackHandler {
        if (showColorPicker) {
            showColorPicker = false
            themeViewModel.updateTheme(
                theme.copy(
                    appcolor = String.format("#%08X", appColor.toArgb()),
                    appcolordark = String.format("#%08X", appColorDark.toArgb())
                )
            )
        } else {
            navController.popBackStack()
        }
    }
}

@Composable
fun StyleItem(
    icon: Painter = painterResource(R.drawable.ic_header),
    name: String = "Header",
    context: String = "Header, profile pic, name, buttons, etc.,",
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 2.dp)
            .clickable { onClick() }
            .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(35.dp),
            painter = icon,
            contentDescription = name,
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.Top) {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight(400),
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
            Text(
                text = context,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(18.dp)
                .alpha(0.7f),
            painter = painterResource(R.drawable.ic_nextarrow),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun rememberCustomIconPainter(
    themeId: Int,
    iconName: String,
    refreshKey: Int,
    fallback: Int = R.drawable.logo
): Painter {
    val context = LocalContext.current
    val file = File(context.filesDir, "theme$themeId/$iconName")
    val fileExists = remember(refreshKey) { file.exists() }
    return if (fileExists) {
        key(refreshKey) {
            rememberAsyncImagePainter(file)
        }
    } else {
        painterResource(id = fallback)
    }
}

@Composable
fun rememberCustomProfileIconPainter(
    chatId: Int?,
    refreshKey: Int,
    fallback: Int = R.drawable.user
): Painter {
    if (chatId == null) painterResource(fallback)
    val context = LocalContext.current
    val file = File(context.filesDir, "icons/icon$chatId.jpg")
    val fileExists = remember(refreshKey) { file.exists() }
    return if (fileExists) {
        key(refreshKey) {
            rememberAsyncImagePainter(file)
        }
    } else {
        painterResource(fallback)
    }
}

