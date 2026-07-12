package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.data.local.formatShort
import kotlin.math.abs

@Composable
fun ChartLayout(
    points: List<Pair<String, Int>>,
    modifier: Modifier = Modifier.background(Color.White),
    labels: List<String>? = null
) {
    var selectedIndex by remember { mutableStateOf(-1) }

    val maxValue = points.maxOfOrNull { it.second } ?: 0

    if (points.isEmpty())
        Text(
            text = "Not enough data to display graph! Choose another chat",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textAlign = TextAlign.Center
        )
    else
        Column(modifier) {
            val leftPadding = 30.dp
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->

                            val spacing = (size.width - leftPadding.toPx()) / (points.size - 1)

                            val nearest = points.indices.minByOrNull { index ->
                                val pointX = leftPadding.toPx() + index * spacing
                                abs(offset.x - pointX)
                            }
                            if (selectedIndex == nearest) selectedIndex = -1
                            else selectedIndex = nearest ?: -1
                        }
                    }
            ) {

                val chartHeight = size.height - 10.dp.toPx()
                val spacing = (size.width - leftPadding.toPx()) / (points.size - 1)

                val Pairs = points.mapIndexed { index, point ->

                    val x = leftPadding.toPx() + index * spacing

                    val y =
                        chartHeight - (point.second.toFloat() / maxValue) * chartHeight

                    Offset(x, y)
                }

                // Grid lines
                repeat(5) { i ->

                    val y = chartHeight / 4 * i

                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(leftPadding.toPx(), y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Count labels
                repeat(5) { i ->

                    val value = maxValue - (maxValue / 4f * i)

                    val y = chartHeight / 4 * i

                    drawContext.canvas.nativeCanvas.drawText(
                        value.toInt().formatShort(),
                        0f,
                        y + 5.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.LEFT
                            isAntiAlias = true
                        }
                    )
                }

                // Smooth curve
                val path = Path().apply {

                    moveTo(Pairs.first().x, Pairs.first().y)

                    for (i in 1 until Pairs.size) {

                        val p1 = Pairs[i - 1]
                        val p2 = Pairs[i]

                        cubicTo(
                            p1.x + spacing / 2,
                            p1.y,
                            p2.x - spacing / 2,
                            p2.y,
                            p2.x,
                            p2.y
                        )
                    }
                }

                // Area below line
                val fillPath = Path().apply {
                    addPath(path)

                    lineTo(Pairs.last().x, chartHeight)
                    lineTo(Pairs.first().x, chartHeight)
                    close()
                }


                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF0DA4B5).copy(alpha = .35f),
                            Color.Transparent
                        )
                    )
                )


                drawPath(
                    path,
                    color = Color(0xFF0DA4B5),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )


                // Vertical selected line
                if (selectedIndex != -1) {

                    val point = Pairs[selectedIndex]

                    drawLine(
                        color = Color.Black.copy(alpha = .5f),
                        strokeWidth = 2f,
                        start = Offset(point.x, 0f),
                        end = Offset(point.x, chartHeight),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                // Dots
                Pairs.forEachIndexed { index, point ->

                    drawCircle(
                        color = if (index == selectedIndex)
                            Color(0xFF0DA4B5)
                        else
                            Color.White,
                        radius = if (index == selectedIndex)
                            8.dp.toPx()
                        else if (points.size > 12) 3.dp.toPx()
                        else 4.dp.toPx(),
                        center = point
                    )

                    drawCircle(
                        color = Color(0xFF0DA4B5),
                        radius = if (index == selectedIndex)
                            8.dp.toPx()
                        else if (points.size > 12) 3.dp.toPx()
                        else 4.dp.toPx(),
                        center = point,
                        style = Stroke(if (points.size <= 12) 3.dp.toPx() else 2.dp.toPx())
                    )
                }
            }


            if (selectedIndex != -1)
                Text(
                    text = "${points[selectedIndex].first} • ${points[selectedIndex].second} messages",
                    modifier = Modifier
                        .padding(start = 10.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftPadding - 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (labels != null)
                    labels.forEach {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                else
                    points.forEach {
                        Text(
                            text = if (it.first.length > 3) it.first.substring(
                                0,
                                3
                            ) else it.first,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
            }
        }
}