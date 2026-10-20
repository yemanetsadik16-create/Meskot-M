package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme: ColorScheme = lightColorScheme(
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

@Composable
fun MeskotTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MeskotTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun meskotTextFieldColors(
    containerColor: Color = Paper2,
    borderColor: Color = LineBorder,
    textColor: Color = Ink,
    focusedBorderColor: Color = Gold,
    cursorColor: Color = Gold
) = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    disabledContainerColor = containerColor,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = borderColor,
    cursorColor = cursorColor,
    focusedTextColor = textColor,
    unfocusedTextColor = textColor
)
