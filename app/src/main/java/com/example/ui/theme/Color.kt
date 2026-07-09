package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Premium Minimalist Dark Palette (Clean, flat, neutral dark)
val DarkBackground = Color(0xFF0A0A0A)      // Deep pure black
val DarkSurface = Color(0xFF121212)         // Flat dark gray
val DarkSurfaceVariant = Color(0xFF1C1C1E)  // Light gray for borders/divs
val DarkPrimary = Color(0xFFFFFFFF)          // Soft white text
val DarkOnPrimary = Color(0xFF000000)        // Contrasting black
val DarkSecondary = Color(0xFF8E8E93)        // Soft gray
val DarkBorder = Color(0xFF2C2C2E)           // Very light dark border

// Premium Minimalist Light Palette (Porcelain off-white theme)
val LightBackground = Color(0xFFF7F7F8)     // Soft off-white
val LightSurface = Color(0xFFFFFFFF)        // Pure white card surface
val LightSurfaceVariant = Color(0xFFF1F1F3) // Light porcelain gray
val LightPrimary = Color(0xFF000000)         // Pitch black text/accent
val LightOnPrimary = Color(0xFFFFFFFF)       // White text
val LightSecondary = Color(0xFF6E6E73)       // Medium gray secondary text
val LightBorder = Color(0xFFE5E5EA)          // Very light gray border

// Interactive accents
val SilverAccent = Color(0xFFE5E5EA)
val CharcoalAccent = Color(0xFF1D1D1F)
val AlertRed = Color(0xFFE30000)

val SparkexGold: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

