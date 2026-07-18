package az.tribe.lifeplanner.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * D5, visual identity.
 *
 * D3 deliberately ratified the existing v2 token system ("no working theme code is rewritten
 * here"), which is why the v3 redesign shipped without changing a single pixel of color, type or
 * spacing. This file is the missing layer: a swappable identity that drives **both** the
 * [ModernColorScheme] and the Material3 `ColorScheme`, so changing one line restyles every screen,
 * including the 164 files that read `MaterialTheme.colorScheme` rather than `modernColors`.
 *
 * Direction comes from D1, not from taste:
 * - **P6** calm by default, restraint over vibrance.
 * - **P3 / Finch** warmth "without infantilizing an adult" (D1 section 3.2).
 * - **Oura** calm, premium, trustworthy data presentation (D1 section 3.3).
 *
 * [CLASSIC] is the v2 palette, kept byte-identical so the identity swap is fully reversible.
 * Switch with [ACTIVE_VISUAL_IDENTITY].
 */
enum class VisualIdentity {
    /** v2. Generic indigo/violet SaaS palette. Unchanged, the safe rollback target. */
    CLASSIC,

    /** Warm ink neutrals + brass. Premium and adult, the Oura read of D1. */
    WARM_INK,

    /** Sage green + warm taupe and terracotta. Natural calm, the Finch read of D1. */
    SAGE,
}

/**
 * The identity the app ships with. **This is the D5 switch**: change this one value to restyle
 * the entire app, then rebuild. Reverting to [VisualIdentity.CLASSIC] restores v2 exactly.
 */
val ACTIVE_VISUAL_IDENTITY = VisualIdentity.WARM_INK

/**
 * The brand gradient behind [az.tribe.lifeplanner.ui.components.GradientHero], which is the single
 * most prominent surface in the app (For You, Goals and You all lead with it).
 *
 * It is stated per identity rather than derived, because a hero gradient is a brand decision, not a
 * mechanical tint of the primary. CLASSIC returns the untouched v2 brush so the rollback is exact.
 */
@Composable
fun heroGradient(): Brush = when (ACTIVE_VISUAL_IDENTITY) {
    VisualIdentity.CLASSIC -> LifePlannerGradients.primary
    // Deep brass into amber. Warm and premium, the Oura read.
    VisualIdentity.WARM_INK -> Brush.linearGradient(listOf(Color(0xFF8A4A25), Color(0xFFC98A3F)))
    // Deep forest into soft sage. Natural and calm, the Finch read.
    VisualIdentity.SAGE -> Brush.linearGradient(listOf(Color(0xFF35604A), Color(0xFF7FA37E)))
}

/** Resolve the modern token set for an identity. */
fun visualScheme(identity: VisualIdentity, darkTheme: Boolean): ModernColorScheme =
    when (identity) {
        VisualIdentity.CLASSIC -> if (darkTheme) ModernThemeColors.Dark else ModernThemeColors.Light
        VisualIdentity.WARM_INK -> if (darkTheme) WarmInkDark else WarmInkLight
        VisualIdentity.SAGE -> if (darkTheme) SageDark else SageLight
    }

/**
 * Compact spec for an identity. Only the colors that actually differ between identities; the
 * full [ModernColorScheme] (containers, chips, gradients, disabled states) is derived in [scheme]
 * so a new identity is a short, reviewable block instead of 40 hand-maintained fields.
 */
private class Palette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val primary: Color,
    val primaryVariant: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val accent: Color,
    val accentVariant: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val divider: Color,
    val outline: Color,
)

