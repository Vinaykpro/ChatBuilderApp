package com.vinaykpro.chatbuilder.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R

@SuppressLint("SuspiciousIndentation")
@Preview
@Composable
fun Input(
    name: String = "Name: ",
    value: String = "Default",
    placeholder: String = "Enter here",
    disabledColor: Color = Color(0x257B7B7B),
    disabledTextColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onUpdate: (String) -> Unit = {}
) {
    var input by remember { mutableStateOf(value) }
    var enabled by remember { mutableStateOf(false) }
    val inputFieldColors: TextFieldColors = TextFieldDefaults.colors().copy(
        focusedIndicatorColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = disabledColor,
        disabledTextColor = disabledTextColor
    )
    TextField(
        value = input,
        enabled = enabled,
        onValueChange = { input = it },
        label = { Text(name, color = disabledTextColor) },
        placeholder = { Text(placeholder, color = disabledTextColor) },
        textStyle = TextStyle(fontSize = 15.sp),
        colors = inputFieldColors,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                onUpdate(input)
            }
        ),
        trailingIcon = {
            IconButton(onClick = {
                if (enabled) onUpdate(input)
                enabled = !enabled
            }) {
                Icon(
                    painter = painterResource(if (enabled) R.drawable.ic_tick else R.drawable.ic_edit),
                    contentDescription = "edit",
                    modifier = Modifier.size(20.dp),
                    tint = disabledTextColor
                )
            }

        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                1.dp,
                color = disabledTextColor,
                shape = RoundedCornerShape(12.dp)
            )
    )
}