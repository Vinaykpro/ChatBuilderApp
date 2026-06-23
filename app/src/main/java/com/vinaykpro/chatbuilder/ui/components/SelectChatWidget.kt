package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.ChatEntity

@Preview
@Composable
fun SelectChatWidget(
    chat: ChatEntity? = null,
    pic: Painter? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (chat != null)
                    Modifier.border(
                        shape = RoundedCornerShape(5.dp),
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                else
                    Modifier.dashedBorder(1.5.dp, Color.LightGray, 4.dp, 2.dp, 5.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (chat != null) {
            Box() {
                ChatListItem(
                    id = chat.chatid,
                    name = chat.name,
                    pic = pic,
                    lastSeen = "",
                    lastMessage = "Great! You can tap again to change chat!",
                    onClick = { onClick() }
                )

                Image(
                    painter = painterResource(R.drawable.ic_check_round_prim),
                    contentDescription = "indicator",
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .align(Alignment.CenterEnd)
                        .size(28.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        } else {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "User profile icon",
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 10.dp)
                    .size(35.dp)
            )
            Column() {
                Text(
                    text = "Tap to select a chat",
                    fontSize = 15.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 2.dp, bottom = 0.dp)
                )
                Text(
                    text = "Tap here to change the color",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.ic_warn),
                contentDescription = "indicator",
                modifier = Modifier
                    .padding(end = 22.dp)
                    .size(25.dp)
            )
        }
    }
}


fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    dashWidth: Dp,
    gapWidth: Dp,
    cornerRadius: Dp = 0.dp
) = this.drawBehind {
    // 1. Convert Dp values to Pixels for the drawing canvas
    val strokeWidthPx = strokeWidth.toPx()
    val dashWidthPx = dashWidth.toPx()
    val gapWidthPx = gapWidth.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    // 2. Create the pattern: [dash_length, gap_length]
    val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
        intervals = floatArrayOf(dashWidthPx, gapWidthPx),
        phase = 0f
    )

    // 3. Draw the rounded or flat rectangle outline
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect
        ),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}