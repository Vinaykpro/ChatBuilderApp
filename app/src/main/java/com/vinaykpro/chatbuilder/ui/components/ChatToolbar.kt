package com.vinaykpro.chatbuilder.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MyConstants

//@Preview
@SuppressLint("SuspiciousIndentation")
@Composable
fun ChatToolbar(
    name: String = "Vinaykpro",
    status: String = "online",
    isDarkTheme: Boolean = false,
    style: HeaderStyle = HeaderStyle(),
    backIcon: Painter = painterResource(R.drawable.ic_back),
    profileIcon: Painter = painterResource(R.drawable.user),
    icon1: Painter = painterResource(R.drawable.ic_call),
    icon2: Painter = painterResource(R.drawable.ic_videocall),
    icon3: Painter = painterResource(R.drawable.ic_starredmessages),
    icon4: Painter = painterResource(R.drawable.ic_more),
    preview: Boolean = false,
    previewColors: ParsedHeaderStyle = ParsedHeaderStyle(),
    previewAttrs: HeaderStyle = HeaderStyle(),
    onMenuClick: (Int) -> Unit = {}
) {
    val themeColors = if (preview) previewColors else remember(style, isDarkTheme) {
        style.toParsed(isDarkTheme)
    }
    val style = if (preview) previewAttrs else style
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.navBar)
            .padding(
                top = if (preview) 6.dp else WindowInsets.statusBars.asPaddingValues()
                    .calculateTopPadding()
            )
            .padding(bottom = 6.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (style.showbackbtn)
            IconButton(onClick = {}, modifier = Modifier.padding(start = style.backbtn_gap.dp)) {
                Icon(
                    modifier = Modifier.size(style.backbtn_size.dp),
                    painter = backIcon,
                    contentDescription = "back",
                    tint = themeColors.navIcons
                )
            }
        if (style.showprofilepic)
            Image(
                painter = profileIcon,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = style.profilepic_gap_sides.dp)
                    .clip(shape = CircleShape)
                    .size(style.profilepic_size.dp)
            )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight(500),
                lineHeight = 20.sp,
                color = themeColors.textPrimary
            )
            if (style.showstatus)
                Text(
                    text = status,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    color = themeColors.textSecondary
                )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(style.actionicons_gap.dp)) {
            if (style.is_icon1_visible) {
                IconButton(onClick = {}) {
                    Icon(
                        modifier = Modifier.size(style.actionicons_size.dp),
                        painter = icon1,
                        contentDescription = null,
                        tint = themeColors.navIcons
                    )
                }
            }
            if (style.is_icon2_visible) {
                IconButton(onClick = {}) {
                    Icon(
                        modifier = Modifier.size(style.actionicons_size.dp),
                        painter = icon2,
                        contentDescription = null,
                        tint = themeColors.navIcons
                    )
                }
            }
            if (style.is_icon3_visible) {
                IconButton(onClick = {}) {
                    Icon(
                        modifier = Modifier.size(style.actionicons_size.dp),
                        painter = icon3,
                        contentDescription = null,
                        tint = themeColors.navIcons
                    )
                }
            }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    modifier = Modifier.size(style.actionicons_size.dp),
                    painter = icon4,
                    contentDescription = null,
                    tint = themeColors.navIcons
                )
            }
            AnimatedVisibility(
                visible = expanded
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    MyConstants.chatMenuList.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = { expanded = false; onMenuClick(index) }
                        )
                    }
                }
            }
        }
    }
}


fun HeaderStyle.toParsed(isDarkTheme: Boolean): ParsedHeaderStyle {
    fun parse(hex: String): Color = Color(hex.toColorInt())

    return ParsedHeaderStyle(
        navBar = parse(if (isDarkTheme) color_navbar_dark else color_navbar),
        navIcons = parse(if (isDarkTheme) color_navicons_dark else color_navicons),
        textPrimary = parse(if (isDarkTheme) color_text_primary_dark else color_text_primary),
        textSecondary = parse(if (isDarkTheme) color_text_secondary_dark else color_text_secondary)
    )
}

data class ParsedHeaderStyle(
    val navBar: Color = Color.Transparent,
    val navIcons: Color = Color.Transparent,
    val textPrimary: Color = Color.Transparent,
    val textSecondary: Color = Color.Transparent,
)