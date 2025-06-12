package com.vinaykpro.chatbuilder

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.ui.navigation.AppNavHost
import com.vinaykpro.chatbuilder.ui.theme.ChatBuilderTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(window)
        setContent {
            val theme = isSystemInDarkTheme();
            val isDarkTheme = remember { mutableStateOf(false) };
            ChatBuilderTheme(darkTheme = isDarkTheme.value) {
                val navController = rememberNavController()
                AppNavHost(navController = navController, isDarkTheme = isDarkTheme)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun enableEdgeToEdge(window: Window) {
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