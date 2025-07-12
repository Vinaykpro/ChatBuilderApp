package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.FileEntity
import java.io.File

@Preview
@Composable
fun SenderMessage(
    text: String? = "Hii man",
    sentTime: String = "11:25 pm",
    color: Color = Color(0xFFBEFFEA),
    textColor: Color = Color(0xFF000000),
    hintTextColor: Color = Color(0xFF414141),
    bubbleStyle: Int = 0,
    bubbleRadius: Float = 10f,
    bubbleTipRadius: Float = 8f,
    file: FileEntity? = null,
    screenWidth: Int = 200,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    showTime: Boolean = true,
    showTicks: Boolean = true,
    imageLoader: ImageLoader? = null
) {
    val space = if (showTime) " ⠀ ⠀     ⠀" else if (showTicks) "   " else ""
    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(File(context.getExternalFilesDir(null), file?.filename ?: ""))
            .crossfade(true)
            .build(),
        imageLoader = imageLoader!!
    )

    val (imageContainerModifier, containerModifier) = getContainerModifier(
        file,
        bubbleRadius,
        screenWidth
    )

    val bubbleModifier: Modifier = when (bubbleStyle) {
        0 -> {
            Modifier
                .background(color = color, shape = RoundedCornerShape(bubbleRadius.dp))
                .padding(3.dp)
        }

        1 -> {
            Modifier
                .padding(end = 10.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        bubbleRadius.dp,
                        if (isFirst) 0.dp else bubbleRadius.dp,
                        bubbleRadius.dp,
                        bubbleRadius.dp
                    )
                )
                .padding(3.dp)
        }

        2 -> {
            Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        bubbleRadius.dp,
                        if (isFirst) bubbleRadius.dp else 2.dp,
                        if (isLast) bubbleRadius.dp else 2.dp,
                        bubbleRadius.dp
                    )
                )
                .padding(3.dp)
        }

        3 -> {
            Modifier
                .padding(end = 10.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        bubbleRadius.dp,
                        if (isFirst) bubbleRadius.dp else 2.dp,
                        if (isLast) 0.dp else 2.dp,
                        bubbleRadius.dp
                    )
                )
                .padding(3.dp)
        }

        else -> {
            Modifier
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .padding(top = if (isFirst) 2.dp else 0.dp)
            .padding(start = 40.dp), contentAlignment = Alignment.Center
    ) {
        if (bubbleStyle == 1 && isFirst) SentArrow(
            modifier = Modifier.align(Alignment.TopEnd),
            bubbleRadius = bubbleTipRadius,
            color
        )
        else if (bubbleStyle == 3 && isLast) SentArrowBottom(
            modifier = Modifier.align(Alignment.BottomEnd),
            color
        )
        Box(
            modifier = bubbleModifier
                .align(if (bubbleStyle != 3) Alignment.TopEnd else Alignment.BottomEnd)
        ) {
            Column(modifier = containerModifier) {
                if (file != null)
                    Box(
                        modifier = imageContainerModifier
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                if (text != null)
                    Text(
                        text = "$text  $space", // Extra spaces for spacing
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 1.dp, bottom = 1.dp, start = 5.dp, end = 3.dp)
                    )
            }

            // Sent time
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showTime) {
                    Text(
                        text = sentTime,
                        color = hintTextColor,
                        fontSize = 11.sp,
                        lineHeight = 11.sp
                    )
                }
                if (showTicks) {
                    Image(
                        painter = painterResource(id = R.drawable.doubleticks),
                        contentDescription = "Double ticks",
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(13.dp),
                        colorFilter = ColorFilter.tint(Color(0xFF1EA6E4))
                    )
                }
            }
        }
    }


}

@Composable
fun SentArrow(modifier: Modifier = Modifier, bubbleRadius: Float = 11f, color: Color) {
    Box(
        modifier = modifier
            .size(11.dp)
            .background(color = color, shape = BubbleShape(bubbleRadius = bubbleRadius))
    )
}

@Composable
fun SentArrowBottom(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .size(11.dp)
            .background(color = color, shape = BottomBubbleShape)
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

fun getContainerModifier(
    file: FileEntity?,
    bubbleRadius: Float,
    screenWidth: Int
): Pair<Modifier, Modifier> {
    var imageWidthDp: Double = -1.0
    var imageContainerModifier = Modifier
        .clip(RoundedCornerShape(bubbleRadius.dp))
        .background(Color.Black)
    var maxImageWidthDp = screenWidth * 0.65
    val maxImageHeightDp = 350.0
    val minImageHeightDp = 60.0

    if (file != null && file.thumbWidth != null && file.thumbHeight != null) {
        if (file.thumbHeight == file.thumbWidth) {
            imageWidthDp = maxImageWidthDp
            imageContainerModifier = imageContainerModifier.size(imageWidthDp.dp)
        } else {
            val imageAspectRatio = file.thumbHeight.toFloat() / file.thumbWidth.toFloat()
            maxImageWidthDp =
                if (file.thumbWidth > file.thumbHeight) screenWidth * 0.7 else screenWidth * 0.6
            val widthScaleFactor = maxImageWidthDp / file.thumbWidth
            val scaledWidthDp = file.thumbWidth * widthScaleFactor
            val scaledHeightDp = file.thumbHeight * widthScaleFactor

            val finalWidthDp = scaledWidthDp.coerceAtMost(maxImageWidthDp)
            val finalHeightDp = scaledHeightDp.coerceIn(minImageHeightDp, maxImageHeightDp)

            imageWidthDp = finalWidthDp
            imageContainerModifier = imageContainerModifier
                .width(finalWidthDp.dp)
                .height(finalHeightDp.dp)
        }
    }

    return imageContainerModifier to
            if (imageWidthDp > 0) Modifier.width(imageWidthDp.dp)
            else Modifier.wrapContentWidth()
}
