package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import androidx.core.graphics.toColorInt

@Preview
@Composable
fun ChatMessageBar(
    placeholder: String = "Message",
    value: String = "",
    icon1: Painter? = painterResource(R.drawable.ic_starredmessages),
    icon2: Painter? = painterResource(R.drawable.ic_replace),
    icon3: Painter? = painterResource(R.drawable.ic_eyeoff),
    style: MessageBarStyle = MessageBarStyle(),
    isDarkTheme: Boolean = false,
    preview: Boolean = false
) {
    var input by remember { mutableStateOf(value) }
    val themeColors = remember(style, isDarkTheme) {
        style.toParsed(isDarkTheme)
    }
    Row(modifier = Modifier.padding(bottom = if(preview) 0.dp else WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        .height(52.dp).fillMaxWidth().background(themeColors.widgetBackground)
        , verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.padding(start = 2.dp).height(46.dp).weight(1f).clip(RoundedCornerShape(40.dp)).background(themeColors.barBackground), verticalAlignment = Alignment.CenterVertically) {
            if(style.showleftinnerbutton)
                Icon(painter = painterResource(R.drawable.ic_call), contentDescription = null, tint = Color.Black,
                modifier = Modifier.padding(start = 3.dp).size(42.dp).clip(CircleShape).background(themeColors.leftInnerButton).padding(10.dp))
            else Spacer(modifier = Modifier.width(15.dp))
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    color = themeColors.inputText
                ),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(start = 0.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 0.dp, vertical = 0.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (input.isEmpty()) {
                            Text(text = placeholder, color = themeColors.hintText)
                        }
                        innerTextField()
                    }
                }
            )


            if(icon1!=null)
                Icon(painter = icon1, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))
            if(icon2!=null)
                Icon(painter = icon2, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))
            if(icon3!=null)
                Icon(painter = icon3, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))

            if(style.showrightinnerbutton)
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White,
                modifier = Modifier.padding(horizontal = 3.dp).size(42.dp).clip(CircleShape).background(themeColors.rightInnerButton).padding(10.dp))
            else Spacer(modifier = Modifier.size(10.dp))
        }
        if(style.showouterbutton)
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White,
                modifier = Modifier.padding(horizontal = 3.dp).size(46.dp).clip(CircleShape).background(themeColors.outerButton).padding(12.dp))
    }
}


fun MessageBarStyle.toParsed(isDarkTheme: Boolean): ParsedMessageBarStyle {
    fun parse(hex: String): Color = Color(hex.toColorInt())

    return ParsedMessageBarStyle(
        widgetBackground = parse(if(isDarkTheme) color_widgetbackground_dark else color_widgetbackground),
        barBackground = parse(if(isDarkTheme) color_barbackground_dark else color_barbackground),
        outerButton = parse(if(isDarkTheme) color_outerbutton_dark else color_outerbutton),
        rightInnerButton = parse(if(isDarkTheme) color_rightinnerbutton_dark else color_rightinnerbutton),
        leftInnerButton = parse(if(isDarkTheme) color_leftinnerbutton_dark else color_leftinnerbutton),
        inputText = parse(if(isDarkTheme) color_inputtext_dark else color_inputtext),
        hintText = parse(if(isDarkTheme) color_hinttext_dark else color_hinttext),
    )
}

data class ParsedMessageBarStyle(
    val widgetBackground: Color,
    val barBackground: Color,
    val outerButton: Color,
    val rightInnerButton: Color,
    val leftInnerButton: Color,
    val inputText: Color,
    val hintText: Color,
)
