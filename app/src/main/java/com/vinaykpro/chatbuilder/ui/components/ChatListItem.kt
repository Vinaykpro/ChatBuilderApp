package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomProfileIconPainter

@Preview
@Composable
fun ChatListItem(
    id: Int? = null,
    name: String = "Vinay",
    lastMessage: String = "Somemsg",
    lastSeen: String = "12:15",
    onClick: () -> Unit = {},
    themeid: Int = 0,
    isForceFake: Boolean = false,
) {
    val profilePicPainter = rememberCustomProfileIconPainter(
        chatId = id,
        refreshKey = 0,
        fallback = R.drawable.user
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = profilePicPainter,
            contentDescription = "icon",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
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
                    color = if (!isForceFake) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                )
            )
            Text(
                text = lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = if (!isForceFake) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray
                )
            )
        }
        Text(
            text = lastSeen,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight(500),
                color = if (!isForceFake) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray
            ),
            modifier = Modifier.height(40.dp)
        )
    }
}