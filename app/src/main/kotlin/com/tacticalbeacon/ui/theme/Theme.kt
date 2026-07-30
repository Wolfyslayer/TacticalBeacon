package com.tacticalbeacon.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Tactical Color Palette ───────────────────────────────────────────────────

object TacticalColors {
    // Backgrounds
    val MatteBlack = Color(0xFF0A0A0A)
    val DarkSurface = Color(0xFF121212)
    val CardSurface = Color(0xFF1A1A1A)
    val ElevatedSurface = Color(0xFF222222)

    // Accents
    val OliveGreen = Color(0xFF6B7C3A)
    val OliveGreenLight = Color(0xFF8A9E4A)
    val OliveGreenDark = Color(0xFF4A5A28)
    val OliveGreenContainer = Color(0xFF2A3015)

    // Text
    val HighContrastWhite = Color(0xFFF5F5F5)
    val SecondaryText = Color(0xFFB0B0B0)
    val DisabledText = Color(0xFF606060)

    // Status colors
    val AlertRed = Color(0xFFD32F2F)
    val AlertAmber = Color(0xFFF57F17)
    val AlertGreen = Color(0xFF388E3C)
    val AlertCyan = Color(0xFF00838F)

    // Pin colors
    val PinOlive = Color(0xFF6B7C3A)
    val PinRed = Color(0xFFD32F2F)
    val PinAmber = Color(0xFFF57F17)
    val PinBlue = Color(0xFF1565C0)
    val PinWhite = Color(0xFFFAFAFA)
    val PinCyan = Color(0xFF00838F)
    val PinPurple = Color(0xFF6A1B9A)
    val PinOrange = Color(0xFFE65100)

    // Proximity level colors
    val ProxFar = Color(0xFF606060)
    val ProxNear = Color(0xFF6B7C3A)
    val ProxClose = Color(0xFFF57F17)
    val ProxVeryClose = Color(0xFFE65100)
    val ProxImmediate = Color(0xFFD32F2F)
    val ProxCritical = Color(0xFFFF1744)
    val ProxArrived = Color(0xFF00E676)

    // Red light mode
    val RedLightBackground = Color(0xFF0A0A0A)
    val RedLightSurface = Color(0xFF1A0A0A)
    val RedLightCard = Color(0xFF2A1515)
    val RedLightAccent = Color(0xFF8B0000)
    val RedLightText = Color(0xFF8B0000)
    val RedLightDim = Color(0xFF3A1515)
}

private val TacticalDarkColorScheme = darkColorScheme(
    primary = TacticalColors.OliveGreen,
    onPrimary = TacticalColors.HighContrastWhite,
    primaryContainer = TacticalColors.OliveGreenContainer,
    onPrimaryContainer = TacticalColors.OliveGreenLight,
    secondary = TacticalColors.OliveGreenLight,
    onSecondary = TacticalColors.MatteBlack,
    secondaryContainer = TacticalColors.OliveGreenDark,
    onSecondaryContainer = TacticalColors.HighContrastWhite,
    tertiary = TacticalColors.AlertCyan,
    onTertiary = TacticalColors.MatteBlack,
    tertiaryContainer = Color(0xFF003A3F),
    onTertiaryContainer = TacticalColors.AlertCyan,
    error = TacticalColors.AlertRed,
    onError = TacticalColors.HighContrastWhite,
    errorContainer = Color(0xFF4A0000),
    onErrorContainer = Color(0xFFFF8A80),
    background = TacticalColors.MatteBlack,
    onBackground = TacticalColors.HighContrastWhite,
    surface = TacticalColors.DarkSurface,
    onSurface = TacticalColors.HighContrastWhite,
    surfaceVariant = TacticalColors.CardSurface,
    onSurfaceVariant = TacticalColors.SecondaryText,
    outline = TacticalColors.OliveGreenDark,
    outlineVariant = Color(0xFF2A2A2A),
    scrim = Color(0x80000000),
    inverseSurface = TacticalColors.HighContrastWhite,
    inverseOnSurface = TacticalColors.MatteBlack,
    inversePrimary = TacticalColors.OliveGreenDark,
    surfaceTint = TacticalColors.OliveGreen,
    surfaceContainerHighest = TacticalColors.ElevatedSurface,
    surfaceContainerHigh = TacticalColors.CardSurface,
    surfaceContainer = TacticalColors.DarkSurface,
    surfaceContainerLow = TacticalColors.MatteBlack,
    surfaceContainerLowest = Color(0xFF050505)
)

@Composable
fun TacticalBeaconTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = TacticalTypography,
        content = content
    )
}
