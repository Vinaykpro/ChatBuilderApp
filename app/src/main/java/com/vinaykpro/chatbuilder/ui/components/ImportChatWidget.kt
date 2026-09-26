package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.vinaykpro.chatbuilder.data.local.IMPORTSTATE
import com.vinaykpro.chatbuilder.data.local.ZipItem
import com.vinaykpro.chatbuilder.data.local.formatFileSize
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun ImportChatWidget(
    step: Int = 5,
    onClose: () -> Unit = {},
    files: List<ZipItem> = emptyList(),
    onUpdate: (List<ZipItem>) -> Unit = {},
    onMediaSave: (Boolean) -> Unit = {},
    onWatchAdAction: () -> Unit = {},
    onGoPremiumClick: () -> Unit = {},
) {
    val index = step
    val checkboxColors = CheckboxDefaults.colors(
        checkedColor = LightColorScheme.primary,
        checkmarkColor = MaterialTheme.colorScheme.background,
        uncheckedColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x65000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { })
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {})
                .clip(shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.onSurface)
                .align(Alignment.BottomCenter)
                .heightIn(max = 620.dp),
        ) {
            if (step != IMPORTSTATE.MEDIASELECTION)
                IconButton(
                    onClick = { onClose() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            Column(
                modifier = Modifier
                    .padding(vertical = if (index == IMPORTSTATE.MEDIASELECTION) 12.dp else 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (index) {
                    IMPORTSTATE.STARTED,
                    IMPORTSTATE.UNSUPPORTEDFILE,
                    IMPORTSTATE.ALMOSTCOMPLETED,
                    IMPORTSTATE.SUCCESS -> {
                        val (currentAsset, text, iterations) = when (index) {
                            IMPORTSTATE.STARTED, IMPORTSTATE.ALMOSTCOMPLETED -> Triple(
                                "file_scan.json",
                                if (index == IMPORTSTATE.STARTED) "Analyzing file contents..."
                                else "Almost completed, please wait...",
                                LottieConstants.IterateForever
                            )

                            IMPORTSTATE.UNSUPPORTEDFILE -> Triple(
                                "file_error.json",
                                "Unsupported file format",
                                1
                            )

                            IMPORTSTATE.SUCCESS -> Triple(
                                "file_success.json",
                                "Successfully imported!",
                                1
                            )

                            else -> Triple("file_scan.json", "", 1)
                        }

                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.Asset(currentAsset)
                        )

                        LottieAnimation(
                            composition,
                            iterations = iterations,
                            modifier = Modifier.size(240.dp)
                        )

                        Text(
                            text = text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
                        )
                    }

                    IMPORTSTATE.MEDIASELECTION -> {
                        var toggleSelectAll by remember { mutableStateOf(true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Media Found (${files.count { it.isSelected }}/${files.size})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = formatFileSize(files.sumOf { if (it.isSelected) it.byteCount else 0L }),
                                color = LightColorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "S.no",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.15f)
                            )
                            Text(
                                text = "Media details",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.65f)
                            )
                            Box(
                                modifier = Modifier.weight(0.2f),
                                contentAlignment = Alignment.Center
                            ) {
                                Checkbox(
                                    checked = toggleSelectAll,
                                    onCheckedChange = {
                                        val new = files.toMutableList()
                                        files.forEachIndexed { i, item ->
                                            new[i] = item.copy(isSelected = it)
                                        }
                                        onUpdate(new)
                                        toggleSelectAll = it
                                    },
                                    colors = checkboxColors
                                )
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(files) { i, file ->
                                ZipListItem(
                                    index = i,
                                    item = file,
                                    isSelected = file.isSelected,
                                    onCheckChange = {
                                        val new = files.toMutableList()
                                        new[i] = files[i].copy(isSelected = it)
                                        onUpdate(new)
                                    },
                                    checkboxColors = checkboxColors
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Skip",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                    .clickable { onMediaSave(false) }
                                    .padding(14.dp)
                            )
                            Text(
                                text = "Keep",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(LightColorScheme.primary)
                                    .clickable { onMediaSave(true) }
                                    .padding(14.dp)
                            )
                        }
                    }

                    IMPORTSTATE.WATCHAD -> {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.Asset("watchAd.json")
                        )

                        LottieAnimation(
                            composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(240.dp)
                        )

                        Text(
                            text = "Watch an Ad to Continue [Import]",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LightColorScheme.primary)
                                .clickable { onWatchAdAction() }
                                .padding(14.dp)
                        )

                        Text(
                            text = "OR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
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
                                text = "Go Premium | Ad-Free Imports",
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
    }
}
