package az.tribe.lifeplanner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Whether the active theme is dark. Components that pick their own surface treatment (glass
 * cards, the nav pill) must read this instead of hardcoding, or the Light / Dark / System
 * control in [ThemeController] cannot actually reach them.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

// Static composition local for providing modern colors throughout the app
val LocalModernColors = staticCompositionLocalOf {
    ModernColorScheme(
        // Initialize with default color
        background = Color.Unspecified,
        surface = Color.Unspecified,
        surfaceVariant = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textTertiary = Color.Unspecified,
        textDisabled = Color.Unspecified,

        primary = Color.Unspecified,
        primaryVariant = Color.Unspecified,
        primaryDark = Color.Unspecified,
        primaryContainer = Color.Unspecified,
        onPrimaryContainer = Color.Unspecified,

        secondary = Color.Unspecified,
        secondaryVariant = Color.Unspecified,
        secondaryContainer = Color.Unspecified,
        onSecondaryContainer = Color.Unspecified,

        accent = Color.Unspecified,
        accentVariant = Color.Unspecified,
        tertiaryContainer = Color.Unspecified,
        onTertiaryContainer = Color.Unspecified,

        success = Color.Unspecified,
        successContainer = Color.Unspecified,
        onSuccessContainer = Color.Unspecified,

        error = Color.Unspecified,
        errorContainer = Color.Unspecified,
        onErrorContainer = Color.Unspecified,

        warning = Color.Unspecified,
        warningContainer = Color.Unspecified,
        onWarningContainer = Color.Unspecified,

        divider = Color.Unspecified,
        outline = Color.Unspecified,
        outlineVariant = Color.Unspecified,
        scrim = Color.Unspecified,

        cardBackground = Color.Unspecified,
        chipBackground = Color.Unspecified,
        chipText = Color.Unspecified,

        disabledBackground = Color.Unspecified,
        disabledContent = Color.Unspecified,

        gradientPrimary = listOf(Color.Unspecified, Color.Unspecified),
        gradientSecondary = listOf(Color.Unspecified, Color.Unspecified),
        gradientAccent = listOf(Color.Unspecified, Color.Unspecified),
        gradientSuccess = listOf(Color.Unspecified, Color.Unspecified),
        gradientWarning = listOf(Color.Unspecified, Color.Unspecified),
        gradientError = listOf(Color.Unspecified, Color.Unspecified)
    )
}

@Composable
fun LifePlannerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // D5: the active visual identity drives both token sets. Swap ACTIVE_VISUAL_IDENTITY to
    // restyle the whole app; VisualIdentity.CLASSIC restores the v2 look exactly.
    val modernColorScheme = visualScheme(ACTIVE_VISUAL_IDENTITY, darkTheme)
    val colorScheme = createColorScheme(modernColorScheme, darkTheme)

    // Provide the modern colors to the composition
    CompositionLocalProvider(
        LocalModernColors provides modernColorScheme,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LifePlannerTypography(),
            shapes = ModernShapes,
            content = content
        )
    }
}

// Extension property to easily access modern colors
val MaterialTheme.modernColors: ModernColorScheme
    @Composable
    get() = LocalModernColors.current

// Create Material3 ColorScheme from the active identity's modern token set.
// Deriving it from ModernColorScheme (rather than hardcoding ModernColors) is what makes a
// D5 identity swap reach the 164 files that read MaterialTheme.colorScheme instead of modernColors.
private fun createColorScheme(c: ModernColorScheme, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = c.primary,
            onPrimary = c.textPrimary,
            primaryContainer = c.primaryContainer,
            onPrimaryContainer = c.onPrimaryContainer,

            secondary = c.secondary,
            onSecondary = c.textPrimary,
            secondaryContainer = c.secondaryContainer,
            onSecondaryContainer = c.onSecondaryContainer,

            tertiary = c.accent,
            onTertiary = c.textPrimary,
            tertiaryContainer = c.tertiaryContainer,
            onTertiaryContainer = c.onTertiaryContainer,

            error = c.error,
            onError = c.textPrimary,
            errorContainer = c.errorContainer,
            onErrorContainer = c.onErrorContainer,

            background = c.background,
            onBackground = c.textPrimary,

            surface = c.surface,
            onSurface = c.textPrimary,
            surfaceVariant = c.surfaceVariant,
            onSurfaceVariant = c.textSecondary,

            outline = c.outline,
            outlineVariant = c.outlineVariant,

            scrim = c.scrim
        )
    } else {
        lightColorScheme(
            primary = c.primary,
            onPrimary = Color.White,
            primaryContainer = c.primaryContainer,
            onPrimaryContainer = c.onPrimaryContainer,

            secondary = c.secondary,
            onSecondary = Color.White,
            secondaryContainer = c.secondaryContainer,
            onSecondaryContainer = c.onSecondaryContainer,

            tertiary = c.accent,
            onTertiary = Color.White,
            tertiaryContainer = c.tertiaryContainer,
            onTertiaryContainer = c.onTertiaryContainer,

            error = c.error,
            onError = Color.White,
            errorContainer = c.errorContainer,
            onErrorContainer = c.onErrorContainer,

            background = c.background,
            onBackground = c.textPrimary,

            surface = c.surface,
            onSurface = c.textPrimary,
            surfaceVariant = c.surfaceVariant,
            onSurfaceVariant = c.textSecondary,

            outline = c.outline,
            outlineVariant = c.outlineVariant,

            scrim = c.scrim
        )
    }
}
