package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.data.local.MessageBarStyle

@Preview
@Composable
fun ChatMessageBar(
    placeholder: String = "Message",
    value: String = "",
    icon1: Painter? = painterResource(R.drawable.ic_file),
    icon2: Painter? = painterResource(R.drawable.ic_camera),
    icon3: Painter? = null,
    style: MessageBarStyle = MessageBarStyle(),
    isDarkTheme: Boolean = false,
    preview: Boolean = false,
    previewColors: ParsedMessageBarStyle = ParsedMessageBarStyle(),
    previewAttrs: MessageBarStyle = MessageBarStyle()
) {
    var input by remember { mutableStateOf(value) }
    val themeColors = if(preview) previewColors else remember(style, isDarkTheme) {
        style.toParsed(isDarkTheme)
    }
    val style = if(preview) previewAttrs else style
    Row(modifier = Modifier.padding(bottom = if(preview) 0.dp else WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        .height(52.dp).fillMaxWidth().background(themeColors.widgetBackground)
        , verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.padding(start = 2.dp).height(46.dp).weight(1f).clip(RoundedCornerShape(40.dp)).background(themeColors.barBackground), verticalAlignment = Alignment.CenterVertically) {
            if(style.showleftinnerbutton)
                Icon(painter = painterResource(R.drawable.ic_emoji), contentDescription = null, tint = themeColors.leftInnerButtonIcon,
                modifier = Modifier.padding(horizontal = 3.dp).size(42.dp).clip(CircleShape).background(themeColors.leftInnerButton).padding(8.dp))
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
                Icon(painter = icon1, contentDescription = null, tint = themeColors.colorIcons,
                    modifier = Modifier.padding(horizontal = 10.dp).size(22.dp))
            if(icon2!=null)
                Icon(painter = icon2, contentDescription = null, tint = themeColors.colorIcons,
                    modifier = Modifier.padding(horizontal = 10.dp).size(22.dp))
            if(icon3!=null)
                Icon(painter = icon3, contentDescription = null, tint = themeColors.colorIcons,
                    modifier = Modifier.padding(horizontal = 10.dp).size(22.dp))

            if(style.showrightinnerbutton)
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = themeColors.rightInnerButtonIcon,
                modifier = Modifier.padding(horizontal = 3.dp).size(42.dp).clip(CircleShape).background(themeColors.rightInnerButton).padding(10.dp))
            else Spacer(modifier = Modifier.size(10.dp))
        }
        if(style.showouterbutton)
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = themeColors.outerButtonIcon,
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
        outerButtonIcon = parse(if(isDarkTheme) color_outerbutton_icon_dark else color_outerbutton_icon),
        rightInnerButtonIcon = parse(if(isDarkTheme) color_rightinnerbutton_icon_dark else color_rightinnerbutton_icon),
        leftInnerButtonIcon = parse(if(isDarkTheme) color_leftinnerbutton_icon_dark else color_leftinnerbutton_icon),
        colorIcons = parse(if(isDarkTheme) color_icons_dark else color_icons),
        inputText = parse(if(isDarkTheme) color_inputtext_dark else color_inputtext),
        hintText = parse(if(isDarkTheme) color_hinttext_dark else color_hinttext),
    )
}

data class ParsedMessageBarStyle(
    val widgetBackground: Color = Color.Transparent,
    val barBackground: Color = Color.Transparent,
    val outerButton: Color = Color.Transparent,
    val outerButtonIcon: Color = Color.Transparent,
    val rightInnerButton: Color = Color.Transparent,
    val rightInnerButtonIcon: Color = Color.Transparent,
    val leftInnerButton: Color = Color.Transparent,
    val leftInnerButtonIcon: Color = Color.Transparent,
    val colorIcons: Color = Color.Transparent,
    val inputText: Color = Color.Transparent,
    val hintText: Color = Color.Transparent,
)
