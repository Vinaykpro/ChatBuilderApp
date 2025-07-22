package com.vinaykpro.chatbuilder.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.screens.theme.rememberCustomIconPainter
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun ThemeItem(
    context: Context = LocalContext.current,
    selected: Boolean = false,
    id: Int = 1,
    name: String = "Default theme",
    author: String = "Vinaykpro",
    iconColor: Color = LightColorScheme.primary,
    onClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(top = 10.dp, start = 20.dp, end = 20.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
            .height(74.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val themeIcon = rememberCustomIconPainter(id, "icon.png", 0, R.drawable.logo)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = { onClick() },
                colors = RadioButtonColors(
                    selectedColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    unselectedColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledSelectedColor = RadioButtonDefaults.colors().disabledSelectedColor,
                    disabledUnselectedColor = RadioButtonDefaults.colors().disabledSelectedColor
                )
            )
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor)
                    .padding(8.dp),
                painter = themeIcon,
                contentDescription = null,
                tint = Color.White
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
        Box(
            modifier = Modifier
                .height(74.dp)
                .width(60.dp)
                .clickable { onClick(); onNextClick() }) {
            Spacer(
                Modifier
                    .padding(top = 17.dp)
                    .height(40.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
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