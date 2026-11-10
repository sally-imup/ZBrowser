package com.sally.zbrowser.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFA6185E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E5),
    onPrimaryContainer = Color(0xFF3E0022),
    secondary = Color(0xFF75565F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E3),
    onSecondaryContainer = Color(0xFF2B151C),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFF2DDE3),
    onSurfaceVariant = Color(0xFF514347),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB0CB),
    onPrimary = Color(0xFF5E1133),
    primaryContainer = Color(0xFF7D2A4A),
    onPrimaryContainer = Color(0xFFFFD9E5),
    secondary = Color(0xFFE3BDC7),
    onSecondary = Color(0xFF42292F),
    secondaryContainer = Color(0xFF5A3F46),
    onSecondaryContainer = Color(0xFFFFD9E3),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF524348),
    onSurfaceVariant = Color(0xFFD6C2C7),
)

@Composable
fun ZBrowserTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
