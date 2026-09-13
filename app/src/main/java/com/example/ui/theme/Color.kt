package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Meskot Brand Palette (Ethiopian Heritage & Earth Tones - Inspired by the iconic Meskot emblem)
val Ink = Color(0xFF1B2A22)
val InkDark = Color(0xFF121D17)
val Paper = Color(0xFFF7F5F0) // Warm linen backdrop complimenting the gold emblem
val Paper2 = Color(0xFFEBE6DC) // Warm tinted container
val CardBg = Color(0xFFFFFFFC) // Pristine warm card background
val Gold = Color(0xFFC48F37) // Luminous Meskot Gold
val GoldDeep = Color(0xFF946820) // Deep royal amber
val GoldLight = Color(0xFFF3CA68) // Radiant gold highlight
val GoldSurface = Color(0xFFFDF7EC) // Gentle warm gold surface tint
val GoldBorder = Color(0xFFE5CE9F) // Delicate warm gold border
val CrossRed = Color(0xFF8C2F39)
val LineBorder = Color(0xFFE3DCCF)
val MutedText = Color(0xFF5F6E64)
val ActiveGreen = Color(0xFF31A24C)
val GreenAccent = Color(0xFF31A24C)

// Meskot Signature Gradients (Iconic arched window logo colors)
val MeskotLogoBrush = Brush.linearGradient(listOf(GoldLight, Gold, GoldDeep))
val MeskotLogoHorizontalBrush = Brush.horizontalGradient(listOf(GoldLight, Gold, GoldDeep))
val MeskotAccentBrush = Brush.horizontalGradient(listOf(Gold, CrossRed, GoldDeep))

// Post Background Gradients
val PostGradient1 = Brush.linearGradient(listOf(Color(0xFF8C2F39), Color(0xFFB8863A)))
val PostGradient2 = Brush.linearGradient(listOf(Color(0xFF1B2A22), Color(0xFF2A4838)))
val PostGradient3 = Brush.linearGradient(listOf(Color(0xFF335577), Color(0xFF5A8FBE)))
val PostGradient4 = Brush.linearGradient(listOf(Color(0xFF4A3B5C), Color(0xFF8C5CA8)))
val PostGradient5 = Brush.linearGradient(listOf(Color(0xFFB8863A), Color(0xFFE8B94F)))

val PostGradientList = listOf(
    null, // Standard card
    PostGradient1,
    PostGradient2,
    PostGradient3,
    PostGradient4,
    PostGradient5
)

