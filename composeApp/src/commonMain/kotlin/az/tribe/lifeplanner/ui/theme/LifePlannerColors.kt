package az.tribe.lifeplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Material Brutalist color scheme
data class BrutalistColorScheme(
    // Base Colors
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,

    // UI Elements
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val accent: Color,
    val error: Color,
    val errorContainer: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val divider: Color,
    val scrim: Color,

    // Component Colors
    val buttonPrimary: Color,
    val buttonSecondary: Color,
    val buttonDisabled: Color,
    val buttonText: Color,
    val buttonTextDisabled: Color,
    val inputBackground: Color,
    val inputText: Color,
    val cardBackground: Color,
    val chipBackground: Color,
    val chipText: Color,

    // Navigation
    val navBackground: Color,
    val navSelected: Color,
    val navUnselected: Color,
    val tabIndicator: Color,

    // States
    val focusedBorder: Color,
    val hoveredBorder: Color,
    val disabledBackground: Color,
    val disabledContent: Color,

    // Elevation and Shadows - brutalism often uses stark shadows
    val elevationOverlay: Color
)

// Static composition local for providing colors throughout the app
val LocalBrutalistColors = staticCompositionLocalOf {
    BrutalistColorScheme(
        // Initialize with Color.Unspecified for all properties
        background = Color.Unspecified,
        surface = Color.Unspecified,
        surfaceVariant = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textTertiary = Color.Unspecified,

        primary = Color.Unspecified,
        primaryContainer = Color.Unspecified,
        secondary = Color.Unspecified,
        secondaryContainer = Color.Unspecified,
        accent = Color.Unspecified,
        error = Color.Unspecified,
        errorContainer = Color.Unspecified,
        success = Color.Unspecified,
        successContainer = Color.Unspecified,
        warning = Color.Unspecified,
        warningContainer = Color.Unspecified,
        divider = Color.Unspecified,
        scrim = Color.Unspecified,

        buttonPrimary = Color.Unspecified,
        buttonSecondary = Color.Unspecified,
        buttonDisabled = Color.Unspecified,
        buttonText = Color.Unspecified,
        buttonTextDisabled = Color.Unspecified,
        inputBackground = Color.Unspecified,
        inputText = Color.Unspecified,
        cardBackground = Color.Unspecified,
        chipBackground = Color.Unspecified,
        chipText = Color.Unspecified,

        navBackground = Color.Unspecified,
        navSelected = Color.Unspecified,
        navUnselected = Color.Unspecified,
        tabIndicator = Color.Unspecified,

        focusedBorder = Color.Unspecified,
        hoveredBorder = Color.Unspecified,
        disabledBackground = Color.Unspecified,
        disabledContent = Color.Unspecified,

        elevationOverlay = Color.Unspecified
    )
}

