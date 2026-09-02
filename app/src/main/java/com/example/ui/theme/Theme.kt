package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Standard Purple Schemes
private val StandardDarkColorScheme = darkColorScheme(
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

private val StandardLightColorScheme = lightColorScheme(
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

// AMOLED Pure Black Color Scheme
private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = Color(0xFF1E004A),
    primaryContainer = AmoledPrimaryContainer,
    onPrimaryContainer = AmoledOnPrimaryContainer,
    secondary = Color(0xFFA855F7),
    onSecondary = Color(0xFF000000),
    background = AmoledBackground,
    onBackground = AmoledOnSurface,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurfaceVariant,
    outline = AmoledOutline,
    outlineVariant = AmoledCardBorder
)

// Midnight Blue Schemes
private val MidnightDarkColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color(0xFF082F49),
    primaryContainer = MidnightPrimaryContainer,
    onPrimaryContainer = MidnightOnPrimaryContainer,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0B0F19),
    background = MidnightDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MidnightDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MidnightDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = MidnightDarkCardBorder
)

private val MidnightLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    background = MidnightLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = MidnightLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MidnightLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

// Emerald Green Schemes
private val EmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF064E3B),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF062016),
    background = EmeraldDarkBackground,
    onBackground = Color(0xFFF0FDF4),
    surface = EmeraldDarkSurface,
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = EmeraldDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA7F3D0),
    outline = Color(0xFF047857),
    outlineVariant = EmeraldDarkCardBorder
)

private val EmeraldLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    background = EmeraldLightBackground,
    onBackground = Color(0xFF064E3B),
    surface = EmeraldLightSurface,
    onSurface = Color(0xFF064E3B),
    surfaceVariant = EmeraldLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF047857),
    outline = Color(0xFFA7F3D0),
    outlineVariant = Color(0xFFD1FAE5)
)

// Sunset Amber Schemes
private val SunsetDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24),
    onPrimary = Color(0xFF78350F),
    primaryContainer = Color(0xFF92400E),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF1C1308),
    background = SunsetDarkBackground,
    onBackground = Color(0xFFFFFBEB),
    surface = SunsetDarkSurface,
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = SunsetDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFFDE68A),
    outline = Color(0xFFB45309),
    outlineVariant = SunsetDarkCardBorder
)

private val SunsetLightColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = SunsetPrimaryContainer,
    onPrimaryContainer = SunsetOnPrimaryContainer,
    secondary = Color(0xFFF59E0B),
    onSecondary = Color.White,
    background = SunsetLightBackground,
    onBackground = Color(0xFF451A03),
    surface = SunsetLightSurface,
    onSurface = Color(0xFF451A03),
    surfaceVariant = SunsetLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF92400E),
    outline = Color(0xFFFDE68A),
    outlineVariant = Color(0xFFFEF3C7)
)

// Lavender Schemes
private val LavenderDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF4C1D95),
    primaryContainer = Color(0xFF5B21B6),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFF8B5CF6),
    onSecondary = Color(0xFF171324),
    background = LavenderDarkBackground,
    onBackground = Color(0xFFFAF5FF),
    surface = LavenderDarkSurface,
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = LavenderDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFDDD6FE),
    outline = Color(0xFF7C3AED),
    outlineVariant = LavenderDarkCardBorder
)

private val LavenderLightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = LavenderPrimaryContainer,
    onPrimaryContainer = LavenderOnPrimaryContainer,
    secondary = Color(0xFF8B5CF6),
    onSecondary = Color.White,
    background = LavenderLightBackground,
    onBackground = Color(0xFF2E1065),
    surface = LavenderLightSurface,
    onSurface = Color(0xFF2E1065),
    surfaceVariant = LavenderLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF6D28D9),
    outline = Color(0xFFDDD6FE),
    outlineVariant = Color(0xFFEDE9FE)
)

// Ocean Cyan Schemes
private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF075985),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF0284C7),
    onSecondary = Color(0xFF081826),
    background = OceanDarkBackground,
    onBackground = Color(0xFFF0F9FF),
    surface = OceanDarkSurface,
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = OceanDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBAE6FD),
    outline = Color(0xFF0284C7),
    outlineVariant = OceanDarkCardBorder
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    background = OceanLightBackground,
    onBackground = Color(0xFF0C4A6E),
    surface = OceanLightSurface,
    onSurface = Color(0xFF0C4A6E),
    surfaceVariant = OceanLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF0369A1),
    outline = Color(0xFFBAE6FD),
    outlineVariant = Color(0xFFE0F2FE)
)

@Composable
fun TaskManagerTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themePreferences = ThemePreferences.getInstance(context)
    val settings by themePreferences.themeSettings.collectAsState()

    val isDark = when (settings.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme: ColorScheme = when (settings.palette) {
        ThemePalette.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) StandardDarkColorScheme else StandardLightColorScheme
            }
        }
        ThemePalette.AMOLED -> {
            if (isDark) AmoledColorScheme else StandardLightColorScheme
        }
        ThemePalette.MIDNIGHT -> {
            if (isDark) MidnightDarkColorScheme else MidnightLightColorScheme
        }
        ThemePalette.EMERALD -> {
            if (isDark) EmeraldDarkColorScheme else EmeraldLightColorScheme
        }
        ThemePalette.SUNSET -> {
            if (isDark) SunsetDarkColorScheme else SunsetLightColorScheme
        }
        ThemePalette.LAVENDER -> {
            if (isDark) LavenderDarkColorScheme else LavenderLightColorScheme
        }
        ThemePalette.OCEAN -> {
            if (isDark) OceanDarkColorScheme else OceanLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) = TaskManagerTheme(content)
