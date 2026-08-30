package com.videocompress.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun scaledTypography(multiplier: Float): Typography {
    fun style(size: Int, weight: FontWeight, line: Int, letter: Float = 0f) = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = weight,
        fontSize = (size * multiplier).sp,
        lineHeight = (line * multiplier).sp,
        letterSpacing = letter.sp,
    )
    return Typography(
        displaySmall = style(32, FontWeight.SemiBold, 40, -0.3f),
        headlineMedium = style(26, FontWeight.SemiBold, 34, -0.2f),
        headlineSmall = style(22, FontWeight.SemiBold, 28),
        titleLarge = style(20, FontWeight.SemiBold, 26),
        titleMedium = style(16, FontWeight.SemiBold, 22),
        titleSmall = style(14, FontWeight.SemiBold, 20),
        bodyLarge = style(16, FontWeight.Normal, 24),
        bodyMedium = style(14, FontWeight.Normal, 20),
        bodySmall = style(12, FontWeight.Normal, 16),
        labelLarge = style(14, FontWeight.Medium, 20),
        labelMedium = style(12, FontWeight.Medium, 16),
        labelSmall = style(11, FontWeight.Medium, 14),
    )
}
