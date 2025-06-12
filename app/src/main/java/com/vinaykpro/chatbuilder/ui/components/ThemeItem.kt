package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vinaykpro.chatbuilder.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun ThemeItem(
    selected: Boolean = false,
    name: String = "Default theme",
    author: String = "Vinaykpro",
    onClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    Row(modifier = Modifier
        .padding(top = 10.dp, start = 20.dp, end = 20.dp)
        .fillMaxWidth()
        .clickable { onClick() }
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
        .height(74.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.fillMaxHeight().weight(1f).clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = { onClick() }, colors = RadioButtonColors(selectedColor = MaterialTheme.colorScheme.onTertiaryContainer, unselectedColor = MaterialTheme.colorScheme.secondaryContainer, disabledSelectedColor = RadioButtonDefaults.colors().disabledSelectedColor, disabledUnselectedColor = RadioButtonDefaults.colors().disabledSelectedColor))
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                painter = painterResource(R.drawable.icon),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 20.sp
                )
                Text(
                    text = "Made by: $author",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 16.sp
                )
            }
        }
        Box(modifier = Modifier
            .height(74.dp)
            .width(60.dp)
            .clickable { onNextClick() }) {
            Spacer(Modifier
                .padding(top = 17.dp)
                .height(40.dp)
                .width(1.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer))
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
                painter = painterResource(R.drawable.ic_nextarrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondaryContainer
            )

        }
    }
}