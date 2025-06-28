package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.DarkColorScheme

@Preview
@Composable
fun SettingsItem(
    icon: Painter = painterResource(R.drawable.ic_theme),
    name: String = "Chat theme",
    context: String = "Create, import or customize the current chat theme",
    isForceDark: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(top = 2.dp)
        .clickable(
            onClick = { onClick() },
            indication = rememberRipple(color = MaterialTheme.colorScheme.onPrimaryContainer),
            interactionSource = remember { MutableInteractionSource() })
        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(modifier = Modifier
            .size(35.dp)
            .alpha(0.7f),
            painter = icon,
            contentDescription = name,
            tint = if(isForceDark) DarkColorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.Top) {
            Text(text = name, color = if(isForceDark) DarkColorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(500), fontSize = 16.sp, lineHeight = 20.sp)
            Text(text = context, color = if(isForceDark) DarkColorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp, lineHeight = 20.sp)
        }
    }
}