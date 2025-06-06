package com.vinaykpro.chatbuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vinaykpro.chatbuilder.ui.screens.chat.ChatScreen
import com.vinaykpro.chatbuilder.ui.screens.home.HomeScreen
import com.vinaykpro.chatbuilder.ui.screens.splash.SplashScreen

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Chat = "chat"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        composable(Routes.Splash) {
            SplashScreen(navController)
        }
        composable(Routes.Home) {
            HomeScreen(navController)
        }
        composable(Routes.Chat) {
            ChatScreen()
        }
    }
}
