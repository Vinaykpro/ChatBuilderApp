package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
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
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun ProgressItem(
    name: String = "Progress",
    min: Float = 0f,
    max: Float = 10f,
    value: Float = 10f,
    progress: MutableFloatState? = null,
    onChange: (Int) -> Unit = {}
) {
    val progress = progress?: remember { mutableFloatStateOf(value) }
    val colors: SliderColors = SliderDefaults.colors(thumbColor = LightColorScheme.primary, activeTrackColor = LightColorScheme.primary)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$name:", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 5.dp, top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = progress.floatValue,
                onValueChange = { progress.floatValue = it },
                valueRange = min..max,
                steps = max.toInt() - 1,
                modifier = Modifier.weight(1f),
                colors = colors
            )
            onChange(progress.floatValue.toInt())
            Text(text = progress.floatValue.toInt().toString(), color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight(500), fontSize = 14.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 8.dp, end = 15.dp))
        }
    }
}

