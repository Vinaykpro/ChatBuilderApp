package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
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
    icon1: Painter? = null,
    icon2: Painter? = null
) {
    Row(modifier = Modifier.height(60.dp).fillMaxWidth()
        .background(MaterialTheme.colorScheme.primary)
        .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "back",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Chat theme",
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