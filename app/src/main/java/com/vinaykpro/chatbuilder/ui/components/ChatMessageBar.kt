package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun ChatMessageBar(
    placeholder: String = "Message",
    value: String = "",
    isStartIconVisible: Boolean = false,
    isEndIconVisible: Boolean = false,
    isSendIconVisible: Boolean = true,
    icon1: Painter? = painterResource(R.drawable.ic_starredmessages),
    icon2: Painter? = painterResource(R.drawable.ic_replace),
    icon3: Painter? = painterResource(R.drawable.ic_eyeoff),
    preview: Boolean = false
) {
    var input by remember { mutableStateOf(value) }
    val inputFieldColors: TextFieldColors = TextFieldDefaults.colors().copy(
        focusedIndicatorColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        unfocusedIndicatorColor =  Color.Transparent
    )
    Row(modifier = Modifier.padding(bottom = if(preview) 0.dp else WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        .height(58.dp).fillMaxWidth().background(Color.Transparent)
        , verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.padding(start = 2.dp).height(54.dp).weight(1f).clip(RoundedCornerShape(40.dp)).background(MaterialTheme.colorScheme.background), verticalAlignment = Alignment.CenterVertically) {
            if(isStartIconVisible)
                Icon(painter = painterResource(R.drawable.ic_call), contentDescription = null, tint = Color.White,
                modifier = Modifier.padding(start = 3.dp).size(50.dp).clip(CircleShape).background(LightColorScheme.primary).padding(10.dp))

            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSecondaryContainer) },
                textStyle = TextStyle(fontSize = 18.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer),
                colors = inputFieldColors,
                modifier = Modifier.fillMaxHeight().weight(1f))

            if(icon1!=null)
                Icon(painter = icon1, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))
            if(icon2!=null)
                Icon(painter = icon2, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))
            if(icon3!=null)
                Icon(painter = icon3, contentDescription = null, tint = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp).size(18.dp))

            if(isEndIconVisible)
                Icon(painter = painterResource(R.drawable.ic_call), contentDescription = null, tint = Color.White,
                modifier = Modifier.padding(horizontal = 3.dp).size(50.dp).clip(CircleShape).background(LightColorScheme.primary).padding(10.dp))
            else Spacer(modifier = Modifier.size(10.dp))
        }
        Icon(painter = painterResource(R.drawable.ic_call), contentDescription = null, tint = Color.White,
            modifier = Modifier.padding(horizontal = 3.dp).size(54.dp).clip(CircleShape).background(LightColorScheme.primary).padding(10.dp))
    }
}