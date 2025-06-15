package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.ui.components.ActionIcons
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.ProgressItem
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme

@Preview
@Composable
fun BodyStyleScreen(
    navController: NavController = rememberNavController()
) {
    Column {
        BasicToolbar(name = "Body Style")

        // TODO body-preview

        Column(Modifier.padding(start = 18.dp, end = 10.dp).verticalScroll(rememberScrollState())) {
            SelectModeWidget()

            Text(
                text = "Chat bubble style:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 14.dp)
            )

            var selectedBubbleStyle by remember { mutableStateOf(0) }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                BubbleItem(selected = selectedBubbleStyle == 0, onClick = { selectedBubbleStyle = 0})
                BubbleItem(painterResource(R.drawable.ic_topbubble), "Start bubble", selected = selectedBubbleStyle == 1, { selectedBubbleStyle = 1})
                BubbleItem(painterResource(R.drawable.ic_groupbubble), "Group bubble", selected = selectedBubbleStyle == 2, { selectedBubbleStyle = 2})
                BubbleItem(painterResource(R.drawable.ic_endbubble), "End bubble", selected = selectedBubbleStyle == 3, { selectedBubbleStyle = 3})
            }

            Text(
                text = "Colors:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )
            ColorSelectionItem(name = "Chat backgrund", color = MaterialTheme.colorScheme.primary)
            ColorSelectionItem(name = "Sender bubble color", color = Color.White)
            ColorSelectionItem(name = "Reciever bubble color", color = Color.Gray)
            ColorSelectionItem(name = "Date bubble color", color = Color.Gray)
            ColorSelectionItem(name = "Primary text color", color = Color.Gray)
            ColorSelectionItem(name = "Secondary text color", color = Color.Gray)


            Text(
                text = "Widgets:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )
            val backBtnState = remember { mutableStateOf(true) }
            val profilePicState = remember { mutableStateOf(true) }
            val showNameState = remember { mutableStateOf(true) }
            val showStatusState = remember { mutableStateOf(true) }

            SwitchItem(state = backBtnState)
            if (backBtnState.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ProgressItem(name = "Icon size")
                        ProgressItem(name = "Left gap")
                        EditIcon(name = "Back button icon")
                    }
                }
            }

            SwitchItem(
                state = profilePicState,
                name = "Show profile pic",
                context = "Show/hide the profile picture and customize."
            )
            if (profilePicState.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(
                        modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ProgressItem(name = "Icon size")
                        ProgressItem(name = "Horizontal gap")
                        EditIcon(name = "Default profile icon")
                    }
                }
            }

            SwitchItem(
                state = showNameState,
                name = "Show name",
                context = "Show/hide the name as shown in the preview."
            )

            SwitchItem(
                state = showStatusState,
                name = "Show status/username",
                context = "Show/hide the status as shown in the preview."
            )

            SwitchItem(
                enabled = false,
                name = "Show three dots",
                context = "You can only customize the icon."
            )
            Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                Spacer(
                    modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    ProgressItem(name = "Icon size")
                    ProgressItem(name = "Horizontal gap")
                    EditIcon(name = "Default profile icon")
                }
            }

            Text(
                text = "Action icons:",
                fontSize = 17.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
            )
            Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                Spacer(
                    modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    ActionIcons()
                }
            }

        }

    }
}


@Composable
fun BubbleItem(
    icon: Painter = painterResource(R.drawable.ic_nobubble),
    name: String = "No bubble",
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(end = 6.dp)
            .clip(shape = RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(2.dp, color = if(selected) LightColorScheme.primary else MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if(selected) LightColorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(bottom = 5.dp).width(70.dp)
        )
        Text(text = name, color = if(selected) LightColorScheme.primary else MaterialTheme.colorScheme.secondaryContainer, fontWeight = FontWeight(500), fontSize = 13.sp, lineHeight = 14.sp)
    }
}