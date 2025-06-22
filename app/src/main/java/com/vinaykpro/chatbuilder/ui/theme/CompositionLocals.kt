package com.vinaykpro.chatbuilder.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.vinaykpro.chatbuilder.data.local.ThemeEntity

val LocalThemeEntity = compositionLocalOf<ThemeEntity> {
    error("ThemeEntity not provided")
}
