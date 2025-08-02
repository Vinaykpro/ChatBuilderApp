package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Composable
fun ActionIconItem(
    name: String,
    icon: Painter,
    iconSize: Int,
    visible: Boolean = true,
    onEdit: () -> Unit,
    onVisibilityChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(LightColorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize.dp)
            )
        }
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight(500),
            fontSize = 14.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onEdit() }) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "replace",
                tint = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .padding(5.dp)
                    .size(18.dp)
            )
        }

        IconButton(onClick = { onVisibilityChange(!visible) }) {
            Icon(
                painter = if (visible) painterResource(R.drawable.ic_eye) else painterResource(R.drawable.ic_eyeoff),
                contentDescription = null,
                tint = LightColorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
            )
        }
    }
}

@Composable
fun OuterButtonIcon(
    painter: Painter,
    tint: Color,
    background: Color
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(46.dp)
            .clip(shape = CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            with(painter) {
                draw(
                    size = this@Canvas.size,
                    colorFilter = ColorFilter.tint(tint)
                )
            }
        }
    }
}
