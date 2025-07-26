package com.vinaykpro.chatbuilder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinaykpro.chatbuilder.R

@Preview
@Composable
fun SearchBar(
    backgroundColor: Color = Color.White,
    color: Color = Color.Black,
    showArrows: Boolean = false,
    modifier: Modifier = Modifier,
    onExit: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onNext: () -> Unit = {},
    onPrev: () -> Unit = {},
    resultsLength: Int = 0,
    currentResultIndex: Int = 0
) {
    var input by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(30.dp))
                .background(color.copy(alpha = 0.25f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                    tint = color
                )
            }
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    color = color
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .height(40.dp)
                    .weight(1f),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        val text = input.trim()
                        if (text.isEmpty()) return@KeyboardActions
                        onSearch(text)
                        keyboardController?.hide()
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 0.dp, vertical = 0.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (input.isEmpty()) {
                            Text(text = "Search...", color = color.copy(alpha = 0.7f))
                        }
                        innerTextField()
                    }
                }
            )

            if (showArrows) {
                IconButton(onClick = { if (currentResultIndex < (resultsLength - 1)) onNext() }) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.ic_arrow),
                        contentDescription = "Next",
                        tint = if (currentResultIndex < (resultsLength - 1)) color else color.copy(
                            alpha = 0.5f
                        )
                    )
                }
                IconButton(onClick = { if (currentResultIndex > 0) onPrev() }) {
                    Icon(
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(-180f),
                        painter = painterResource(R.drawable.ic_arrow),
                        contentDescription = "Prev",
                        tint = if (currentResultIndex > 0) color else color.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}