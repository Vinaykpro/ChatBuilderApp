package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
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
    step: Int = 2,
    onClose: () -> Unit = {},
    files: List<ZipItem> = emptyList(),
    onUpdate: (List<ZipItem>) -> Unit = {},
    onMediaSave: (Boolean) -> Unit = {},
    isDark: Boolean = true,
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
                .clip(shape = RoundedCornerShape(14.dp, 14.dp))
                .background(if (isDark) Color(0xFF252525) else LightColorScheme.background)
                .align(Alignment.BottomCenter)
                .heightIn(max = 600.dp),
        ) {
            IconButton(
                onClick = { onClose() }, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .padding(vertical = if (index == 2) 0.dp else 25.dp)
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
                                if (index == IMPORTSTATE.STARTED) "Analyzing file contents"
                                else "Almost completed, please wait..",
                                LottieConstants.IterateForever
                            )

                            IMPORTSTATE.UNSUPPORTEDFILE -> Triple(
                                "file_error.json",
                                "Unsupported file format",
                                1
                            )

                            IMPORTSTATE.SUCCESS -> Triple(
                                "file_success.json",
                                "Successfully imported",
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
                            modifier = Modifier.size(250.dp)
                        )

                        Text(
                            text = text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 25.dp, bottom = 25.dp)
                        )
                    }

                    2 -> {
                        var toggleSelectAll by remember { mutableStateOf(true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 18.dp, start = 20.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Media Found (${files.count { it.isSelected }}/${files.size})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight(500),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = formatFileSize(files.sumOf { if (it.isSelected) it.byteCount else 0L }),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 16.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "S.no",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.2f)
                            )
                            Text(
                                text = "Media files details",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(0.6f)
                            )
                            Box(
                                modifier = Modifier.weight(0.2f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Checkbox(checked = toggleSelectAll, onCheckedChange = {
                                    val new = files.toMutableList()
                                    files.forEachIndexed { i, item ->
                                        new[i] = item.copy(isSelected = it)
                                    }
                                    onUpdate(new)
                                    toggleSelectAll = it
                                }, colors = checkboxColors)
                            }
                        }
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(files) { i, file ->
                                ZipListItem(
                                    index = i,
                                    item = file,
                                    isSelected = file.isSelected,
                                    onCheckChange = {
                                        val new = files.toMutableList(); new[i] =
                                        files[i].copy(isSelected = it); onUpdate(new)
                                    },
                                    checkboxColors = checkboxColors
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 1.dp)
                                .padding(3.dp)
                                .padding(top = 3.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight(500),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x4B777777))
                                    .clickable { onMediaSave(false) }
                                    .padding(12.dp)
                            )
                            Text(
                                text = "Keep",
                                fontSize = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LightColorScheme.primary)
                                    .clickable { onMediaSave(true) }
                                    .padding(12.dp)
                            )
                        }
                    }

                    4 -> {

                    }
                }
            }
        }
    }
}