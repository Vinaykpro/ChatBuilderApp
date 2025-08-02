package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun SwitchItem(
    name: String = "Show back button",
    context: String = "Show/hide that allows users to exit.",
    enabled: Boolean = true,
    checked: Boolean = false,
    onCheckChange: (Boolean) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val switchColors = SwitchDefaults.colors(
        checkedTrackColor = LightColorScheme.primary,
        checkedThumbColor = MaterialTheme.colorScheme.background
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 2.dp, end = 6.dp)
            .clickable(indication = null, interactionSource = interactionSource) {
                onCheckChange(!checked)
            }
            .padding(6.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight(500),
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
            if (context != "")
                Text(
                    text = context,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (enabled) Switch(
            checked = checked,
            onCheckedChange = onCheckChange,
            colors = switchColors
        )
        else Switch(checked = true, onCheckedChange = null, enabled = false, colors = switchColors)
    }
}
