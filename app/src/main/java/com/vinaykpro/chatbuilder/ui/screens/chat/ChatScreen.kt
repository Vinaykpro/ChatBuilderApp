package com.vinaykpro.chatbuilder.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity


@Preview(showBackground = true)
@Composable
fun ChatScreen(isDarkTheme: Boolean = false) {
    val themeColors = LocalThemeEntity.current
    val appColor = remember(themeColors.appcolor, themeColors.appcolordark, isDarkTheme) {
        val colorHex = if (isDarkTheme) themeColors.appcolordark else themeColors.appcolor
        Color(colorHex.toColorInt())
    }
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
        ChatToolbar(color = appColor)

        //body
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(Color.LightGray)
            .padding(horizontal = 5.dp)
        ) {
            items(items = sampleItems) { item ->
                SenderMessage(text = item, sentTime = "1:00 PM", bubbleStyle = 1, isFirst = true)
                Message(text = item, sentTime = "1:00 PM", bubbleStyle = 1, isFirst = true)
            }
        }

        //input
        ChatMessageBar()
    }
}