package com.SkrinVex.syncwave.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudioColorScheme = darkColorScheme(
    primary = StudioAccent,
    onPrimary = Zinc100,
    primaryContainer = StudioAccentHover,
    onPrimaryContainer = Zinc100,
    secondary = StudioEmerald,
    onSecondary = Zinc100,
    background = StudioBg,
    onBackground = Zinc100,
    surface = StudioSurface,
    onSurface = Zinc100,
    surfaceVariant = StudioElevated,
    onSurfaceVariant = Zinc300,
    outline = StudioBorder,
    outlineVariant = StudioBorderSubtle,
    error = StudioRed,
    onError = Zinc100
)

@Composable
fun SyncWaveTheme(
    darkTheme: Boolean = true, // Obsidian Studio is strictly dark
    content: @Composable () -> Unit
) {
    val colorScheme = StudioColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StudioBg.toArgb()
            window.navigationBarColor = StudioBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
