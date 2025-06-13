package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vinaykpro.chatbuilder.R

@Preview
@Composable
fun EditIcon(
    size: Int = 75,
    onClick: () -> Unit = {}
) {
    Column(modifier = Modifier.width(size.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Image(painter = painterResource(R.drawable.iconalpha), contentDescription = null, modifier = Modifier.size(size.dp).clip(shape = RoundedCornerShape(10.dp)).background(Color(0xFF31ABBB)))
        Box(modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(10.dp)).border(0.5.dp, color = MaterialTheme.colorScheme.onSecondaryContainer, shape = RoundedCornerShape(10.dp)).clickable {  }.padding(8.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp).align(Alignment.Center)
            )
        }
    }
}