package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.DateInfo
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun DateNavigationWidget(
    dates: List<DateInfo> = emptyList(),
    currentDate: String = "2/1/2004",
    onNavigation: (Int) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var names = mutableListOf<String>()
    var currentDateId by remember { mutableIntStateOf(0) }
    dates.forEachIndexed { ind, item ->
        if (item.date == currentDate) {
            currentIndex = ind
            currentDateId = item.messageId
        }
        names.add(item.date)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x59000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.onSurface)
        ) {
            Row(
                modifier = Modifier.padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Navigate to Date",
                    fontSize = 20.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onClose() }, modifier = Modifier.padding(7.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (names.size == dates.size) {
                WheelPicker(
                    names,
                    visibleExtrasCount = 3,
                    selectedIndex = currentIndex,
                    onItemChange = {
                        if(dates.isNotEmpty())
                        currentDateId = dates[it].messageId
                    }
                )
            } else {
                CircularProgressIndicator()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp)
                    .padding(3.dp)
                    .padding(top = 3.dp)
            ) {
                Text(
                    text = "Close",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x4B777777))
                        .clickable { onClose() }
                        .padding(12.dp)
                )
                Text(
                    text = "Go",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightColorScheme.primary)
                        .clickable {
                            onNavigation(currentDateId)
                            onClose()
                        }
                        .padding(12.dp)
                )
            }
        }
    }
}