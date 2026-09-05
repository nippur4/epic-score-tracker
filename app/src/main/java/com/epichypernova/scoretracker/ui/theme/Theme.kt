package com.epichypernova.scoretracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppColorScheme = darkColorScheme(
    primary = Palette.Cyan,
    onPrimary = Palette.OnAccent,
    background = Palette.AppBg,
    onBackground = Palette.TextPrimary,
    surface = Palette.AppBg,
    onSurface = Palette.TextPrimary,
    surfaceVariant = Palette.SheetSurface,
    onSurfaceVariant = Palette.TextSecondary,
    error = Palette.PlayerPink,
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
)

@Composable
fun EpicHypernovaTheme(content: @Composable () -> Unit) {
    // The design is a single dark theme; we ignore the system light/dark setting.
    @Suppress("UNUSED_EXPRESSION") isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
