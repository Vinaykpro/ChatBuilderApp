package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun GlobalSearchItem(
    chatName: String = "Vinaykpro",
    senderName: String = "Vinay",
    message: String = "Hii",
    date: String = "2/1/2004",
    searchTerm: String? = "vinay",
    onClick: () -> Unit = {}
) {
    val color = MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp)
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = chatName,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            val message = if(searchTerm == null) buildAnnotatedString { append(message) }
            else buildAnnotatedString {
                var startIndex = 0
                val lowerFull = message.lowercase()
                val lowerSearch = searchTerm.lowercase()

                append("$senderName: ")

                while (startIndex < lowerFull.length) {
                    val index = lowerFull.indexOf(lowerSearch, startIndex)
                    if (index == -1) {
                        append(message.substring(startIndex))
                        break
                    }

                    append(message.substring(startIndex, index))

                    withStyle(
                        SpanStyle(
                            color = color,
                            fontWeight = FontWeight(600)
                        )
                    ) {
                        append(message.substring(index, index + searchTerm.length))
                    }
                    startIndex = index + searchTerm.length
                }
            }

            Text(
                text = message,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(
            text = date,
            style = TextStyle(
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.height(40.dp)
        )
    }
}