package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun AddUserWidget(
    onAdd: (String) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var input by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,

        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,

        cursorColor = LightColorScheme.primary,
        selectionColors = TextSelectionColors(
            handleColor = LightColorScheme.primary,
            backgroundColor = LightColorScheme.primary.copy(alpha = 0.4f)
        ),

        focusedIndicatorColor = LightColorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x59000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.onSurface)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add User",
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
            Row {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        if(it.length < 15)
                        input = it
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(start = 12.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    colors = colors
                )

                IconButton(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = {
                        if(input.trim().isNotEmpty()) onAdd(input)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tick),
                        contentDescription = null,
                        tint = if(input.trim().isNotEmpty()) LightColorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}