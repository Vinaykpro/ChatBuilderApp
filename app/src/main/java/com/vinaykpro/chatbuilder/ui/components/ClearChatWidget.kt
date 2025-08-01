package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun ClearChatWidget(
    onClear: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    var deletionStarted by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x59000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(deletionStarted) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Clearing up data",
                        fontSize = 18.sp,
                        fontWeight = FontWeight(500),
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Text(
                    text = "Clear this chat?",
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "This action removes all the messages and media in this chat from your device",
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(500),
                        color = LightColorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onCancel()
                            }
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                    Text(
                        text = "Clear chat",
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(500),
                        color = LightColorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                deletionStarted = true
                                onClear()
                            }
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
            }
        }
    }
}