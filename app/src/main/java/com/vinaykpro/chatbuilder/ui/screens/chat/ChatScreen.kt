package com.vinaykpro.chatbuilder.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
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
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.Message
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
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer)) {
        ChatToolbar()

        //body
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(Color.LightGray)
            .padding(horizontal = 5.dp)
        ) {
            items(items = sampleItems) { item ->
                //  if((0..1).random() == 0)
                SenderMessage(text = item, sentTime = "1:00 PM", bubbleStyle = 1, isFirst = true)
                Message(text = item, sentTime = "1:00 PM", bubbleStyle = 1, isFirst = true)
            }
        }

        //input
        ChatMessageBar()
    }
}

@Composable
fun getScreenWidthInDp(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}
