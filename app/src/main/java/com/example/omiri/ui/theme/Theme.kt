package com.example.omiri.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AppColors.BrandOrange,
    onPrimary = Color.White,
    primaryContainer = AppColors.BrandOrangeSoft,
    onPrimaryContainer = AppColors.BrandInk,

    background = AppColors.Bg,
    onBackground = AppColors.BrandInk,

    surface = AppColors.Surface,
    onSurface = AppColors.BrandInk,
    surfaceVariant = AppColors.SurfaceAlt,
    onSurfaceVariant = AppColors.MutedText,

    outline = AppColors.Border
)

private val DarkColors = darkColorScheme(
    primary = AppColors.BrandOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2A1A12),
    onPrimaryContainer = Color(0xFFFFD6C7),

    background = Color(0xFF0B0F1A),
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF0F1524),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF111A2B),
    onSurfaceVariant = Color(0xFFB6C2D1),

    outline = Color(0xFF243044)
)

@Composable
fun OmiriTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
