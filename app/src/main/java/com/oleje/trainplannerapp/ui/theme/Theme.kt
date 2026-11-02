package com.oleje.trainplannerapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = UzBlueAccent,
    secondary = UzAccentYellow,
    background = UzBackground,
    surface = UzCardBg,
    surfaceVariant = UzCardHeader,
    onPrimary = UzTextPrimary,
    onSecondary = UzBackground,
    onBackground = UzTextPrimary,
    onSurface = UzTextPrimary,
    onSurfaceVariant = UzTextSecondary
)

@Composable
fun TrainPlannerAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
