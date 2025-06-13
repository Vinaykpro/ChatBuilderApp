package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.Input
import com.vinaykpro.chatbuilder.ui.components.SettingsItem

@Preview
@Composable
fun EditThemeScreen(themename : String = "Default theme") {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        BasicToolbar(name = themename)
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 18.dp)) {
            Text(
                text = "Theme properties",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 18.dp)
            )
            Row(modifier = Modifier.padding(start = 6.dp)) {
                EditIcon(size = 80)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Input()
                    Input(name = "Made by:", value = "Vinaykpro")
                }
            }
            Text(
                text = "App Style",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
            )
            ColorSelectionItem(name = "Primary Color", color = Color(0xFF29C6CB))
            ColorSelectionItem(name = "Primary Color (Dark)", color = Color(0xFF2A2B2B))
            Text(
                text = "Chat Screen Styles",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 15.dp)
            )
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 18.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(Color(0x0F7B7B7B))
                .padding(vertical = 10.dp)
            ) {
                StyleItem(onClick = {})
                StyleItem(icon = painterResource(R.drawable.ic_body), name = "Body", context = "Chat bubbles, background, widgets, etc.,")
                StyleItem(icon = painterResource(R.drawable.ic_msgbar), name = "Message Bar", context = "Chat input, buttons, actions, etc.,")
            }
        }
    }
}

@Composable
fun StyleItem(
    icon: Painter = painterResource(R.drawable.ic_header),
    name: String = "Header",
    context: String = "Header, profile pic, name, buttons, etc.,",
    onClick: () -> Unit = {}) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(top = 2.dp)
        .clickable { onClick() }
        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(modifier = Modifier.size(35.dp),
            painter = icon,
            contentDescription = name,
            tint = Color.Unspecified)
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.Top) {
            Text(text = name, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(400), fontSize = 16.sp, lineHeight = 20.sp)
            Text(text = context, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp, lineHeight = 20.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(modifier = Modifier
            .padding(end = 6.dp)
            .size(18.dp)
            .alpha(0.7f),
            painter = painterResource(R.drawable.ic_nextarrow),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}