@Composable
fun BrutalistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Map brutalist colors to Material3 color scheme
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = BrutalistThemeColors.Dark.primary,
            onPrimary = BrutalistThemeColors.Dark.buttonText,
            primaryContainer = BrutalistThemeColors.Dark.primaryContainer,
            onPrimaryContainer = BrutalistThemeColors.Dark.textPrimary,
            secondary = BrutalistThemeColors.Dark.secondary,
            onSecondary = BrutalistThemeColors.Dark.buttonText,
            secondaryContainer = BrutalistThemeColors.Dark.secondaryContainer,
            onSecondaryContainer = BrutalistThemeColors.Dark.textPrimary,
            tertiary = BrutalistThemeColors.Dark.accent,
            onTertiary = BrutalistThemeColors.Dark.buttonText,
            background = BrutalistThemeColors.Dark.background,
            onBackground = BrutalistThemeColors.Dark.textPrimary,
            surface = BrutalistThemeColors.Dark.surface,
            onSurface = BrutalistThemeColors.Dark.textPrimary,
            surfaceVariant = BrutalistThemeColors.Dark.surfaceVariant,
            onSurfaceVariant = BrutalistThemeColors.Dark.textSecondary,
            error = BrutalistThemeColors.Dark.error,
            onError = BrutalistThemeColors.Dark.buttonText,
            errorContainer = BrutalistThemeColors.Dark.errorContainer,
            onErrorContainer = BrutalistThemeColors.Dark.textPrimary,
            outline = BrutalistThemeColors.Dark.divider,
            outlineVariant = BrutalistThemeColors.Dark.disabledContent,
            scrim = BrutalistThemeColors.Dark.scrim
        )
        else -> lightColorScheme(
            primary = BrutalistThemeColors.Light.primary,
            onPrimary = BrutalistThemeColors.Light.buttonText,
            primaryContainer = BrutalistThemeColors.Light.primaryContainer,
            onPrimaryContainer = BrutalistThemeColors.Light.textPrimary,
            secondary = BrutalistThemeColors.Light.secondary,
            onSecondary = BrutalistThemeColors.Light.buttonText,
            secondaryContainer = BrutalistThemeColors.Light.secondaryContainer,
            onSecondaryContainer = BrutalistThemeColors.Light.textPrimary,
            tertiary = BrutalistThemeColors.Light.accent,
            onTertiary = BrutalistThemeColors.Light.buttonText,
            background = BrutalistThemeColors.Light.background,
            onBackground = BrutalistThemeColors.Light.textPrimary,
            surface = BrutalistThemeColors.Light.surface,
            onSurface = BrutalistThemeColors.Light.textPrimary,
            surfaceVariant = BrutalistThemeColors.Light.surfaceVariant,
            onSurfaceVariant = BrutalistThemeColors.Light.textSecondary,
            error = BrutalistThemeColors.Light.error,
            onError = BrutalistThemeColors.Light.buttonText,
            errorContainer = BrutalistThemeColors.Light.errorContainer,
            onErrorContainer = BrutalistThemeColors.Light.textPrimary,
            outline = BrutalistThemeColors.Light.divider,
            outlineVariant = BrutalistThemeColors.Light.disabledContent,
            scrim = BrutalistThemeColors.Light.scrim
        )
    }

    // Select the appropriate brutalist color scheme based on theme
    val brutalistColorScheme = when {
        darkTheme -> BrutalistThemeColors.Dark
        else -> BrutalistThemeColors.Light
    }

    // Provide the brutalist colors to the composition
    CompositionLocalProvider(
        LocalBrutalistColors provides brutalistColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

// Extension property to easily access brutalist colors
val MaterialTheme.brutalistColors: BrutalistColorScheme
    @Composable
    get() = LocalBrutalistColors.current

// Object containing predefined material brutalist color schemes
object BrutalistThemeColors {
    // Light material brutalist theme
    val Light = BrutalistColorScheme(
        // Base Colors
        background = Color(0xFFF0F0F0), // Raw concrete-like background
        surface = Color(0xFFE0E0E0),    // Slightly darker surface
        surfaceVariant = Color(0xFFD0D0D0), // Alternate surface for cards/containers
        textPrimary = Color(0xFF000000), // Pure black for maximum readability
        textSecondary = Color(0xFF333333), // Near black
        textTertiary = Color(0xFF666666), // Dark gray

        // UI Elements - bold, unfiltered colors
        primary = Color(0xFF000000),     // Black as primary - brutalist signature
        primaryContainer = Color(0xFFCCCCCC), // Gray containers
        secondary = Color(0xFF2B2B2B),   // Near black
        secondaryContainer = Color(0xFFBBBBBB), // Light gray
        accent = Color(0xFFFF0000),      // Pure red accent - stark, unfiltered
        error = Color(0xFFE50000),       // Bright red error
        errorContainer = Color(0xFFFFDDDD), // Light red container
        success = Color(0xFF007000),     // Forest green for success
        successContainer = Color(0xFFDDFFDD), // Light green container
        warning = Color(0xFFDD8800),     // Orange for warnings
        warningContainer = Color(0xFFFFEEDD), // Light orange container
        divider = Color(0xFF000000),     // Black dividers - brutalist style strong separation
        scrim = Color(0x99000000),       // Semi-transparent black for overlays

        // Component Colors - unadorned, functional
        buttonPrimary = Color(0xFF000000), // Black buttons
        buttonSecondary = Color(0xFFFFFFFF), // White secondary buttons with border
        buttonDisabled = Color(0xFFA0A0A0), // Gray disabled
        buttonText = Color(0xFFFFFFFF),    // White text on buttons
        buttonTextDisabled = Color(0xFF666666), // Dark gray text when disabled
        inputBackground = Color(0xFFFFFFFF), // White input fields
        inputText = Color(0xFF000000),     // Black text input
        cardBackground = Color(0xFFE8E8E8), // Slightly off-white cards
        chipBackground = Color(0xFFCCCCCC), // Gray chips
        chipText = Color(0xFF000000),      // Black chip text

        // Navigation - bold contrasts
        navBackground = Color(0xFFE0E0E0), // Light gray nav background
        navSelected = Color(0xFF000000),   // Black for selected
        navUnselected = Color(0xFF777777), // Gray for unselected
        tabIndicator = Color(0xFF000000),  // Black indicator

        // States - clear indicators
        focusedBorder = Color(0xFF000000), // Black borders when focused
        hoveredBorder = Color(0xFF333333), // Dark gray on hover
        disabledBackground = Color(0xFFBBBBBB), // Light gray disabled
        disabledContent = Color(0xFF888888), // Medium gray for disabled content

        // Elevation - brutalist minimal shadows
        elevationOverlay = Color(0x0A000000) // Very subtle shadow
    )

    // Dark material brutalist theme
    val Dark = BrutalistColorScheme(
        // Base Colors - industrial tones
        background = Color(0xFF121212), // Near black background
        surface = Color(0xFF1E1E1E),    // Dark surface
        surfaceVariant = Color(0xFF2A2A2A), // Alternate surface
        textPrimary = Color(0xFFFFFFFF), // Pure white for maximum readability
        textSecondary = Color(0xFFE0E0E0), // Near white
        textTertiary = Color(0xFFAAAAAA), // Light gray

        // UI Elements - bold, industrial
        primary = Color(0xFFFFFFFF),     // White primary - stark contrast
        primaryContainer = Color(0xFF2A2A2A), // Dark gray containers
        secondary = Color(0xFFE0E0E0),   // Light gray secondary
        secondaryContainer = Color(0xFF333333), // Dark gray container
        accent = Color(0xFFFF3B30),      // Bright red accent
        error = Color(0xFFFF5252),       // Bright red error
        errorContainer = Color(0xFF450000), // Dark red container
        success = Color(0xFF00CC00),     // Bright green for success
        successContainer = Color(0xFF004400), // Dark green container
        warning = Color(0xFFFFAA00),     // Bright orange for warnings
        warningContainer = Color(0xFF442200), // Dark orange container
        divider = Color(0xFF444444),     // Dark gray dividers
        scrim = Color(0xBF000000),       // Black scrim with higher opacity

        // Component Colors - stark, dramatic
        buttonPrimary = Color(0xFFFFFFFF), // White buttons
        buttonSecondary = Color(0xFF2A2A2A), // Dark gray secondary buttons
        buttonDisabled = Color(0xFF555555), // Medium gray disabled
        buttonText = Color(0xFF000000),    // Black text on white buttons
        buttonTextDisabled = Color(0xFF999999), // Gray text when disabled
        inputBackground = Color(0xFF2A2A2A), // Dark input fields
        inputText = Color(0xFFFFFFFF),     // White text input
        cardBackground = Color(0xFF252525), // Slightly lighter cards
        chipBackground = Color(0xFF333333), // Dark gray chips
        chipText = Color(0xFFFFFFFF),      // White chip text

        // Navigation - high contrast
        navBackground = Color(0xFF1A1A1A), // Very dark nav background
        navSelected = Color(0xFFFFFFFF),   // White for selected
        navUnselected = Color(0xFF888888), // Gray for unselected
        tabIndicator = Color(0xFFFFFFFF),  // White indicator

        // States - bold indicators
        focusedBorder = Color(0xFFFFFFFF), // White borders when focused
        hoveredBorder = Color(0xFFCCCCCC), // Light gray on hover
        disabledBackground = Color(0xFF333333), // Dark gray disabled
        disabledContent = Color(0xFF666666), // Medium gray for disabled content

        // Elevation - minimal but visible shadows
        elevationOverlay = Color(0x33000000) // Slightly more visible shadow in dark mode
    )
}