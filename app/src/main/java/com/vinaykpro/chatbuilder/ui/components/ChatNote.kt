package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun ChatNote(note: String = "Today") {
    Box(modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth()) {
        Text(text = note, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight(500), color = MaterialTheme.colorScheme.onSecondaryContainer, textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 12.dp, vertical = 8.dp))
    }
}