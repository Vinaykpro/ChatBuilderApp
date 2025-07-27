package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.UserInfo

@Preview
@Composable
fun SwapSenderWidget(
    users: List<UserInfo> = emptyList(),
    currentId: Int = 0,
    onSenderChange: (Int) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var names = mutableListOf<String>()
    users.forEachIndexed { ind, item ->
        if (item.userid == currentId) currentIndex = ind
        names.add(item.username)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier.padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Change Sender",
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
            if (names.size == users.size) {
                WheelPicker(
                    names,
                    visibleExtrasCount = 1,
                    selectedIndex = currentIndex,
                    onItemChange = {
                        onSenderChange(users[it].userid)
                    }
                )
            }
        }
    }
}