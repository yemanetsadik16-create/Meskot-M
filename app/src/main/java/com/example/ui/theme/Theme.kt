package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Paper2,
    onPrimaryContainer = Ink,
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = GoldLight.copy(alpha = 0.2f),
    onSecondaryContainer = GoldDeep,
    tertiary = CrossRed,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = CardBg,
    onSurface = Ink,
    surfaceVariant = Paper2,
    onSurfaceVariant = MutedText,
    outline = LineBorder
)

// Standard high-contrast text field colors for Meskot UI inputs
@Composable
fun meskotTextFieldColors(
    containerColor: Color = Color.White,
    textColor: Color = Ink,
    placeholderColor: Color = MutedText,
    borderColor: Color = LineBorder,
    focusedBorderColor: Color = Gold
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    cursorColor = GoldDeep,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = borderColor,
    focusedLabelColor = GoldDeep,
    unfocusedLabelColor = placeholderColor,
    focusedPlaceholderColor = placeholderColor,
    unfocusedPlaceholderColor = placeholderColor
)

@Composable
fun MeskotTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Meskot features a cohesive Ethiopian Royal Parchment aesthetic with high-contrast Ink typography
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
