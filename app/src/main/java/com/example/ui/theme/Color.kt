package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Bright, Beautiful & Vibrant Palette
val BrightPrimary = Color(0xFF2563EB)         // Royal Blue
val BrightPrimaryDark = Color(0xFF1D4ED8)
val BrightPrimaryContainer = Color(0xFFEFF6FF)
val BrightOnPrimary = Color(0xFFFFFFFF)
val BrightOnPrimaryContainer = Color(0xFF1E3A8A)

val BrightSecondary = Color(0xFFF97316)       // Vibrant Tangerine / Coral
val BrightSecondaryContainer = Color(0xFFFFF7ED)
val BrightOnSecondary = Color(0xFFFFFFFF)
val BrightOnSecondaryContainer = Color(0xFF9A3412)

val BrightTertiary = Color(0xFF06B6D4)        // Vibrant Cyan / Teal
val BrightTertiaryContainer = Color(0xFFECFEFF)
val BrightOnTertiary = Color(0xFFFFFFFF)

val BrightSuccess = Color(0xFF10B981)         // Emerald Mint
val BrightSuccessContainer = Color(0xFFECFDF5)
val BrightSuccessDark = Color(0xFF047857)

val BrightWarning = Color(0xFFF59E0B)         // Sunshine Amber
val BrightWarningContainer = Color(0xFFFEF3C7)

val BrightSurface = Color(0xFFFFFFFF)
val BrightBackground = Color(0xFFF6F8FC)      // Soft luminous pearl canvas
val BrightSurfaceVariant = Color(0xFFF1F5F9)
val BrightOutline = Color(0xFFE2E8F0)

// Radiant Gradients for UI Elements
val BlueGradient = Brush.horizontalGradient(
    listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
)

val CoralOrangeGradient = Brush.horizontalGradient(
    listOf(Color(0xFFF97316), Color(0xFFFB923C))
)

val MintEmeraldGradient = Brush.horizontalGradient(
    listOf(Color(0xFF059669), Color(0xFF10B981))
)

val PurplePinkGradient = Brush.horizontalGradient(
    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
)

val HeaderCardGradient = Brush.linearGradient(
    listOf(Color(0xFF1E40AF), Color(0xFF3B82F6), Color(0xFF60A5FA))
)
