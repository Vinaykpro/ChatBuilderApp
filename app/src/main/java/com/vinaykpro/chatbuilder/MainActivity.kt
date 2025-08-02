package com.vinaykpro.chatbuilder

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.vinaykpro.chatbuilder.data.models.ThemeViewModel
import com.vinaykpro.chatbuilder.ui.navigation.AppNavHost
import com.vinaykpro.chatbuilder.ui.screens.splash.SplashScreen
import com.vinaykpro.chatbuilder.ui.theme.ChatBuilderTheme

class MainActivity : ComponentActivity() {
    private lateinit var themeViewModel: ThemeViewModel

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ThemeViewModel::class.java]

        enableEdgeToEdge(window)

        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("my_prefs", MODE_PRIVATE) }
            val theme = themeViewModel.themeEntity.collectAsState(initial = null).value
            val isDarkTheme = remember { mutableStateOf(prefs.getBoolean("isDarkEnabled", false)) }
            themeViewModel.trySavedIdSwitch(prefs.getInt("themeId", 1))
            window.setBackgroundDrawable((if (isDarkTheme.value) Color.BLACK else Color.WHITE).toDrawable())

            val sharedFileUri = extractSharedFile(intent)
            //TestMessages()
            if (theme != null) {
                val navController = rememberNavController()
                ChatBuilderTheme(theme = theme, darkTheme = isDarkTheme.value) {
                    AppNavHost(
                        themeViewModel = themeViewModel,
                        navController = navController,
                        context = context,
                        isDarkTheme = isDarkTheme,
                        prefs = prefs,
                        sharedFileUri = sharedFileUri
                    )
                }
            } else {
                SplashScreen(isDarkTheme = isDarkTheme.value)
            }
        }
    }

    private fun extractSharedFile(intent: Intent?): Uri? {
        val action = intent?.action
        val data = intent?.data
        val stream = intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        Log.d("vkrpo", "Action: $action")
        Log.d("vkpro", "Data: $data")
        Log.d("vkpro", "Stream: $stream")

        return stream ?: data
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

    override fun onResume() {
        super.onResume()
        Log.d(
            "vkpro",
            "Intent action: ${intent?.action}, data: ${intent?.data}, extras: ${intent?.extras}"
        )
    }
}