package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R

@Preview
@Composable
fun Message(text: String = "Hii man",
            sentTime: String = "11:25 pm",
            bubbleStyle: Int = 0,
            bubbleRadius: Float = 10f,
            bubbleTipRadius: Float = 8f,
            isFirst: Boolean = false,
            isLast: Boolean = false) {
    val bubbleModifier: Modifier = when(bubbleStyle) {
        0 -> {
            Modifier
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        1 -> {
            Modifier
                .padding(start = 10.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(if(isFirst) 0.dp else bubbleRadius.dp, bubbleRadius.dp, bubbleRadius.dp, bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        2 -> {
            Modifier
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(if(isFirst) bubbleRadius.dp else 2.dp, bubbleRadius.dp, bubbleRadius.dp, if(isLast) bubbleRadius.dp else 2.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        3 -> {
            Modifier
                .padding(start = 10.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(if(isFirst) bubbleRadius.dp else 2.dp, bubbleRadius.dp, bubbleRadius.dp, if(isLast) 0.dp else 2.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        else -> {
            Modifier
        }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(1.dp).padding(top = if(isFirst) 2.dp else 0.dp).padding(end = 40.dp), contentAlignment = Alignment.Center) {
        if(bubbleStyle == 1 && isFirst) Arrow(modifier = Modifier.align(Alignment.TopStart), bubbleTipRadius)
        else if(bubbleStyle == 3 && isLast) ArrowBottom(modifier = Modifier.align(Alignment.BottomStart))

        Box(
            modifier = bubbleModifier.align( if(bubbleStyle != 3) Alignment.TopStart else Alignment.BottomStart)
        ) {
            // Message text
            Text(
                text = "$text ⠀ ⠀    ", // Extra spaces for spacing
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )

            // Sent time
            Text(
                text = sentTime,
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 11.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 1.dp)
            )
        }
    }
}

@Composable
fun Arrow(modifier: Modifier = Modifier, bubbleRadius: Float) {
    Box(
        modifier = modifier
            .size(11.dp)
            .scale(-1f, 1f)
            .background(color = Color.White, shape = BubbleShape(bubbleRadius = bubbleRadius))
    )
}

@Composable
fun ArrowBottom(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(11.dp)
            .scale(-1f, 1f)
            .background(color = Color(0xFFFFFFFF), shape = BottomBubbleShape)
    )
}