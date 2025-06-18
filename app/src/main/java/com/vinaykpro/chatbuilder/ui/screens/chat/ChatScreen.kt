package com.vinaykpro.chatbuilder.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.components.SenderMessage


var messageWidthLimit : Double = 0.0;
@Preview(showBackground = true)
@Composable
fun ChatScreen() {
    val screenWidth = getScreenWidthInDp();
    messageWidthLimit = screenWidth.value * 0.8;
    val sampleItems = listOf(
        "Hii",
        "Hello world",
        "sfddfbdfbfbdbdfdfdfggdfgdfgdsfgdsjgfdgfsdgfuksgdfukgsdfukgsdgfdsukfgdsukgfuksgfudsgfukdsgfukgdskfgdsukg",
        "Hello world somewhat",
        "Hello world somewhat big",
        "Hello world somewhat extra big",
        "Hello world somewhat very extra big",
        "Hello world somewhat very very extra big",
        "Hello world somewhat very very extra big then previous",
        "Hello world somewhat very very extra big then previous and some extension",
        "Hello world somewhat very very extra big then previous and some extension and one more additional extension",
        "Hello world",
        "sfddfbdfbfbdbdfdfdfggdfgdfgdsfgdsjgfdgfsdgfuksgdfukgsdfukgsdgfdsukfgdsukgfuksgfudsgfukdsgfukgdskfgdsukg",
        "Hello world somewhat",
        "Hello world somewhat big",
        "Hello world somewhat extra big",
        "Hello world somewhat very extra big",
        "Hello world somewhat very very extra big",
        "Hello world somewhat very very extra big then previous",
        "Hello world somewhat very very extra big then previous and some extension",
        "Hello world somewhat very very extra big then previous and some extension and one more additional extension",
    )
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .background(Color.White), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            Image(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(24.dp))
            Image(painter = painterResource(id = R.drawable.user), contentDescription = "profile", modifier = Modifier.size(34.dp))
            Column(modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
                .padding(vertical = 10.dp)
            ) {
                Text(text = "Vinaykpro", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(400), color = Color.Black))
                Text(text = "online", style = TextStyle(fontSize = 12.sp, color = Color(0xFF444444)))
            }
            Row {
                Image(imageVector = Icons.Default.Call, contentDescription = "Voice", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp))
                Image(imageVector = Icons.Default.Star, contentDescription = "Star", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp))
                Image(imageVector = Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp))
            }
        }

        //body
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(Color.LightGray)
        ) {
            items(items = sampleItems) { item ->
                //  if((0..1).random() == 0)
                SenderMessage(text = item, sentTime = "1:00 PM")
                Message(text = item, time = "1:00 PM")
            }
        }

        //input
        Row() {

        }
    }
}

@Composable
fun Message() {

}

val TriangleShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(0f, size.height)
    close()
}

@Composable
fun SentArrow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(15.dp) // Consistent size
            .padding(end = 4.dp)
            .graphicsLayer {
                rotationZ = 70f // Rotation angle
                translationY = -10f
            }
            .background(color = Color(0xFFE1FFC7), shape = TriangleShape)
    )
}

@Composable
fun MessageArrow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(15.dp) // Consistent size
            .padding(start = 4.dp)
            .graphicsLayer {
                rotationZ = -70f // Rotation angle
                translationY = -10f
            }
            .background(color = Color.White, shape = TriangleShape)
    )
}

@Composable
fun Message(text: String, time: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .padding(1.dp)
                .padding(start = 10.dp)
                .widthIn(max = messageWidthLimit.dp)
                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                .padding(vertical = 4.dp, horizontal = 10.dp)
                .align(Alignment.CenterStart)
        ) {
            // Message text
            Text(
                text = text + " ⠀ ⠀ ⠀", // Extra spaces for spacing
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )

            // Sent time
            Text(
                text = time,
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        // Arrow at the top-end
        MessageArrow(modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
fun getScreenWidthInDp(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}
