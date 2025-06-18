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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R

@Preview
@Composable
fun SenderMessage(text: String = "Hii man",
                  sentTime: String = "11:25 pm",
                  bubbleStyle: Int = 0,
                  bubbleRadius: Int = 10,
                  bubbleTipRadius: Int = 8,
                  isFirst: Boolean = false,
                  isLast: Boolean = false) {

    var bubbleModifier: Modifier = when(bubbleStyle) {
        0 -> {
            Modifier
                .background(color = Color(0xFFE1FFC7), shape = RoundedCornerShape(bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        1 -> {
            Modifier
                .padding(end = 10.dp)
                .background(color = Color(0xFFE1FFC7), shape = RoundedCornerShape(bubbleRadius.dp, if(isFirst) 0.dp else bubbleRadius.dp, bubbleRadius.dp, bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        2 -> {
            Modifier
                .background(color = Color(0xFFE1FFC7), shape = RoundedCornerShape(bubbleRadius.dp, if(isFirst) bubbleRadius.dp else 2.dp, if(isLast) bubbleRadius.dp else 2.dp, bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        3 -> {
            Modifier
                .padding(end = 10.dp)
                .background(color = Color(0xFFE1FFC7), shape = RoundedCornerShape(bubbleRadius.dp, if(isFirst) bubbleRadius.dp else 2.dp, if(isLast) 0.dp else 2.dp, bubbleRadius.dp))
                .padding(vertical = 4.dp)
                .padding(start = 10.dp, end = 5.dp)
        }
        else -> {
            Modifier
        }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(1.dp).padding(start = 40.dp), contentAlignment = Alignment.Center) {
        if(bubbleStyle == 1 && isFirst) SentArrow(modifier = Modifier.align(Alignment.TopEnd), bubbleRadius = bubbleTipRadius)
        else if(bubbleStyle == 3 && isLast) SentArrowBottom(modifier = Modifier.align(Alignment.BottomEnd))
        Box(
            modifier = bubbleModifier
                .align(if(bubbleStyle != 3) Alignment.TopEnd else Alignment.BottomEnd)
        ) {
            // Message text
            Text(
                text = "$text ⠀ ⠀ ⠀   ⠀", // Extra spaces for spacing
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )

            // Sent time
            Row(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sentTime,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                )
                Image(
                    painter = painterResource(id = R.drawable.doubleticks),
                    contentDescription = "Custom SVG Image",
                    modifier = Modifier.padding(start = 2.dp).size(13.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF1EA6E4))
                )
            }
        }
    }


}

@Composable
fun SentArrow(modifier: Modifier = Modifier, bubbleRadius: Int = 11) {
    Box(
        modifier = modifier
            .size(11.dp)
            .background(color = Color(0xFFE1FFC7), shape = BubbleShape(bubbleRadius = bubbleRadius.toFloat()))
    )
}
@Composable
fun SentArrowBottom(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(11.dp)
            .background(color = Color(0xFFE1FFC7), shape = BottomBubbleShape)
    )
}

fun BubbleShape(bubbleRadius: Float): Shape = GenericShape { size, _ ->
    val radius = bubbleRadius

    moveTo(0f, 0f)
    lineTo(size.width - radius, 0f)

    quadraticBezierTo(
        size.width, 0f,
        size.width - radius / 3f, radius
    )

    lineTo(0f, size.height)
    close()
}

val BottomBubbleShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height

    moveTo(w * 8.82502f / 13.2375f, h * 8.9375f / 11f)
    cubicTo(
        w * 1.93246f / 13.2375f, h * 5.71577f / 11f,
        w * 0.735418f / 13.2375f, h * 2.29167f / 11f,
        0f, 0f
    )
    lineTo(0f, h)
    lineTo(w * 7.72189f / 13.2375f, h)
    cubicTo(
        w * 9.92815f / 13.2375f, h,
        w, h,
        w * 8.82502f / 13.2375f, h * 8.9375f / 11f
    )
    close()
}