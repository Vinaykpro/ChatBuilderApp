package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatMessageBar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem

@Preview
@Composable
fun MessageBarStyleScreen(
    navController: NavController = rememberNavController()
) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
        BasicToolbar(name = "Message Bar Style")

        Column (modifier = Modifier.padding(top = 12.dp).padding(horizontal = 10.dp).clip(shape = RoundedCornerShape(12.dp))
            .border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))
            .background(Color(0xFFAA6D52))
            .padding(5.dp)) {
            Spacer(modifier = Modifier.height(60.dp))
            ChatMessageBar(preview = true)
        }

        Column(Modifier.padding(start = 18.dp, end = 10.dp).verticalScroll(rememberScrollState())) {
            SelectModeWidget()


            Text(text = "Colors:",fontSize = 17.sp,fontWeight = FontWeight(500),color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            ColorSelectionItem(name = "Widget backgrund", color = MaterialTheme.colorScheme.primary)
            ColorSelectionItem(name = "Message bar background", color = Color.White)
            ColorSelectionItem(name = "Outer action button color", color = Color.Gray)
            ColorSelectionItem(name = "Left inner action button color", color = Color.Gray)
            ColorSelectionItem(name = "Right inner action button color", color = Color.Gray)
            ColorSelectionItem(name = "Input text color", color = Color.Gray)
            ColorSelectionItem(name = "Hint text color", color = Color.Gray)


            Text(text = "Widgets:",fontSize = 17.sp,fontWeight = FontWeight(500),color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            val showOuterBtn = remember { mutableStateOf(true) }
            val showLeftInnerBtn = remember { mutableStateOf(true) }
            val showRightInnerBtn = remember { mutableStateOf(false) }

            SwitchItem(state = showOuterBtn, name = "Show outer action button",  context = "Show/hide time inside message bubble")
            if (showOuterBtn.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Seen ticks icon", icon = painterResource(R.drawable.ic_videocall))
                    }
                }
            }
            SwitchItem(state = showLeftInnerBtn, name = "Show left inner action button",  context = "Will show time in 12hr throughout the messages")
            if (showLeftInnerBtn.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Seen ticks icon", icon = painterResource(R.drawable.ic_call))
                    }
                }
            }
            SwitchItem(state = showRightInnerBtn, name = "Show right inner action button",  context = "Show/hide ticks inside message bubble")
            if (showRightInnerBtn.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        EditIcon(name = "Seen ticks icon", icon = painterResource(R.drawable.doubleticks))
                    }
                }
            }
        }

    }
}