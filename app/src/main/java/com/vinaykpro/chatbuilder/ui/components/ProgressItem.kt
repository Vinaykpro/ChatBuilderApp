package com.vinaykpro.chatbuilder.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun ProgressItem(
    name: String = "Progress",
    min: Float = 0f,
    max: Float = 10f,
    progress: MutableFloatState = remember { mutableFloatStateOf(5f) }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$name:", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 5.dp, top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = progress.floatValue,
                onValueChange = { progress.floatValue = it },
                valueRange = min..max,
                steps = max.toInt() - 1,
                modifier = Modifier.weight(1f)
            )
            Text(text = progress.floatValue.toInt().toString(), color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 8.dp, end = 15.dp))
        }
    }
}

