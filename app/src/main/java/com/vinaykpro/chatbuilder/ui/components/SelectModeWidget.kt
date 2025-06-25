package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun SelectModeWidget(
    onUpdate: (Boolean) -> Unit = {}
) {
    var isDark by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select mode:",
            fontSize = 17.sp,
            fontWeight = FontWeight(500),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(top = 18.dp, bottom = 15.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(0.5f).padding(horizontal = 5.dp).clip(shape = RoundedCornerShape(12.dp)).border(2.dp, color = if(isDark) MaterialTheme.colorScheme.secondaryContainer else LightColorScheme.primary, shape = RoundedCornerShape(12.dp)).clickable { isDark = false; onUpdate(false) }) {
                RadioButton(selected = !isDark, onClick = { isDark = false; onUpdate(false) }, colors = RadioButtonColors(selectedColor = LightColorScheme.primary, unselectedColor = MaterialTheme.colorScheme.secondaryContainer, disabledSelectedColor = RadioButtonDefaults.colors().disabledSelectedColor, disabledUnselectedColor = RadioButtonDefaults.colors().disabledSelectedColor),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp))
                Text(text = "Light", fontSize = 16.sp, fontWeight = FontWeight(500), color = if(!isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.align(Alignment.Center).padding(20.dp))
            }
            Box(modifier = Modifier.weight(0.5f).padding(horizontal = 5.dp).clip(shape = RoundedCornerShape(12.dp)).border(2.dp, color = if(!isDark) MaterialTheme.colorScheme.secondaryContainer else LightColorScheme.primary, shape = RoundedCornerShape(12.dp)).clickable { isDark = true; onUpdate(true) }) {
                RadioButton(selected = isDark, onClick = { isDark = true; onUpdate(true) }, colors = RadioButtonColors(selectedColor = LightColorScheme.primary, unselectedColor = MaterialTheme.colorScheme.secondaryContainer, disabledSelectedColor = RadioButtonDefaults.colors().disabledSelectedColor, disabledUnselectedColor = RadioButtonDefaults.colors().disabledSelectedColor),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp))
                Text(text = "Dark", fontSize = 16.sp, fontWeight = FontWeight(500), color = if(isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.align(Alignment.Center).padding(20.dp))
            }
        }
    }

}