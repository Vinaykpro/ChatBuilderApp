package com.vinaykpro.chatbuilder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.ui.theme.ChatBuilderTheme

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val isInHomeScreen = remember { mutableStateOf(true) };
    ChatBuilderTheme(isInHomeScreen = isInHomeScreen.value) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                isInHomeScreen.value = true
                Home(navController)
            }
            composable("chat") { isInHomeScreen.value = false; ChatScreen() }
        }
    }
}