// ==========================================
// IDENTITY: The Drip / Compose Theme
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/theme/Theme.kt
// COMPONENT: UI Aesthetics
// VIBE: Strictly dark mode. Light mode is for sociopaths. 🦇
// ==========================================

package com.example.pocket_orbit.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OrbitDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC), // Neon Cyan
    secondary = Color(0xFF8A2BE2), // Deep Purple
    tertiary = Color(0xFFFF3366), // Stop Loss Red
    background = Color(0xFF0D0D0D), // Void Black
    surface = Color(0xFF1A1A1A), // Dark Grey
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun OrbitTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = OrbitDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}