package com.vinaykpro.chatbuilder.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.icu.text.BreakIterator
import android.os.Build
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
//@Preview
@Composable
fun SharedTransitionScope.SenderMessage(
    text: String? = "Hii man",
    sentTime: String = "11:25 pm",
    date: (@Composable () -> Unit)? = null,
    color: Color = Color(0xFFBEFFEA),
    textColor: Color = Color(0xFF000000),
    textColorSecondary: Color = Color(0xFF414141),
    ticksIcon: Painter = painterResource(R.drawable.doubleticks),
    bubbleStyle: Int = 0,
    bubbleRadius: Float = 10f,
    bubbleTipRadius: Float = 8f,
    file: FileEntity? = null,
    searchedString: String?,
    screenWidth: Int = 200,
    screenWidthDp: Dp,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    showTime: Boolean = true,
    showTicks: Boolean = true,
    imageLoader: ImageLoader? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onMediaClick: (Int) -> Unit = {},
    onCopy: (String) -> Unit = {}
) {
    //var space = if (showTime) "  " + "\u2004".repeat(sentTime.length) else ""
    val spaceCount = (sentTime.length * 0.6f).toInt()
    var space = if (showTime) "  " + "⠀".repeat(spaceCount) else ""
    if (showTicks) space += "⠀"
    val containsEmoji = text != null && containsEmoji(text)
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
        getBubbleModifier(bubbleStyle, bubbleRadius, color, isFirst, isLast)

    date?.invoke()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .clickable {
                if (text != null) onCopy(text)
            }
            .padding(top = if (isFirst) 2.dp else 0.dp),
        contentAlignment = Alignment.Center
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
                .widthIn(max = screenWidthDp * 0.8f)
                .align(if (bubbleStyle != 3) Alignment.TopEnd else Alignment.BottomEnd)
                .then(
                    if (file != null) {
                        Modifier.clickable {
                            if (isFile)
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
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(modifier = containerModifier) {
                if (file != null) {
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
                                    .padding(vertical = 8.dp),
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
                                        )
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
                }

                if (text != null || isFile) {
                    if (searchedString != null)
                        HighlightedText(
                            fullText = if (isFile) " " else "$text$space",
                            searchedText = searchedString,
                            textColor = textColor,
                        )
                    else if (containsEmoji)
                        EmojiStyledText(
                            fullText = text!!,
                            textColor = textColor,
                            emojiFontSize = 22.sp,
                            space = space
                        )
                    else
                        Text(
                            text = if (isFile) " " else "$text$space", // Extra spaces for spacing
                            color = textColor,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(
                                top = 1.dp,
                                bottom = 1.dp,
                                start = 5.dp,
                                end = 3.dp
                            )
                        )
                }
            }

            // Sent time
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (file != null && !isFile) 2.dp else 0.dp, end = 1.dp)
                    .background(
                        if (text == null && file != null && !isFile) MaterialTheme.colorScheme.background.copy(
                            alpha = 0.3f
                        ) else Color.Transparent
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showTime) {
                    Text(
                        text = sentTime,
                        color = textColorSecondary,
                        fontSize = 11.sp,
                        lineHeight = 11.sp
                    )
                }
                if (showTicks) {
                    Image(
                        painter = ticksIcon,
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

fun getBubbleModifier(
    bubbleStyle: Int,
    bubbleRadius: Float,
    color: Color,
    isFirst: Boolean,
    isLast: Boolean,
    isSender: Boolean = true
): Modifier {
    val modifier = when (bubbleStyle) {
        0 -> {
            Modifier
                .background(color = color, shape = RoundedCornerShape(bubbleRadius.dp))
                .padding(3.dp)
        }

        1 -> {
            Modifier
                .padding(
                    end = if (isSender) 10.dp else 0.dp,
                    start = if (!isSender) 10.dp else 0.dp
                )
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        if (!isSender && isFirst) 0.dp else bubbleRadius.dp,
                        if (isSender && isFirst) 0.dp else bubbleRadius.dp,
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
                        if (!isSender && isFirst) bubbleRadius.dp else 2.dp,
                        if (isSender && isFirst) bubbleRadius.dp else 2.dp,
                        if (isSender && isLast) bubbleRadius.dp else 2.dp,
                        if (!isSender && isLast) bubbleRadius.dp else 2.dp
                    )
                )
                .padding(3.dp)
        }

        3 -> {
            Modifier
                .padding(
                    end = if (isSender) 10.dp else 0.dp,
                    start = if (!isSender) 10.dp else 0.dp
                )
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        if (!isSender && isFirst) bubbleRadius.dp else 2.dp,
                        if (isSender && isFirst) bubbleRadius.dp else 2.dp,
                        if (isSender && isLast) 0.dp else 2.dp,
                        if (!isSender && isLast) 0.dp else 2.dp
                    )
                )
                .padding(3.dp)
        }

        else -> {
            Modifier
        }
    }

    return modifier
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
    var imageContainerModifier: Modifier = Modifier
    var maxImageWidthDp = screenWidth * 0.65
    val maxImageHeightDp = 350.0
    val minImageHeightDp = 70.0
    if (file != null) {
        imageContainerModifier =
            Modifier
                .clip(RoundedCornerShape(bubbleRadius.dp))
                .background(Color(0x25000000))
        // val imageAspectRatio = file.thumbHeight.toFloat() / file.thumbWidth.toFloat()
        if ((file.type == FILETYPE.IMAGE || file.type == FILETYPE.VIDEO) && file.thumbWidth != null && file.thumbHeight != null) {
            if (file.thumbWidth == file.thumbHeight) {
                imageWidthDp = maxImageWidthDp
                imageContainerModifier = imageContainerModifier.size(imageWidthDp.dp)
            } else {
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
        } else {
            imageWidthDp = maxImageWidthDp
        }
    }

    return imageContainerModifier to
            if (imageWidthDp > 0) Modifier.width(imageWidthDp.dp)
            else Modifier.wrapContentWidth()
}

@Composable
fun HighlightedText(
    fullText: String,
    searchedText: String,
    textColor: Color,
    highlightColor: Color = Color.Yellow
) {
    val annotatedString = buildAnnotatedString {
        var startIndex = 0
        val lowerFull = fullText.lowercase()
        val lowerSearch = searchedText.lowercase()

        while (startIndex < lowerFull.length) {
            val index = lowerFull.indexOf(lowerSearch, startIndex)
            if (index == -1) {
                append(fullText.substring(startIndex))
                break
            }

            append(fullText.substring(startIndex, index))

            withStyle(SpanStyle(background = highlightColor, color = Color.Black)) {
                append(fullText.substring(index, index + searchedText.length))
            }
            startIndex = index + searchedText.length
        }
    }

    Text(
        text = annotatedString,
        color = textColor,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        modifier = Modifier.padding(top = 1.dp, bottom = 1.dp, start = 5.dp, end = 3.dp)
    )
}

@Composable
fun EmojiStyledText(
    fullText: String,
    textColor: Color = Color.Black,
    fontSize: TextUnit = 16.sp,
    emojiFontSize: TextUnit = 20.sp, // Bigger size for emoji
    modifier: Modifier = Modifier,
    space: String
) {
    //Log.i("vkpro", "'$fullText' : len = ${fullText.length} : fontSize = ${emojiFontSize.value}")
    val graphemes = fullText.graphemeClusters()
    val emojiFontSize = getFontSizeForText(graphemes, emojiFontSize)

    val annotated = buildAnnotatedString {
        for (grapheme in graphemes) {
            if (grapheme.isEmoji()) {
                withStyle(
                    style = SpanStyle(
                        fontSize = emojiFontSize,
                        baselineShift = BaselineShift(-0.1f)
                    )
                ) {
                    append(grapheme)
                }
            } else {
                append(grapheme)
            }
        }
        append(space)
    }

    Text(
        text = annotated,
        color = textColor,
        fontSize = fontSize,
        modifier = Modifier.padding(top = 1.dp, bottom = 1.dp, start = 5.dp, end = 3.dp)
    )
}

fun getFontSizeForText(chunks: List<String>, defaultSize: TextUnit): TextUnit {
    return when (chunks.size) {
        1 -> 35.sp
        2 -> if (chunks[0].isEmoji() && chunks[1].isEmoji()) 28.sp else defaultSize
        else -> defaultSize
    }
}

fun String.graphemeClusters(): List<String> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val result = mutableListOf<String>()
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(this)
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            result.add(this.substring(start, end))
            start = end
            end = iterator.next()
        }
        return result
    }
    return Regex("""\X""").findAll(this).map { it.value }.toList()
}

fun String.isEmoji(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val cp = this.codePoints().toArray()

        val hasEmojiTraits = cp.any { it in 0x1F600..0x1FAFF || it in 0x2600..0x27BF }
                || this.contains('\uFE0F')  // Emoji VS-16
                || cp.any { it in 0x1F3FB..0x1F3FF } // skin tones
                || (this.contains('\u200D') && cp.any { it >= 0x1F300 }) // ZWJ + emoji

        return hasEmojiTraits
    } else {
        return this.length > 1
    }
}

fun containsEmoji(text: String): Boolean {
    return text.any { Character.getType(it) == Character.SURROGATE.toInt() }
}