package com.vitalos.app.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalVitalOsTypography = staticCompositionLocalOf { VitalOsTypography() }

private val vitalOsMaterialColorScheme: ColorScheme = darkColorScheme(
    primary = VitalOsPalette.primaryContainer,
    onPrimary = VitalOsPalette.onPrimaryContainer,
    primaryContainer = VitalOsPalette.primaryContainer,
    onPrimaryContainer = VitalOsPalette.onPrimaryContainer,
    secondary = VitalOsPalette.secondary,
    onSecondary = VitalOsPalette.onSecondary,
    secondaryContainer = VitalOsPalette.secondaryContainer,
    onSecondaryContainer = VitalOsPalette.onSecondaryContainer,
    tertiary = VitalOsPalette.diagnosticBlue,
    background = VitalOsPalette.voidBackground,
    onBackground = VitalOsPalette.onBackground,
    surface = VitalOsPalette.voidBackground,
    onSurface = VitalOsPalette.onSurface,
    surfaceVariant = VitalOsPalette.surfaceVariant,
    onSurfaceVariant = VitalOsPalette.onSurfaceVariant,
    outline = VitalOsPalette.outline,
    error = VitalOsPalette.criticalRed,
    onError = VitalOsPalette.onError,
    errorContainer = VitalOsPalette.errorContainer,
    onErrorContainer = VitalOsPalette.onErrorContainer,
)

object VitalOsTheme {
    val colors: VitalOsPalette get() = VitalOsPalette
    val typography: VitalOsTypography
        @Composable get() = LocalVitalOsTypography.current
    val spacing: VitalOsSpacing get() = VitalOsSpacing
    val shapes: VitalOsShapes get() = VitalOsShapes
}

@Composable
fun VitalOsAppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalVitalOsTypography provides VitalOsTypography()) {
        MaterialTheme(
            colorScheme = vitalOsMaterialColorScheme,
            content = content,
        )
    }
}
