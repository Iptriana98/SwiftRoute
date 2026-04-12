package com.swiftroute.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF44DDC1),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF00BFA5),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFFBFC6DB),
    onSecondary = Color(0xFF293041),
    background = Color(0xFF10141A),
    onBackground = Color(0xFFDFE2EB),
    surface = Color(0xFF10141A),
    onSurface = Color(0xFFDFE2EB),
    surfaceContainer = Color(0xFF1C2026),
    surfaceContainerHigh = Color(0xFF262A31),
    surfaceContainerHighest = Color(0xFF31353C),
    outline = Color(0xFF85948F),
    outlineVariant = Color(0xFF3C4A46)
)

// MD3 Light variant as fallback (but user prefers dark)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006B5C),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFBFDFF),
    onBackground = Color(0xFF191C1E)
)

@Composable
fun SwiftRouteNightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