private fun Palette.scheme(): ModernColorScheme = ModernColorScheme(
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textTertiary = textTertiary,
    textDisabled = textDisabled,

    primary = primary,
    primaryVariant = primaryVariant,
    primaryDark = primaryDark,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,

    secondary = secondary,
    secondaryVariant = secondaryVariant,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,

    accent = accent,
    accentVariant = accentVariant,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,

    success = success,
    successContainer = successContainer,
    onSuccessContainer = onSuccessContainer,

    error = error,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,

    warning = warning,
    warningContainer = warningContainer,
    onWarningContainer = onWarningContainer,

    divider = divider,
    outline = outline,
    outlineVariant = divider,
    scrim = Color(0x80000000),

    cardBackground = surface,
    chipBackground = surfaceVariant,
    chipText = textSecondary,

    disabledBackground = surfaceVariant,
    disabledContent = textDisabled,

    gradientPrimary = listOf(primary, primaryVariant),
    gradientSecondary = listOf(secondary, secondaryVariant),
    gradientAccent = listOf(accent, accentVariant),
    gradientSuccess = listOf(success, successContainer),
    gradientWarning = listOf(warning, warningContainer),
    gradientError = listOf(error, errorContainer),
)

// ── WARM INK ────────────────────────────────────────────────────────────────
// Warm paper and ink neutrals with a brass primary. Reads premium and grown-up
// rather than "productivity SaaS", and carries warmth through the neutrals
// themselves instead of a bright accent (D1 P6 restraint).

private val WarmInkLight = Palette(
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1EBE1),
    textPrimary = Color(0xFF211C16),
    textSecondary = Color(0xFF6A6055),
    textTertiary = Color(0xFF988D80),
    textDisabled = Color(0xFFC7BDB0),
    primary = Color(0xFFA65A2E),
    primaryVariant = Color(0xFF8A4A25),
    primaryDark = Color(0xFF6B3819),
    primaryContainer = Color(0xFFF7E7D8),
    onPrimaryContainer = Color(0xFF4A2410),
    secondary = Color(0xFF3F5A50),
    secondaryVariant = Color(0xFF32483F),
    secondaryContainer = Color(0xFFDFE9E4),
    onSecondaryContainer = Color(0xFF172A24),
    accent = Color(0xFFC98A3F),
    accentVariant = Color(0xFFA96F2E),
    tertiaryContainer = Color(0xFFFBEEDA),
    onTertiaryContainer = Color(0xFF4E3510),
    success = Color(0xFF4A7C52),
    successContainer = Color(0xFFE1EEE2),
    onSuccessContainer = Color(0xFF1B3A20),
    error = Color(0xFFA9453B),
    errorContainer = Color(0xFFF7E2DF),
    onErrorContainer = Color(0xFF4A160F),
    warning = Color(0xFFB5802C),
    warningContainer = Color(0xFFF9EDD6),
    onWarningContainer = Color(0xFF4A3208),
    divider = Color(0xFFEAE2D6),
    outline = Color(0xFFC7BDB0),
).scheme()

private val WarmInkDark = Palette(
    background = Color(0xFF16130F),
    surface = Color(0xFF1E1A15),
    surfaceVariant = Color(0xFF2A251E),
    textPrimary = Color(0xFFF2EBE1),
    textSecondary = Color(0xFFB5AA9B),
    textTertiary = Color(0xFF8A8073),
    textDisabled = Color(0xFF5E564C),
    primary = Color(0xFFD98B57),
    primaryVariant = Color(0xFFC07544),
    primaryDark = Color(0xFFA65A2E),
    primaryContainer = Color(0xFF3A2517),
    onPrimaryContainer = Color(0xFFF7E0CB),
    secondary = Color(0xFF7FA394),
    secondaryVariant = Color(0xFF648579),
    secondaryContainer = Color(0xFF24352E),
    onSecondaryContainer = Color(0xFFD6E7DE),
    accent = Color(0xFFE0A85F),
    accentVariant = Color(0xFFC08B45),
    tertiaryContainer = Color(0xFF3A2C15),
    onTertiaryContainer = Color(0xFFF7E4C4),
    success = Color(0xFF79B183),
    successContainer = Color(0xFF253A29),
    onSuccessContainer = Color(0xFFD4EAD7),
    error = Color(0xFFD9776B),
    errorContainer = Color(0xFF3E1E19),
    onErrorContainer = Color(0xFFF7DAD4),
    warning = Color(0xFFD8A44E),
    warningContainer = Color(0xFF3A2E14),
    onWarningContainer = Color(0xFFF7E6C4),
    divider = Color(0xFF2F2921),
    outline = Color(0xFF4A4239),
).scheme()

