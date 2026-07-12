package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopKeywordsLayout(
    keywords: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    isEmoji: Boolean = false
) {
    val colors = listOf(
        Color(0xFFE7F8FC),
        Color(0xFFEAF1FF),
        Color(0xFFF2ECFF),
        Color(0xFFFFF0E8),
        Color(0xFFEAF6EA),
        Color(0xFFFFF7E7)
    )

    val max = keywords.maxOfOrNull { it.second } ?: 1

    Column(modifier.padding(12.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            keywords.forEachIndexed { index, item ->

                val bg = colors[index % colors.size]
                val alpha = 0.45f + (item.second / max.toFloat()) * 0.35f

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isDark) MaterialTheme.colorScheme.background else bg.copy(
                                alpha = alpha
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.first,
                        fontSize = if (isEmoji) 30.sp else 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F6FB5)
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = item.second.toString(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}