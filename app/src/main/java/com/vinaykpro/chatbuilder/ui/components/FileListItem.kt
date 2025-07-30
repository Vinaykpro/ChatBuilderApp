package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.FileEntity
import com.vinaykpro.chatbuilder.data.local.MessageEntity

@Preview
@Composable
fun FileListItem(
    msg: MessageEntity? = null,
    file: FileEntity? = FileEntity(),
    onClick: () -> Unit = {}
) {
    val icon = painterResource(
        when (file?.type) {
            FILETYPE.IMAGE -> R.drawable.ic_imagefile
            FILETYPE.VIDEO -> R.drawable.ic_videofile
            FILETYPE.AUDIO -> R.drawable.ic_audiofile
            FILETYPE.ZIP -> R.drawable.ic_zipfile
            else -> R.drawable.ic_anyfile
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file?.displayname ?: "File not found",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 17.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                style = TextStyle(
                    lineBreak = LineBreak.Simple
                )
            )
            Text(
                text = file?.size ?: "",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Text(
            text = msg?.date ?: "",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxHeight()
        )
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer)
    )
}