// ── SAGE ────────────────────────────────────────────────────────────────────
// Sage green primary, warm taupe secondary, terracotta accent. The Finch read:
// natural and calming, warmth from earth tones rather than from cuteness.

private val SageLight = Palette(
    background = Color(0xFFF6F8F4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EFE5),
    textPrimary = Color(0xFF1B241D),
    textSecondary = Color(0xFF5C6960),
    textTertiary = Color(0xFF8B978F),
    textDisabled = Color(0xFFBDC7C0),
    primary = Color(0xFF4A7A5C),
    primaryVariant = Color(0xFF3B6249),
    primaryDark = Color(0xFF2C4A37),
    primaryContainer = Color(0xFFDDEDE2),
    onPrimaryContainer = Color(0xFF14301F),
    secondary = Color(0xFF7A6A52),
    secondaryVariant = Color(0xFF61543F),
    secondaryContainer = Color(0xFFEDE7DC),
    onSecondaryContainer = Color(0xFF2E2617),
    accent = Color(0xFFC4784B),
    accentVariant = Color(0xFFA66139),
    tertiaryContainer = Color(0xFFF7E4D8),
    onTertiaryContainer = Color(0xFF4A2814),
    success = Color(0xFF3F8A5B),
    successContainer = Color(0xFFDDEFE3),
    onSuccessContainer = Color(0xFF14371F),
    error = Color(0xFFB04A42),
    errorContainer = Color(0xFFF7E1DF),
    onErrorContainer = Color(0xFF4A1712),
    warning = Color(0xFFB5822F),
    warningContainer = Color(0xFFF7EBD6),
    onWarningContainer = Color(0xFF4A330A),
    divider = Color(0xFFE3EADF),
    outline = Color(0xFFBDC7C0),
).scheme()

private val SageDark = Palette(
    background = Color(0xFF121710),
    surface = Color(0xFF1A2018),
    surfaceVariant = Color(0xFF262E23),
    textPrimary = Color(0xFFE9F0E7),
    textSecondary = Color(0xFFA9B5A9),
    textTertiary = Color(0xFF7E8A7E),
    textDisabled = Color(0xFF55604F),
    primary = Color(0xFF7FB292),
    primaryVariant = Color(0xFF659578),
    primaryDark = Color(0xFF4A7A5C),
    primaryContainer = Color(0xFF24382B),
    onPrimaryContainer = Color(0xFFD6EDDE),
    secondary = Color(0xFFBCAA8D),
    secondaryVariant = Color(0xFF9C8C71),
    secondaryContainer = Color(0xFF332E22),
    onSecondaryContainer = Color(0xFFEDE5D4),
    accent = Color(0xFFDD9468),
    accentVariant = Color(0xFFBD7A4F),
    tertiaryContainer = Color(0xFF3C2718),
    onTertiaryContainer = Color(0xFFF7DEC9),
    success = Color(0xFF7FB292),
    successContainer = Color(0xFF24382B),
    onSuccessContainer = Color(0xFFD6EDDE),
    error = Color(0xFFD97B70),
    errorContainer = Color(0xFF3E1F1B),
    onErrorContainer = Color(0xFFF7DCD7),
    warning = Color(0xFFD6A554),
    warningContainer = Color(0xFF3A2F16),
    onWarningContainer = Color(0xFFF7E8C6),
    divider = Color(0xFF2A3227),
    outline = Color(0xFF464F42),
).scheme()
