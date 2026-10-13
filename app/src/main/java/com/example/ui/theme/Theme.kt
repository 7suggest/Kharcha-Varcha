package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ExtendedColors(
    val canvas: Color,
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,
    val brandPrimary: Color,
    val brandAccent: Color,
    val positive: Color,
    val positiveContainer: Color,
    val negative: Color,
    val negativeContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderSubtle: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        canvas = LightCanvas,
        surfaceSecondary = LightSurfaceSecondary,
        surfaceTertiary = LightBorder,
        brandPrimary = LightBrandPrimary,
        brandAccent = LightBrandAccent,
        positive = LightPositive,
        positiveContainer = LightPositiveContainer,
        negative = LightNegative,
        negativeContainer = LightNegativeContainer,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textMuted = LightTextMuted,
        borderSubtle = LightBorder
    )
}

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = LightBrandPrimary,
    onPrimary = Color.White,
    secondary = LightBrandAccent,
    onSecondary = Color.White,
    background = LightCanvas,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightBorder,
    surfaceVariant = LightSurfaceSecondary,
    onSurfaceVariant = LightTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBrandPrimary,
    onPrimary = Color.Black,
    secondary = DarkBrandAccent,
    onSecondary = Color.White,
    background = DarkCanvas,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkBorder,
    surfaceVariant = DarkSurfaceSecondary,
    onSurfaceVariant = DarkTextSecondary
)

@Composable
fun KharchaVarchaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            canvas = DarkCanvas,
            surfaceSecondary = DarkSurfaceSecondary,
            surfaceTertiary = DarkBorder,
            brandPrimary = DarkBrandPrimary,
            brandAccent = DarkBrandAccent,
            positive = DarkPositive,
            positiveContainer = DarkPositiveContainer,
            negative = DarkNegative,
            negativeContainer = DarkNegativeContainer,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            borderSubtle = DarkBorder
        )
    } else {
        ExtendedColors(
            canvas = LightCanvas,
            surfaceSecondary = LightSurfaceSecondary,
            surfaceTertiary = LightBorder,
            brandPrimary = LightBrandPrimary,
            brandAccent = LightBrandAccent,
            positive = LightPositive,
            positiveContainer = LightPositiveContainer,
            negative = LightNegative,
            negativeContainer = LightNegativeContainer,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            borderSubtle = LightBorder
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
