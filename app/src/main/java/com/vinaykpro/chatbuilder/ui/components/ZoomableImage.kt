package com.vinaykpro.chatbuilder.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomableImage(
    painter: Painter,
    text: String? = null,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 6f,
    contentScale: ContentScale = ContentScale.Fit,
    onScaleChanged: (Float) -> Unit = {},
    sharedModifier: Modifier,
    showInfo: Boolean = true,
    onVisibilityChange: (Boolean) -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var detailsVisible by remember { mutableStateOf(showInfo) }

    BoxWithConstraints {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        val coroutineScope = rememberCoroutineScope()

        val gestureModifier = Modifier.pointerInput(Unit) {
            var lastTapTime = 0L
            var lastTapPosition: Offset? = null

            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown()
                    val currentTime = System.currentTimeMillis()

                    val isDoubleTap = lastTapPosition != null &&
                            (currentTime - lastTapTime < 300) &&
                            (down.position - lastTapPosition!!).getDistance() < 50f

                    lastTapTime = currentTime
                    lastTapPosition = down.position

                    if (isDoubleTap) {
                        coroutineScope.launch {
                            val targetScale = if (scale > 1f) 1f else 2f
                            val zoomFactor = targetScale / scale

                            val newOffsetX = if (targetScale > 1f)
                                (containerWidth / 2f - down.position.x) * zoomFactor
                            else 0f

                            val newOffsetY = if (targetScale > 1f)
                                (containerHeight / 2f - down.position.y) * zoomFactor
                            else 0f

                            val scaleAnim = Animatable(scale)
                            val offsetXAnim = Animatable(offsetX)
                            val offsetYAnim = Animatable(offsetY)

                            launch {
                                scaleAnim.animateTo(targetScale, tween(300)) { scale = value }
                            }
                            launch {
                                offsetXAnim.animateTo(newOffsetX, tween(300)) { offsetX = value }
                            }
                            launch {
                                offsetYAnim.animateTo(newOffsetY, tween(300)) { offsetY = value }
                            }

                            onScaleChanged(targetScale)
                        }
                    } else {
                        var moved = false
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()

                            if (zoom != 1f || pan != Offset.Zero) moved = true

                            val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                            scale = newScale
                            onScaleChanged(scale)

                            if (scale > 1f) {
                                offsetX += pan.x * scale
                                offsetY += pan.y * scale

                                val boundX = (scale - 1f) * containerWidth / 2f
                                val boundY = (scale - 1f) * containerHeight / 2f

                                offsetX = offsetX.coerceIn(-boundX, boundX)
                                offsetY = offsetY.coerceIn(-boundY, boundY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        } while (event.changes.any { it.pressed })
                        if (!moved) {
                            detailsVisible = !detailsVisible
                            onVisibilityChange(detailsVisible)
                        }
                    }
                }
            }
        }

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .then(gestureModifier)
                .then(sharedModifier)
        )
        AnimatedVisibility(
            visible = text != null && detailsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .background(Color(0x773D3D3D))
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                )
                .padding(10.dp)
        ) {
            Text(
                text = text.toString(),
                color = Color.White,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }

    }
}




