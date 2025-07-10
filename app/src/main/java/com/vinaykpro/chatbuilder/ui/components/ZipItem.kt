package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.FILETYPE
import com.vinaykpro.chatbuilder.data.local.ZipItem

@Preview
@Composable
fun ZipListItem(
    index: Int = 0,
    item: ZipItem = ZipItem(
        name = "IMG-20250530_1651.JPG",
        index = 0,
        type = FILETYPE.IMAGE,
        byteCount = 3452,
        size = "34.52KB"
    ),
    isSelected: Boolean = false,
    onCheckChange: (Boolean) -> Unit = {},
    checkboxColors: CheckboxColors = CheckboxDefaults.colors()
) {
    val fileIcon = painterResource(
        when (item.type) {
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
            .padding(vertical = 1.dp)
            .background(color = if (isSelected) Color(0x518D8D8D) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckChange(!isSelected) })
            .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}.",
            modifier = Modifier.weight(0.2f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Icon(
            painter = fileIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(end = 7.dp)
                .size(30.dp)
        )
        Column(modifier = Modifier.weight(0.5f)) {
            Text(
                text = item.name,//+"sdvbdsvuidsbvdsbuviscgcghchgcdbvbd",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                softWrap = false
            )
            Text(
                text = item.size,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.CenterStart) {
            Checkbox(checked = isSelected, onCheckedChange = onCheckChange, colors = checkboxColors)
        }
    }
}