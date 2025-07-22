package com.vinaykpro.chatbuilder.ui.navigation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vinaykpro.chatbuilder.TestMessages
import com.vinaykpro.chatbuilder.data.models.ChatMediaViewModel
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.screens.chat.ChatScreen
import com.vinaykpro.chatbuilder.ui.screens.home.HomeScreen
import com.vinaykpro.chatbuilder.ui.screens.mediapreview.MediaPreviewScreen
import com.vinaykpro.chatbuilder.ui.screens.splash.SplashScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.BodyStyleScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.EditThemeScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.HeaderStyleScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.MessageBarStyleScreen
import com.vinaykpro.chatbuilder.ui.screens.theme.ThemeScreen

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Chat = "chat/{chatId}"
    const val Themes = "themes"
    const val EditTheme = "theme/{name}"
    const val HeaderStyle = "headerstyle"
    const val BodyStyle = "bodystyle"
    const val MessagebarStyle = "barstyle"
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    themeViewModel: ThemeViewModel,
    navController: NavHostController,
    context: Context,
    isDarkTheme: MutableState<Boolean>,
    prefs: SharedPreferences,
    sharedFileUri: Uri?
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.toPx().toInt() }

    val chatMediaViewModel: ChatMediaViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Routes.Home
        ) {
            composable(Routes.Splash) {
                SplashScreen(navController, isDarkTheme.value)
            }
            composable(route = Routes.Home) {
                HomeScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    themeViewModel = themeViewModel,
                    prefs = prefs,
                    sharedFileUri = sharedFileUri
                )
            }
            composable(
                route = Routes.Chat,
                enterTransition = {
                    if (targetState.destination.route?.startsWith("mediapreview") == true) {
                        null
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { screenWidthPx },
                            animationSpec = tween(400)
                        )
                    }
                },
                exitTransition = {
                    null
                },
                popEnterTransition = null,
                popExitTransition = {
                    if (initialState.destination.route?.startsWith("mediapreview") == true) {
                        null
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { screenWidthPx },
                            animationSpec = tween(400)
                        )
                    }
                }
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")?.toIntOrNull()
                chatId?.let {
                    ChatScreen(
                        chatId = it,
                        isDarkTheme.value,
                        navController,
                        this,
                        chatMediaViewModel
                    )
                }
            }

            composable(
                "mediapreview/{fileid}",
                enterTransition = null,
                exitTransition = null,
                popEnterTransition = null,
                popExitTransition = null
            ) { backStackEntry ->
                val fileid = backStackEntry.arguments?.getString("fileid")?.toIntOrNull()
                MediaPreviewScreen(fileid, navController, this, chatMediaViewModel)
            }


            composable(
                Routes.Themes,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                }) {
                ThemeScreen(themeViewModel = themeViewModel, navController = navController)
            }
            composable(
                Routes.EditTheme,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                }) { backStackEntry ->
                EditThemeScreen(
                    themename = (backStackEntry.arguments?.getString("name")
                        ?: "Default"),
                    navController = navController,
                    themeViewModel = themeViewModel,
                    isDark = isDarkTheme.value
                )
            }
            composable(
                Routes.HeaderStyle,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                }) {
                HeaderStyleScreen(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    isDarkTheme = isDarkTheme.value
                )
            }
            composable(
                Routes.BodyStyle,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                }) {
                BodyStyleScreen(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    isDarkTheme = isDarkTheme.value
                )
            }
            composable(
                Routes.MessagebarStyle,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { screenWidthPx },
                        animationSpec = tween(400)
                    )
                }) {
                MessageBarStyleScreen(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    isDarkTheme = isDarkTheme.value
                )
            }
            composable("temp") {
                TestMessages()
            }
        }
    }
}
