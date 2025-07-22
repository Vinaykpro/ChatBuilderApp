package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import com.vinaykpro.chatbuilder.ui.theme.White

@Preview
@Composable
fun EditIcon(
    name: String? = null,
    icon: Painter = painterResource(R.drawable.logo),
    size: Int = 75,
    iconSize: Int = 28,
    color: Color = LightColorScheme.primary,
    filter: ColorFilter? = ColorFilter.tint(White),
    onClick: () -> Unit = {}
) {
    if (name != null) Text(
        text = "$name:",
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 14.sp,
        modifier = Modifier.padding(start = 5.dp, top = 5.dp, bottom = 10.dp)
    )
    Column(
        modifier = Modifier
            .padding(start = 8.dp)
            .width(size.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(color), contentAlignment = Alignment.Center
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize.dp),
                colorFilter = filter
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(10.dp))
                .border(
                    0.5.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onClick() }
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "Edit",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
            )
        }
    }
}