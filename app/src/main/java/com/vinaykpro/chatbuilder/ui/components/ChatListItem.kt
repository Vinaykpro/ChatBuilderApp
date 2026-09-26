package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun ChatListItem(
    id: Int? = null,
    name: String = "Vinay",
    pic: Painter? = null,
    lastMessage: String = "Somemsg",
    lastSeen: String = "12:15",
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
    isSelected: Boolean = false,
    showRipple: Boolean = true,
    isForceDark: Boolean = false,
    isPinned: Boolean = false,
    isFavorite: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val profilePicPainter = pic
        ?: rememberCustomProfileIconPainter(
            chatId = id,
            refreshKey = 0,
            fallback = R.drawable.user
        )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = if (showRipple) LocalIndication.current else null,
                onClick = { onClick() },
                onLongClick = { onLongPress() }
            )
            .background(if (isSelected) LightColorScheme.primary.copy(alpha = .25f) else Color.Transparent)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Image(
                painter = profilePicPainter,
                contentDescription = "icon",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(
                    animationSpec = tween(durationMillis = 200),
                    transformOrigin = TransformOrigin.Center
                ),
                exit = scaleOut(
                    animationSpec = tween(durationMillis = 150),
                    transformOrigin = TransformOrigin.Center
                ),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Spacer(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_msg_selected),
                        contentDescription = null,
                        tint = LightColorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight(500),
                    color = if (!isForceDark) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                )
            )
            Text(
                text = lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = if (!isForceDark) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = lastSeen,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(500),
                    color = if (!isForceDark) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray
                )
            )
            if (isPinned) {
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_pin),
                    contentDescription = "Pinned",
                    tint = if (!isForceDark) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}