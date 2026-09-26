package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    isPremium: Boolean = false,
    onWatchAdAction: (Int) -> Unit = {},
    onGoPremiumClick: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var exportType by remember { mutableIntStateOf(0) }
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
                    shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp)
                )
                .then(
                    if (step != 2) Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    else Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                )
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = step != 2,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Export chat",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
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

                    // Option 0: PDF No Media
                    ExportOptionCard(
                        title = "Export to PDF",
                        subtitle = "No Media • Printable • Fast",
                        iconRes = R.drawable.ic_pdficon,
                        isSelected = exportType == 0,
                        isPremiumOption = false,
                        isUserPremium = isPremium,
                        onClick = { if (step != 2) exportType = 0 }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 1: PDF with Media
                    ExportOptionCard(
                        title = "Export to PDF (with Media)",
                        subtitle = "All Media • Printable • Compressed",
                        iconRes = R.drawable.ic_pdficon,
                        isSelected = exportType == 1,
                        isPremiumOption = true,
                        isUserPremium = isPremium,
                        onClick = { if (step != 2) exportType = 1 }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: PDF Legacy HD
                    ExportOptionCard(
                        title = "Export to PDF (Legacy HD)",
                        subtitle = "All languages • HD • Larger size",
                        iconRes = R.drawable.ic_pdficon,
                        isSelected = exportType == 2,
                        isPremiumOption = true,
                        isUserPremium = isPremium,
                        onClick = { if (step != 2) exportType = 2 }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 3: HTML
                    ExportOptionCard(
                        title = "Export to HTML",
                        subtitle = "Light & Fast • Shareable • Interactive",
                        iconRes = R.drawable.ic_htmlicon,
                        isSelected = exportType == 3,
                        isPremiumOption = true,
                        isUserPremium = isPremium,
                        onClick = { if (step != 2) exportType = 3 }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (step == 1) adLoadingText else "Export chat" + if (!isPremium && exportType != 0) " [Ad]" else "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightColorScheme.primary)
                            .clickable { onWatchAdAction(exportType) }
                            .padding(14.dp)
                    )

                    AnimatedVisibility(
                        visible = !isPremium && exportType != 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "OR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE5C06B))
                                    .clickable { onGoPremiumClick() }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_go_pro_crown),
                                    contentDescription = null,
                                    tint = Color(0xFF5B3E00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Go Premium | Unlimited Free Exports",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF382700),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = step == 2,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.Asset("file_scan.json")
                    )
                    LottieAnimation(
                        composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(240.dp)
                    )
                    Text(
                        text = if (messages == null) "Preparing messages for Export..." else if (progress < 1f) "Exporting chat to ${if (exportType <= 2) "PDF" else "HTML"}..." else "Almost completed, please wait...",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    if (messages != null) {
                        BoostingProgressBar(progress = progress)
                        Text(
                            text = "${(messages.size * progress).toInt()} / ${messages.size} messages",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    isSelected: Boolean,
    isPremiumOption: Boolean,
    isUserPremium: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) LightColorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val bgColor = if (isSelected) LightColorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(bgColor)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(42.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (isPremiumOption && !isUserPremium) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(255, 215, 0, 45))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_go_pro_crown),
                            contentDescription = "PRO",
                            tint = Color(212, 160, 23),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PRO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(180, 130, 10)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
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
