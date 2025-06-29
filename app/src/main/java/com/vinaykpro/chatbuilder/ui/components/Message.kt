package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun Message(text: String = "Hii man",
            sentTime: String = "11:25 pm",
            color: Color = Color(0xFFFFFFFF),
            textColor: Color = Color(0xFF000000),
            hintTextColor: Color = Color(0xFF414141),
            bubbleStyle: Int = 0,
            bubbleRadius: Float = 10f,
            bubbleTipRadius: Float = 8f,
            isFirst: Boolean = false,
            isLast: Boolean = false,
            showTime: Boolean = true) {
    val space = if(showTime) " ⠀ ⠀    " else ""
    val bubbleModifier: Modifier = when(bubbleStyle) {
        0 -> {
            Modifier
                .background(color = color, shape = RoundedCornerShape(bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        1 -> {
            Modifier
                .padding(start = 10.dp)
                .background(color = color, shape = RoundedCornerShape(if(isFirst) 0.dp else bubbleRadius.dp, bubbleRadius.dp, bubbleRadius.dp, bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        2 -> {
            Modifier
                .background(color = color, shape = RoundedCornerShape(if(isFirst) bubbleRadius.dp else 2.dp, bubbleRadius.dp, bubbleRadius.dp, if(isLast) bubbleRadius.dp else 2.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        3 -> {
            Modifier
                .padding(start = 10.dp)
                .background(color = color, shape = RoundedCornerShape(if(isFirst) bubbleRadius.dp else 2.dp, bubbleRadius.dp, bubbleRadius.dp, if(isLast) 0.dp else 2.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        else -> {
            Modifier
        }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(1.dp).padding(top = if(isFirst) 2.dp else 0.dp).padding(end = 40.dp), contentAlignment = Alignment.Center) {
        if(bubbleStyle == 1 && isFirst) Arrow(modifier = Modifier.align(Alignment.TopStart), bubbleTipRadius, color = color)
        else if(bubbleStyle == 3 && isLast) ArrowBottom(modifier = Modifier.align(Alignment.BottomStart), color = color)

        Box(
            modifier = bubbleModifier.align( if(bubbleStyle != 3) Alignment.TopStart else Alignment.BottomStart)
        ) {
            Text(
                text = "$text $space", // Extra spaces for spacing
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )

            if(showTime)
            Text(
                text = sentTime,
                color = hintTextColor,
                fontSize = 11.sp,
                lineHeight = 11.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 1.dp)
            )
        }
    }
}

@Composable
fun Arrow(modifier: Modifier = Modifier, bubbleRadius: Float, color: Color) {
    Box(
        modifier = modifier
            .size(11.dp)
            .scale(-1f, 1f)
            .background(color = color, shape = BubbleShape(bubbleRadius = bubbleRadius))
    )
}

@Composable
fun ArrowBottom(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .size(11.dp)
            .scale(-1f, 1f)
            .background(color = color, shape = BottomBubbleShape)
    )
}
