package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PurpleFabContainer,
    onPrimary = PurpleOnPrimaryContainer,
    primaryContainer = PurplePrimary,
    onPrimaryContainer = PurplePrimaryContainer,
    secondary = AccentPurple,
    onSecondary = MinimalDarkBackground,
    background = MinimalDarkBackground,
    onBackground = MinimalDarkOnSurface,
    surface = MinimalDarkSurface,
    onSurface = MinimalDarkOnSurface,
    surfaceVariant = MinimalDarkSurfaceVariant,
    onSurfaceVariant = MinimalDarkOnSurfaceVariant,
    outline = MinimalDarkOutline,
    outlineVariant = MinimalDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = AccentPurple,
    onSecondary = PurpleOnPrimary,
    background = MinimalLightBackground,
    onBackground = MinimalLightOnSurface,
    surface = MinimalLightSurface,
    onSurface = MinimalLightOnSurface,
    surfaceVariant = MinimalLightSurfaceVariant,
    onSurfaceVariant = MinimalLightOnSurfaceVariant,
    outline = MinimalLightOutline,
    outlineVariant = MinimalLightCardBorder
)

/**
 * TaskManagerTheme with Material You Dynamic Theming enabled by default on Android 12+ (API 31+).
 * Colors dynamically adapt to the user's phone wallpaper and system color extraction.
 */
@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Dynamic Material You theming enabled
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) = TaskManagerTheme(darkTheme, dynamicColor, content)
