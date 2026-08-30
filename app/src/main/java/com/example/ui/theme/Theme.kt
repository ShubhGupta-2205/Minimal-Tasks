package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = CyanNeon,
        onPrimary = Color.Black,
        primaryContainer = CyanNeonSubtle,
        onPrimaryContainer = CyanNeon,
        secondary = CyanNeon,
        onSecondary = Color.Black,
        background = DarkBlueDeep,
        onBackground = TextWhite,
        surface = FrostedBackground,
        onSurface = TextWhite,
        surfaceVariant = Color(0x3300FFFF),
        onSurfaceVariant = TextWhite,
        error = DangerRed,
        onError = Color.White
    )

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
