package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun UserWordsItem(
    rank: Int = 1,
    name: String = "Vinay",
    color: Color = Color.Green,
    pairs: List<Pair<String, Int>> = listOf<Pair<String, Int>>(
        Pair("\uD83D\uDE18", 132),
        Pair("\uD83D\uDE18", 89),
        Pair("\uD83D\uDE24", 54),
        Pair("\uD83E\uDD73", 12),
    ),
    isDark: Boolean = false,
    isEmoji: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colors = listOf(
        Color(0xFFE7F8FC),
        Color(0xFFEAF1FF),
        Color(0xFFF2ECFF),
        Color(0xFFFFF0E8),
        Color(0xFFEAF6EA),
        Color(0xFFFFF7E7)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = .18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.width(8.dp))

            FlowRow(
                horizontalArrangement = if(isEmoji) Arrangement.SpaceEvenly else Arrangement.Start
            ) {
                if (isEmoji)
                    pairs.forEach {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = it.first,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(horizontal = 7.dp)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = it.second.toString(),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                else
                    pairs.forEachIndexed { index, item ->

                        val bg = colors[index % colors.size]
                        val alpha = 0.45f + (item.second / pairs[0].second.toFloat()) * 0.35f

                        Column(
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if(isDark) MaterialTheme.colorScheme.background else bg.copy(alpha = alpha))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.first,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F6FB5)
                            )
                            Text(
                                text = item.second.toString(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
            }
        }

        Spacer(Modifier.width(12.dp))
    }
}