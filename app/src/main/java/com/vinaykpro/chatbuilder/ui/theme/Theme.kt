package com.vinaykpro.chatbuilder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.vinaykpro.chatbuilder.data.local.ThemeEntity

var DarkColorScheme = darkColorScheme(
    primary = headerDark,
    secondary = PurpleGrey80,
    tertiary = Pink80,

    background = Black,
    primaryContainer = Black,
    secondaryContainer = Color.Gray,

    onPrimaryContainer = White,
    onSecondaryContainer = Color.LightGray,

    onTertiaryContainer = White,

    onSurface = Color(0xFF252525)
)

var LightColorScheme = lightColorScheme(
    primary = headerLight,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = White,
    primaryContainer = White,
    secondaryContainer = Color.LightGray,

    onPrimaryContainer = Black,
    onSecondaryContainer = Color.Gray,

    onTertiaryContainer = headerLight,

    onSurface = Color.White

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
    theme: ThemeEntity,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeEntity provides theme,
    ) {
        LightColorScheme = remember(theme.appcolor) {
            LightColorScheme.copy(primary = Color(theme.appcolor.toColorInt()))
        }
        DarkColorScheme = remember(theme.appcolordark) {
            DarkColorScheme.copy(primary = Color(theme.appcolordark.toColorInt()))
        }
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}