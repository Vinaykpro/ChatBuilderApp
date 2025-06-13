package com.vinaykpro.chatbuilder.ui.components

import android.annotation.SuppressLint
import android.widget.Switch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Preview
@Composable
fun SwitchItem(
    state: MutableState<Boolean> = remember { mutableStateOf(false) },
    name: String = "Show back button",
    context: String = "Show/hide that allows users to exit",
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 2.dp, end = 6.dp).clickable(indication = null, interactionSource =interactionSource ) { state.value = !state.value }.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.Top) {
            Text(text = name, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight(500), fontSize = 16.sp, lineHeight = 20.sp)
            Text(text = context, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp, lineHeight = 20.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = state.value, onCheckedChange = { state.value = it })
    }
}


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun MySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color.Gray,
        label = "ThumbColor"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else Color(0xFFDFDFDF),
        label = "TrackColor"
    )

    val alignment by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        label = "ThumbPosition"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(height = 30.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable(enabled = enabled) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .offset(x = alignment)
                .size(26.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
