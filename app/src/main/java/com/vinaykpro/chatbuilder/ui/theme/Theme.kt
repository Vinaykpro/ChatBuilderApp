package com.vinaykpro.chatbuilder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

public val DarkColorScheme = darkColorScheme(
    primary = headerDark,
    secondary = PurpleGrey80,
    tertiary = Pink80,

    background = Black,
    primaryContainer = Black,
    secondaryContainer = Color.Gray,

    onPrimaryContainer = White,
    onSecondaryContainer = Color.LightGray,

    onTertiaryContainer = White
)

public val LightColorScheme = lightColorScheme(
    primary = headerLight,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = White,
    primaryContainer = White,
    secondaryContainer = Color.LightGray,

    onPrimaryContainer = Black,
    onSecondaryContainer = Color.Gray,

    onTertiaryContainer = headerLight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ChatBuilderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isInHomeScreen: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if(darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = if(isInHomeScreen) Color(0xFF3DBFDC).toArgb() else Color(0xFFFFFFFF).toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isInHomeScreen
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}