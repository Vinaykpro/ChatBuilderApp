package com.vinaykpro.chatbuilder

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.navigation.AppNavHost
import com.vinaykpro.chatbuilder.ui.screens.splash.SplashScreen
import com.vinaykpro.chatbuilder.ui.theme.ChatBuilderTheme

class MainActivity : ComponentActivity() {
    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ThemeViewModel::class.java]

        enableEdgeToEdge(window)

        setContent {
            val theme = themeViewModel.themeEntity.collectAsState(initial = null).value
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = remember { mutableStateOf(false) }
            if(theme != null) {
                ChatBuilderTheme(theme = theme, darkTheme = isDarkTheme.value) {
                    val navController = rememberNavController()
                    AppNavHost(
                        themeViewModel = themeViewModel,
                        navController = navController,
                        isDarkTheme = isDarkTheme
                    )
                }
            } else {
                SplashScreen()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun enableEdgeToEdge(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }
}