package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R

@Preview
@Composable
fun ColorSelectionItem(
    name: String = "Primary color",
    bubbleSize: Int = 42,
    color: Color = Color(0xff4b48ff),
    onClick: () -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxWidth().padding(end = 10.dp).clickable { onClick() }.padding(vertical = 8.dp, horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(bubbleSize.dp).clip(CircleShape).border(2.dp, color = Color(0xffB1B1B1), shape = CircleShape)) {
            Image(
                painter = painterResource(id = R.drawable.ic_transparent_grid),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().alpha(0.5f)
            )
            Spacer(modifier = Modifier.fillMaxSize().background(color))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 20.sp
            )
            Text(
                text = "Tap here to change the color",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                lineHeight = 16.sp
            )
        }
    }
}