package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import kotlin.String

@Preview
@Composable
fun ChatToolbar(
    name: String = "Vinaykpro",
    status: String = "online",
    isDarkTheme: Boolean = false,
    style: HeaderStyle = HeaderStyle(),
    icon1: Painter? = painterResource(R.drawable.ic_call),
    icon2: Painter? = painterResource(R.drawable.ic_videocall),
    icon3: Painter? = null,
    icon4: Painter = painterResource(R.drawable.ic_more),
    preview: Boolean = false
) {
    val themeColors = remember(style, isDarkTheme) {
        style.toParsed(isDarkTheme)
    }
    Row(modifier = Modifier.fillMaxWidth()
        .background(themeColors.navBar)
        .padding(top = if(preview) 6.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        .padding(bottom = 6.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton( onClick = {} ) {
            Icon( modifier = Modifier.size(24.dp),
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                tint = Color.White )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Image(
            painter = painterResource(R.drawable.user),
            contentDescription = null,
            modifier = Modifier.clip(shape = CircleShape).size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight(500),
                lineHeight = 20.sp,
                color = Color.White
            )
            Text(
                text = status,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            if (icon1 != null) {
                IconButton( onClick = {} ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = icon1,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            if (icon2 != null) {
                IconButton( onClick = {} ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = icon2,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            if (icon3 != null) {
                IconButton( onClick = {} ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = icon3,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            IconButton( onClick = {} ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = icon4,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

fun HeaderStyle.toParsed(isDarkTheme: Boolean): ParsedHeaderStyle {
    fun parse(hex: String): Color = Color(hex.toColorInt())

    return ParsedHeaderStyle(
        navBar = parse(if(isDarkTheme) color_navbar_dark else color_navbar),
        navIcons = parse(if(isDarkTheme) color_navicons_dark else color_navicons),
        textPrimary = parse(if(isDarkTheme) color_text_primary_dark else color_text_primary),
        textSecondary = parse(if(isDarkTheme) color_text_secondary_dark else color_text_secondary)
    )
}

data class ParsedHeaderStyle(
    val navBar: Color,
    val navIcons: Color,
    val textPrimary: Color,
    val textSecondary: Color
)