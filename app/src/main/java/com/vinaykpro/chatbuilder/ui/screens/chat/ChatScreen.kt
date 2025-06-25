package com.vinaykpro.chatbuilder.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.data.local.BodyStyle
import com.vinaykpro.chatbuilder.data.local.HeaderStyle
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.Message
import com.vinaykpro.chatbuilder.ui.components.SenderMessage
import com.vinaykpro.chatbuilder.ui.theme.LocalThemeEntity
import kotlinx.serialization.json.Json


@Preview(showBackground = true)
@Composable
fun ChatScreen(isDarkTheme: Boolean = false) {
    val theme = LocalThemeEntity.current
    val appColor = remember(theme.appcolor, theme.appcolordark, isDarkTheme) {
        val colorHex = if (isDarkTheme) theme.appcolordark else theme.appcolor
        Color(colorHex.toColorInt())
    }
    val headerStyle = remember(theme.headerstyle) {
        try {
            Json.decodeFromString<HeaderStyle>(theme.headerstyle)
        } catch (_: Exception) {
            HeaderStyle()
        }
    }
    val bodyStyle = remember(theme.bodystyle) {
        try {
            Json.decodeFromString<BodyStyle>(theme.bodystyle)
        } catch (_: Exception) {
            BodyStyle()
        }
    }
    val themeBodyColors = remember(bodyStyle, isDarkTheme) {
        bodyStyle.toParsed(isDarkTheme)
    }
    val messageBarStyle = remember(theme.messagebarstyle) {
        try {
            Json.decodeFromString<MessageBarStyle>(theme.messagebarstyle)
        } catch (_: Exception) {
            MessageBarStyle()
        }
    }
    val sampleItems = remember {
        listOf(
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
    }
    Column(modifier = Modifier.fillMaxSize()
        .background(themeBodyColors.chatBackground)
        .padding(bottom = WindowInsets.ime
            .only(WindowInsetsSides.Bottom)
            .exclude(WindowInsets.navigationBars)
            .asPaddingValues()
            .calculateBottomPadding())) {
        ChatToolbar(style = headerStyle, isDarkTheme = isDarkTheme)

        //body
        LazyColumn(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 5.dp)
        ) {
            items(items = sampleItems) { item ->
                SenderMessage(text = item, sentTime = "1:00 PM", bubbleStyle = bodyStyle.bubble_style, bubbleTipRadius = bodyStyle.bubble_tip_radius, isFirst = true, color = themeBodyColors.senderBubble, textColor = themeBodyColors.textPrimary, hintTextColor = themeBodyColors.textSecondary)
                Message(text = item, sentTime = "1:00 PM", bubbleStyle = bodyStyle.bubble_style, bubbleTipRadius = bodyStyle.bubble_tip_radius, isFirst = true, color = themeBodyColors.receiverBubble, textColor = themeBodyColors.textPrimary, hintTextColor = themeBodyColors.textSecondary)
            }
        }

        //input
        ChatMessageBar(style = messageBarStyle, isDarkTheme = isDarkTheme)
    }
}


fun BodyStyle.toParsed(isDarkTheme: Boolean): ParsedBodyStyle {
    fun parse(hex: String): Color = Color(hex.toColorInt())

    return ParsedBodyStyle(
        chatBackground = parse(if(isDarkTheme) color_chatbackground_dark else color_chatbackground),
        senderBubble = parse(if(isDarkTheme) color_senderbubble_dark else color_senderbubble),
        receiverBubble = parse(if(isDarkTheme) color_receiverbubble_dark else color_receiverbubble),
        dateText = parse(if(isDarkTheme) color_datetext_dark else color_datetext),
        textPrimary = parse(if(isDarkTheme) color_text_primary_dark else color_text_primary),
        textSecondary = parse(if(isDarkTheme) color_text_secondary_dark else color_text_secondary)
    )
}

data class ParsedBodyStyle(
    val chatBackground: Color,
    val senderBubble: Color,
    val receiverBubble: Color,
    val dateText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)