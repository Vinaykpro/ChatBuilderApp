package com.vinaykpro.chatbuilder.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.FileEntity
import java.io.File

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun SharedTransitionScope.Message(
    text: String? = "Hii man",
    sentTime: String = "11:25 pm",
    color: Color = Color(0xFFFFFFFF),
    textColor: Color = Color(0xFF000000),
    textColorSecondary: Color = Color(0xFF414141),
    bubbleStyle: Int = 0,
    bubbleRadius: Float = 10f,
    bubbleTipRadius: Float = 8f,
    file: FileEntity? = null,
    screenWidth: Int = 200,
    screenWidthDp: Dp = 250.dp,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    showTime: Boolean = true,
    imageLoader: ImageLoader? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onMediaClick: (Int) -> Unit = {},
) {
    //val space = if (showTime) " " + "\u2004".repeat(sentTime.length) else ""
    val spaceCount = (sentTime.length * 0.6f).toInt()
    val space = if (showTime) " " + "⠀".repeat(spaceCount) else ""
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(File(context.getExternalFilesDir(null), file?.filename ?: ""))
            .build(),
        imageLoader = imageLoader ?: ImageLoader(context)
    )

    val isFile = (file != null && !(file.type == FILETYPE.IMAGE || file.type == FILETYPE.VIDEO))

    val (imageContainerModifier, containerModifier) = getContainerModifier(
        file,
        bubbleRadius,
        screenWidth
    )

    val bubbleModifier: Modifier =
        getBubbleModifier(bubbleStyle, bubbleRadius, color, isFirst, isLast, false)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .padding(top = if (isFirst) 2.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bubbleStyle == 1 && isFirst) Arrow(
            modifier = Modifier.align(Alignment.TopStart),
            bubbleTipRadius,
            color = color
        )
        else if (bubbleStyle == 3 && isLast) ArrowBottom(
            modifier = Modifier.align(Alignment.BottomStart),
            color = color
        )

        Box(
            modifier = bubbleModifier
                .widthIn(max = screenWidthDp * 0.8f)
                .align(if (bubbleStyle != 3) Alignment.TopStart else Alignment.BottomStart)
                .clickable {
                    if (file != null && isFile)
                        try {
                            val filePath = File(
                                context.getExternalFilesDir(null),
                                file.filename
                            )
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                filePath
                            )

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    uri,
                                    context.contentResolver.getType(uri) ?: "*/*"
                                )
                                flags =
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "No app found to open this file",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "This file doesn't exist in storage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        ) {
            Column(modifier = containerModifier) {
                if (file != null)
                    Box(
                        modifier = imageContainerModifier
                    ) {
                        if (!isFile) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .sharedElement(
                                        state = rememberSharedContentState(file.fileid),
                                        animatedVisibilityScope = animatedVisibilityScope!!
                                    )
                                    .clickable {
                                        onMediaClick(file.fileid)
                                    }
                            )
                            if (file.type == FILETYPE.VIDEO) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_playbtn),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(50.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color(0x31000000))
                                        .padding(bottom = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_videocall),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp)
                                            .size(16.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        text = file.duration,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(
                                        when (file.type) {
                                            FILETYPE.AUDIO -> R.drawable.ic_audiofile
                                            FILETYPE.ZIP -> R.drawable.ic_zipfile
                                            else -> R.drawable.ic_anyfile
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .padding(horizontal = 7.dp)
                                        .size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = file.displayname,
                                        color = textColor,
                                        fontSize = 16.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = true,
                                        style = TextStyle(
                                            lineBreak = LineBreak.Simple
                                        ),
                                    )
                                    Text(
                                        text = file.size,
                                        color = textColorSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                if (text != null || isFile)
                    Text(
                        text = "$text$space",
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(
                            top = 1.dp,
                            bottom = 1.dp,
                            start = 5.dp,
                            end = 2.dp
                        )
                    )
            }

            if (showTime)
                Text(
                    text = sentTime,
                    color = textColorSecondary,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            if (text == null && file != null && !isFile) MaterialTheme.colorScheme.background.copy(
                                alpha = 0.3f
                            ) else Color.Transparent
                        )
                        .padding(end = 1.dp)
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
