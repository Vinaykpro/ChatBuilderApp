package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar
import com.vinaykpro.chatbuilder.ui.components.ColorSelectionItem
import com.vinaykpro.chatbuilder.ui.components.EditIcon
import com.vinaykpro.chatbuilder.ui.components.ProgressItem
import com.vinaykpro.chatbuilder.ui.components.SelectModeWidget
import com.vinaykpro.chatbuilder.ui.components.SwitchItem

@Preview
@Composable
fun HeaderStyleScreen(
    navController: NavController = rememberNavController()
) {
    Column {
        BasicToolbar(name = "Header Style")
        Spacer(Modifier.height(12.dp))
        Column (modifier = Modifier.padding(horizontal = 10.dp).clip(shape = RoundedCornerShape(12.dp)).border(1.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(12.dp))) {
            ChatToolbar(preview = true)
            Spacer(modifier = Modifier.height(60.dp))
        }
        Column(Modifier.padding(start = 18.dp, end = 10.dp).verticalScroll(rememberScrollState())) {
            SelectModeWidget()

            Text(text = "Colors:", fontSize = 17.sp, fontWeight = FontWeight(500), color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            ColorSelectionItem(name = "Navbar Color", color = MaterialTheme.colorScheme.primary)
            ColorSelectionItem(name = "Name text color", color = Color.White)
            ColorSelectionItem(name = "Status text color",  color = Color.Gray)


            Text(text = "Widgets:", fontSize = 17.sp, fontWeight = FontWeight(500), color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            val backBtnState = remember { mutableStateOf(true) }
            val profilePicState = remember { mutableStateOf(true) }
            val showNameState = remember { mutableStateOf(true) }
            val showStatusState = remember { mutableStateOf(true) }

            SwitchItem(state = backBtnState)
            if(backBtnState.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp).background(MaterialTheme.colorScheme.secondaryContainer))
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ProgressItem(name = "Icon size")
                        ProgressItem(name = "Left gap")
                        EditIcon(name = "Back button icon")
                    }
                }
            }

            SwitchItem(state = profilePicState, name = "Show profile pic", context = "Show/hide the profile picture and customize.")
            if(profilePicState.value) {
                Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                    Spacer(modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp).background(MaterialTheme.colorScheme.secondaryContainer))
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ProgressItem(name = "Icon size")
                        ProgressItem(name = "Horizontal gap")
                        EditIcon(name = "Default profile icon")
                    }
                }
            }

            SwitchItem(state = showNameState, name = "Show name", context = "Show/hide the name as shown in the preview.")

            SwitchItem(state = showStatusState, name = "Show status/username", context = "Show/hide the status as shown in the preview.")

            SwitchItem(enabled = false, name = "Show three dots", context = "You can only customize the icon.")
            Box(modifier = Modifier.padding(bottom = 12.dp).height(IntrinsicSize.Min)) {
                Spacer(modifier = Modifier.fillMaxHeight().padding(start = 8.dp).width(1.dp).background(MaterialTheme.colorScheme.secondaryContainer))
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    ProgressItem(name = "Icon size")
                    ProgressItem(name = "Horizontal gap")
                    EditIcon(name = "Default profile icon")
                }
            }

            Text(text = "Action icons:", fontSize = 17.sp, fontWeight = FontWeight(500), color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            // TODO movable items layout :-)



        }

    }
}