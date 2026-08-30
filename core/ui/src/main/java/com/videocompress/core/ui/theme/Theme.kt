package com.videocompress.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.videocompress.core.common.ContentScale

private val LightColors = lightColorScheme(
    primary = Palette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF134E4A),
    secondary = Palette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    background = Palette.backgroundLight,
    surface = Palette.surfaceLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Palette.onSurfaceLight,
    onSurface = Palette.onSurfaceLight,
    onSurfaceVariant = Palette.onVariantLight,
    outline = Palette.outlineLight,
    outlineVariant = Color(0xFFEEF2F6),
    error = Color(0xFFB91C1C),
)

private val DarkColors = darkColorScheme(
    primary = Palette.primaryDark,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Palette.surfaceElevatedDark,
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Palette.secondaryDark,
    onSecondary = Color(0xFF083344),
    secondaryContainer = Palette.surfaceElevatedDark,
    onSecondaryContainer = Color(0xFFCFFAFE),
    background = Palette.backgroundDark,
    surface = Palette.surfaceDark,
    surfaceVariant = Palette.surfaceElevatedDark,
    onBackground = Palette.onSurfaceDark,
    onSurface = Palette.onSurfaceDark,
    onSurfaceVariant = Palette.onVariantDark,
    outline = Palette.outlineDark,
    outlineVariant = Color(0xFF1F2937),
    error = Color(0xFFF87171),
)

@Composable
fun VideoCompressorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    contentScale: ContentScale = ContentScale.NORMAL,
    content: @Composable () -> Unit,
) {
    val typography = remember(contentScale) { scaledTypography(contentScale.multiplier) }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = typography,
        content = content,
    )
}
