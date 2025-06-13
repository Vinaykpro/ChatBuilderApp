package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar
import com.vinaykpro.chatbuilder.ui.components.ChatToolbar

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
    }
}