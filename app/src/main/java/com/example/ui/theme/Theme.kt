package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrightColorfulColorScheme = lightColorScheme(
    primary = BrightPrimary,
    onPrimary = BrightOnPrimary,
    primaryContainer = BrightPrimaryContainer,
    onPrimaryContainer = BrightOnPrimaryContainer,
    secondary = BrightSecondary,
    onSecondary = BrightOnSecondary,
    secondaryContainer = BrightSecondaryContainer,
    onSecondaryContainer = BrightOnSecondaryContainer,
    tertiary = BrightTertiary,
    onTertiary = BrightOnTertiary,
    tertiaryContainer = BrightTertiaryContainer,
    background = BrightBackground,
    surface = BrightSurface,
    surfaceVariant = BrightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    outline = BrightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced bright colorful theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrightColorfulColorScheme,
        typography = Typography,
        content = content
    )
}
