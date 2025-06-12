package com.vinaykpro.chatbuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vinaykpro.chatbuilder.ui.screens.chat.ChatScreen
import com.vinaykpro.chatbuilder.ui.screens.home.HomeScreen
import com.vinaykpro.chatbuilder.ui.screens.splash.SplashScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.ThemeScreen

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Chat = "chat"
    const val Themes = "themes"
}

@Composable
fun AppNavHost(navController: NavHostController, isDarkTheme: MutableState<Boolean>) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(Routes.Splash) {
            SplashScreen(navController, isDarkTheme)
        }
        composable(Routes.Home) {
            HomeScreen(navController = navController, isDarkTheme = isDarkTheme)
        }
        composable(Routes.Chat) {
            ChatScreen()
        }
        composable(Routes.Themes) {
            ThemeScreen()
        }
    }
}
