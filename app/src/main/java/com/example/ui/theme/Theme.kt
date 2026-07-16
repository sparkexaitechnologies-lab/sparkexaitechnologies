package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BW_White,
    onPrimary = BW_Black,
    secondary = BW_LightGray,
    background = BW_Black,
    onBackground = BW_White,
    surface = BW_DarkGray,
    onSurface = BW_White,
    surfaceVariant = BW_SurfaceGray,
    onSurfaceVariant = BW_MediumGray,
    outline = BW_Gray,
    tertiary = BW_White,
    error = BW_Error
)

private val LightColorScheme = lightColorScheme(
    primary = BW_Black,
    onPrimary = BW_White,
    background = BW_OffWhite,
    onBackground = BW_Black,
    surface = BW_White,
    onSurface = BW_Black,
    surfaceVariant = BW_Silver,
    onSurfaceVariant = BW_MediumGray,
    outline = BW_LightGray,
    error = BW_Error
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
