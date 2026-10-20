package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF1D241A)
val InkDark = Color(0xFF141911)
val Paper = Color(0xFFFBF8EF)
val Paper2 = Color(0xFFF2ECE1)
val CardBg = Color(0xFFFFFFFF)
val Gold = Color(0xFFA87B25)
val GoldAccent = Color(0xFFA87B25)
val GoldDeep = Color(0xFF8A5F18)
val GoldLight = Color(0xFFEFD06C)
val GoldSurface = Color(0xFFFBF4DF)
val GoldBorder = Color(0xFFE5D5AA)
val CrossRed = Color(0xFF8A1C2B)
val LineBorder = Color(0xFFEADFC2)
val MutedText = Color(0xFF7A7360)
val ActiveGreen = Color(0xFF2E7D32)
val GreenAccent = Color(0xFF2E7D32)

val LuxuryGoldAccent = Color(0xFFD4AF37)
val LuxuryGoldShimmer = Color(0xFFFFF3CC)
val LuxuryGoldBorder = Color(0x66D4AF37)
val LuxuryDarkGlass = Color(0xEE1A1813)
val LuxuryLightGlass = Color(0xF2FBF8EF)
val LuxuryGlassGradient = Brush.verticalGradient(
    listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.85f))
)

val MeskotLogoBrush = Brush.linearGradient(
    listOf(GoldLight, Gold, GoldDeep)
)
val MeskotLogoHorizontalBrush = Brush.horizontalGradient(
    listOf(GoldLight, Gold, GoldDeep)
)
val MeskotAccentBrush = Brush.horizontalGradient(
    listOf(Gold, CrossRed, GoldDeep)
)
val LuxuryGoldGradient = Brush.horizontalGradient(
    listOf(Color(0xFFE5C158), Color(0xFFD4AF37), Color(0xFFB8860B))
)
val LuxuryTabActiveBrush = Brush.horizontalGradient(
    listOf(Color(0xFFFFF3CC), Color(0xFFFEE69C), Color(0xFFFFF3CC))
)

val PostGradient1 = Brush.linearGradient(listOf(Color(0xFF8A1C2B), Color(0xFFB8860B)))
val PostGradient2 = Brush.linearGradient(listOf(Color(0xFF1D241A), Color(0xFF2D3C28)))
val PostGradient3 = Brush.linearGradient(listOf(Color(0xFF355E3B), Color(0xFF5C8A4E)))
val PostGradient4 = Brush.linearGradient(listOf(Color(0xFF4C2A5C), Color(0xFF8A1C2B)))
val PostGradient5 = Brush.linearGradient(listOf(Color(0xFFB8860B), Color(0xFFE8982F)))

val PostGradientList = listOf(null, PostGradient1, PostGradient2, PostGradient3, PostGradient4, PostGradient5)
