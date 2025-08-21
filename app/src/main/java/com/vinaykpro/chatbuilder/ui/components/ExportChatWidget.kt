package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.MessageEntity
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import kotlinx.coroutines.delay

@Preview
@Composable
fun ExportChatWidget(
    messages: List<MessageEntity>? = null,
    step: Int = 0,
    progress: Float = 0f,
    onWatchAdAction: (Boolean) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var isPDF by remember { mutableStateOf(true) }
    var adLoadingText by remember { mutableStateOf("Loading Ad") }

    LaunchedEffect(step) {
        while (step == 1) {
            if (adLoadingText.contains("....")) adLoadingText = "Loading Ad"
            else adLoadingText += "."
            delay(200)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x65000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp)
                )
                .then(
                    if (step != 2) Modifier
                    else Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                )
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (step != 2) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export chat",
                        fontSize = 22.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { if (step != 2) onClose() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { if (step != 2) isPDF = true }
                        .then(
                            if (isPDF)
                                Modifier
                                    .border(
                                        2.dp,
                                        LightColorScheme.primary,
                                        RoundedCornerShape(15.dp)
                                    )
                                    .background(color = LightColorScheme.primary.copy(alpha = 0.1f))
                            else
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(15.dp)
                                )
                        )
                        .padding(vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pdficon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .size(44.dp)
                    )
                    Column {
                        Text(
                            text = "Export to PDF",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "High quality, Printable, Larger size",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { if (step != 2) isPDF = false }
                        .then(
                            if (!isPDF)
                                Modifier
                                    .border(
                                        2.dp,
                                        LightColorScheme.primary,
                                        RoundedCornerShape(15.dp)
                                    )
                                    .background(color = LightColorScheme.primary.copy(alpha = 0.1f))
                            else
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(15.dp)
                                )
                        )
                        .padding(vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_htmlicon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .size(44.dp)
                    )
                    Column {
                        Text(
                            text = "Export to HTML",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "Light & Fast, Sharable, Smaller size",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Text(
                    text = if (step == 1) adLoadingText else "Watch an Ad to Export",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightColorScheme.primary)
                        .clickable { onWatchAdAction(isPDF) }
                        .padding(12.dp)
                )
            } else {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.Asset("file_scan.json")
                )
                LottieAnimation(
                    composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(250.dp)
                )
                Text(
                    if (messages == null) "Preparing messages for Export" else if (progress < 1f) "Exporting chat to ${if (isPDF) "PDF" else "HTML"}" else "Almost completed, please wait",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight(500),
                    modifier = Modifier.padding(bottom = 30.dp)
                )
                if (messages != null) {
                    BoostingProgressBar(progress = progress)
                    Text(
                        "${(messages.size * progress).toInt()}/${messages.size}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun BoostingProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White)
    ) {
        // Actual progress
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(Color(0xFF4CAF50))
        )

        // Shiny streak only inside filled part
        if (progress > 0f) {
            val transition = rememberInfiniteTransition()
            val offsetX by transition.animateFloat(
                initialValue = -200f,
                targetValue = 2000f, // will clamp below anyway
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
            ) {
                val barWidth = size.height * 2
                val clampedX = (offsetX % (size.width + barWidth)) - barWidth

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Gray.copy(alpha = 0.2f),
                            Color.White,
                            Color.Gray.copy(alpha = 0.2f)
                        ),
                        start = Offset(clampedX, 0f),
                        end = Offset(clampedX + barWidth, size.height)
                    ),
                    size = size
                )
            }
        }
    }
}
