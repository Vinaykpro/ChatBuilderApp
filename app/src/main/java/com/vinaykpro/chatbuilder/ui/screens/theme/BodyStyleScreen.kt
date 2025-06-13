package com.vinaykpro.chatbuilder.ui.screens.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.ui.components.BasicToolbar

@Preview
@Composable
fun BodyStyleScreen(
    navController: NavController = rememberNavController()

) {
    BasicToolbar(name = "Body Style")
}