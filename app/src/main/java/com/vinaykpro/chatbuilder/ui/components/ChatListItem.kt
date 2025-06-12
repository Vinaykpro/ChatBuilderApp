package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R

@Preview
@Composable
fun ChatListItem(
    icon: Int = -1,
    name: String = "Vinay",
    lastMessage: String = "Somemsg",
    lastSeen: String = "12:15",
    navController: NavController = rememberNavController(),
    isForceFake: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = { navController.navigate("chat") })
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = R.drawable.user), contentDescription = "icon", modifier = Modifier.size(42.dp))
        Column(modifier = Modifier
            .padding(start = 10.dp)
            .weight(1f)) {
            Text(text = name, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(500), color = if(!isForceFake) MaterialTheme.colorScheme.onPrimaryContainer else Color.White ))
            Text(text = lastMessage, style = TextStyle(fontSize = 13.sp, color = if(!isForceFake) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray))
        }
        Text(text = lastSeen, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight(500), color = if(!isForceFake) MaterialTheme.colorScheme.onSecondaryContainer else Color.LightGray))
    }
}