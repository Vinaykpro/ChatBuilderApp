package com.vinaykpro.chatbuilder.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vinaykpro.chatbuilder.ui.screens.chat.ChatScreen
import com.vinaykpro.chatbuilder.ui.screens.home.HomeScreen
import com.vinaykpro.chatbuilder.ui.screens.splash.SplashScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.BodyStyleScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.EditThemeScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.HeaderStyleScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.MessageBarStyleScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.ThemeScreen

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Chat = "chat"
    const val Themes = "themes"
    const val EditTheme = "theme/{name}"
    const val HeaderStyle = "headerstyle"
    const val BodyStyle = "bodystyle"
    const val MessagebarStyle = "barstyle"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(navController: NavHostController, isDarkTheme: MutableState<Boolean>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.toPx().toInt() }
    NavHost(
        navController = navController,
        startDestination = Routes.BodyStyle
    ) {
        composable(Routes.Splash) {
            SplashScreen(navController, isDarkTheme)
        }
        composable(route = Routes.Home,) {
            HomeScreen(navController = navController, isDarkTheme = isDarkTheme)
        }
        composable(route = Routes.Chat,
            enterTransition = {
                slideInHorizontally ( initialOffsetX = { screenWidthPx }, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { screenWidthPx }, animationSpec = tween(700))
            }) {
            ChatScreen()
        }
        composable(Routes.Themes,
            enterTransition = {
                slideInHorizontally ( initialOffsetX = { screenWidthPx }, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { screenWidthPx }, animationSpec = tween(700))
            }) {
            ThemeScreen(navController = navController)
        }
        composable(Routes.EditTheme,
            enterTransition = {
                slideInHorizontally ( initialOffsetX = { screenWidthPx }, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { screenWidthPx }, animationSpec = tween(700))
            }) { backStackEntry ->
            EditThemeScreen(themename = (backStackEntry.arguments?.getString("name")
                ?: "Default"), navController = navController)
        }
        composable(Routes.HeaderStyle,
            enterTransition = {
                slideInHorizontally ( initialOffsetX = { screenWidthPx }, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { screenWidthPx }, animationSpec = tween(700))
            }) {
            HeaderStyleScreen(navController = navController)
        }
        composable(Routes.BodyStyle,
            enterTransition = {
                slideInHorizontally ( initialOffsetX = { screenWidthPx }, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { screenWidthPx }, animationSpec = tween(700))
            }) {
            BodyStyleScreen(navController = navController)
        }
        composable(Routes.MessagebarStyle,
            enterTransition = {
                slideInHorizontally ( initialOffsetX = { screenWidthPx }, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { screenWidthPx }, animationSpec = tween(700))
            }) {
            MessageBarStyleScreen(navController = navController)
        }
    }
}
