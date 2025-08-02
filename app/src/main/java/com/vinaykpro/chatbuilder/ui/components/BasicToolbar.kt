package com.vinaykpro.chatbuilder.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.vinaykpro.chatbuilder.data.utils.DebounceClickHandler

//@Preview
@Composable
fun BasicToolbar(
    name: String = "Chat theme",
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    icon1: Painter? = null,
    icon2: Painter? = null,
    onIcon1Click: () -> Unit = {},
    onIcon2Click: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val view = LocalView.current
    val activity = LocalContext.current as Activity

    val useDarkIcons = color.luminance() > 0.5f

    SideEffect {
        val window = activity.window
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = useDarkIcons
    }

    Row(modifier = Modifier.fillMaxWidth()
        .background(color)
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        .padding(bottom = 6.dp, start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton( onClick = {
            DebounceClickHandler.run {
                onBackClick()
            }
        }
        ) {
            Icon( modifier = Modifier.size(24.dp),
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                tint = textColor )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight(600),
            color = textColor
        )
        Spacer(modifier = Modifier.weight(1f))
        if (icon1 != null) {
            IconButton(onClick = onIcon1Click) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = icon1,
                    contentDescription = "back",
                    tint = textColor
                )
            }
        }
        if (icon2 != null) {
            IconButton(onClick = onIcon2Click) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = icon2,
                    contentDescription = "back",
                    tint = textColor
                )
            }
        }
    }
}