package com.vinaykpro.chatbuilder.ui.components

import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import com.vinaykpro.chatbuilder.R
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun VideoPlayer(
    file: File,
    text: String? = null,
    modifier: Modifier = Modifier,
    loop: Boolean = false,
    onVisibilityChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }

    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var duration by remember { mutableIntStateOf(0) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var showThumbnail by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = videoView.currentPosition
            delay(300)
        }
    }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(1000)
            showControls = false
            onVisibilityChange(false)
        }
    }

    // Gesture: double tap to seek
    val doubleTapModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                showControls = !showControls
                onVisibilityChange(showControls)
            },
            onDoubleTap = { offset ->
                val width = context.resources.displayMetrics.widthPixels
                if (offset.x < width / 2) {
                    // Left side: rewind 5s
                    val target = (videoView.currentPosition - 5000).coerceAtLeast(0)
                    videoView.seekTo(target)
                } else {
                    // Right side: forward 5s
                    val target = (videoView.currentPosition + 5000).coerceAtMost(duration)
                    videoView.seekTo(target)
                }
                showControls = true
                onVisibilityChange(true)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(doubleTapModifier)
    ) {
        AndroidView(
            factory = {
                videoView.apply {
                    setVideoPath(file.path)
                    setOnPreparedListener {
                        duration = it.duration
                    }
                    setOnCompletionListener {
                        if (loop) {
                            start()
                        } else {
                            isPlaying = false
                            showControls = true
                            onVisibilityChange(true)
                            showThumbnail = true
                        }
                    }
                }
            },
            update = {
                if (isPlaying && !it.isPlaying) it.start()
                else if (!isPlaying && it.isPlaying) it.pause()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showThumbnail) {
            val painter = rememberAsyncImagePainter(
                model = file,
                imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(VideoFrameDecoder.Factory())
                    }
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 🎮 Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
            ) {

                IconButton(
                    onClick = {
                        showThumbnail = false
                        if (isPlaying) {
                            videoView.pause()
                        } else {
                            videoView.start()
                            showControls = false
                            onVisibilityChange(false)
                        }
                        isPlaying = !isPlaying
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(75.dp)
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .background(Color(0x773D3D3D))
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        )
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (text != null)
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = {
                                currentPosition = it.toInt()
                                videoView.seekTo(currentPosition)
                            },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White
                            )
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
