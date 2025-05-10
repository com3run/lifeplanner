// Theme.kt
package az.tribe.lifeplanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun LifePlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = BrutalistThemeColors.Dark.accent,
            onPrimary = BrutalistThemeColors.Dark.buttonText,
            secondary = BrutalistThemeColors.Dark.accent,
            background = BrutalistThemeColors.Dark.background,
            surface = BrutalistThemeColors.Dark.surface,
            onSurface = BrutalistThemeColors.Dark.textPrimary,
            error = BrutalistThemeColors.Dark.error
        )
        else -> lightColorScheme(
            primary = BrutalistThemeColors.Light.accent,
            onPrimary = BrutalistThemeColors.Light.buttonText,
            secondary = BrutalistThemeColors.Light.accent,
            background = BrutalistThemeColors.Light.background,
            surface = BrutalistThemeColors.Light.surface,
            onSurface = BrutalistThemeColors.Light.textPrimary,
            error = BrutalistThemeColors.Light.error
        )
    }

    // Select the appropriate brutalist color scheme based on theme
    val brutalistColorScheme = when {
        darkTheme -> BrutalistThemeColors.Dark
        else -> BrutalistThemeColors.Light
    }
    CompositionLocalProvider(
        LocalBrutalistColors provides brutalistColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LifePlannerTypography(),
            shapes = LifePlannerShapes,
            content = content
        )
    }
}