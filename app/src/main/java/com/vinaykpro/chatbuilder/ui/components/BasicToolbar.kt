package com.vinaykpro.chatbuilder.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun BasicToolbar(
    name: String = "Chat theme",
    color: Color = MaterialTheme.colorScheme.primary,
    icon1: Painter? = null,
    icon2: Painter? = null
) {
    Row(modifier = Modifier.fillMaxWidth()
        .background(color)
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        .padding(bottom = 6.dp, start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton( onClick = {} ) {
            Icon( modifier = Modifier.size(24.dp),
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                tint = Color.White )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight(600),
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        if (icon1 != null) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = icon1,
                contentDescription = "back",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        if (icon2 != null) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = icon2,
                contentDescription = "back",
                tint = Color.White
            )
        }
    }
}