package az.tribe.lifeplanner.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import az.tribe.lifeplanner.domain.enum.GoalCategory

/**
 * Life Planner Gradient System
 *
 * Beautiful gradient definitions for a modern, premium feel.
 */
object LifePlannerGradients {

    // ==================== PRIMARY GRADIENTS ====================

    /**
     * Primary brand gradient - Blue to Purple
     * Use for: Hero sections, primary CTAs, main headers
     */
    val primary: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF667EEA),  // Soft blue
                Color(0xFF764BA2)   // Rich purple
            )
        )

    /**
     * Primary gradient with angle (diagonal)
     */
    val primaryDiagonal: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF667EEA),
                Color(0xFF764BA2)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

    // ==================== SEMANTIC GRADIENTS ====================

    /**
     * Success gradient - Teal to Green
     * Use for: Completion states, achievements, positive feedback
     */
    val success: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF11998E),  // Teal
                Color(0xFF38EF7D)   // Bright green
            )
        )

    /**
     * Warm gradient - Pink to Coral
     * Use for: Streaks, motivation, engagement features
     */
    val warm: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF093FB),  // Soft pink
                Color(0xFFF5576C)   // Coral red
            )
        )

    /**
     * Sunset gradient - Orange to Pink
     * Use for: Highlights, special features
     */
    val sunset: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF9A9E),  // Peach
                Color(0xFFFECFEF)   // Light pink
            )
        )

    /**
     * Ocean gradient - Deep blue to cyan
     * Use for: Calm sections, analytics
     */
    val ocean: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF2193B0),  // Ocean blue
                Color(0xFF6DD5ED)   // Light cyan
            )
        )

    /**
     * Night gradient - Dark purple to indigo
     * Use for: Dark mode accents, premium features
     */
    val night: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F2027),  // Dark
                Color(0xFF203A43),  // Mid dark
                Color(0xFF2C5364)   // Deep blue
            )
        )

    // ==================== CATEGORY GRADIENTS ====================

    /**
     * Career gradient - Professional blue
     */
    val career: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF2196F3),  // Blue
                Color(0xFF21CBF3)   // Cyan
            )
        )

    /**
     * Money gradient - Money green
     */
    val money: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF11998E),
                Color(0xFF38EF7D)
            )
        )

    /**
     * Body gradient - Energy orange
     */
    val body: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF8008),
                Color(0xFFFFC837)
            )
        )

    /**
     * People gradient - Vibrant purple
     */
    val people: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF9C27B0),
                Color(0xFFE040FB)
            )
        )

    /**
     * Wellbeing gradient - Calm teal
     */
    val wellbeing: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF009688),
                Color(0xFF4DB6AC)
            )
        )

    /**
     * Purpose gradient - Deep rose
     */
    val purpose: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE91E63),
                Color(0xFFFF6090)
            )
        )

    /**
     * Family gradient - Warm amber/orange
     */
    val family: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF57C00),
                Color(0xFFFFB74D)
            )
        )

    // ==================== UTILITY FUNCTIONS ====================

    /**
     * Get gradient brush for a goal category
     */
    @Composable
    fun forCategory(category: GoalCategory): Brush {
        return when (category) {
            GoalCategory.CAREER -> career
            GoalCategory.MONEY -> money
            GoalCategory.BODY -> body
            GoalCategory.PEOPLE -> people
            GoalCategory.WELLBEING -> wellbeing
            GoalCategory.PURPOSE -> purpose
            GoalCategory.FAMILY -> family
        }
    }

    /**
     * Get gradient colors for a goal category (for custom use)
     */
    fun colorsForCategory(category: GoalCategory): List<Color> {
        return when (category) {
            GoalCategory.CAREER -> listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
            GoalCategory.MONEY -> listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            GoalCategory.BODY -> listOf(Color(0xFFFF8008), Color(0xFFFFC837))
            GoalCategory.PEOPLE -> listOf(Color(0xFF9C27B0), Color(0xFFE040FB))
            GoalCategory.WELLBEING -> listOf(Color(0xFF009688), Color(0xFF4DB6AC))
            GoalCategory.PURPOSE -> listOf(Color(0xFFE91E63), Color(0xFFFF6090))
            GoalCategory.FAMILY -> listOf(Color(0xFFF57C00), Color(0xFFFFB74D))
        }
    }

    // ==================== GLASS EFFECT GRADIENTS ====================

    /**
     * Subtle glass overlay gradient, light mode only
     * Use for: Glass card backgrounds
     */
    val glassOverlay: Brush
        @Composable get() = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.05f)
            )
        )

    /**
     * Dark-mode glass overlay, a gentle top-to-bottom dark gradient that
     * lifts the card top just enough to read without any white wash.
     * Uses the app's surfaceVariant dark tone (#252E42) fading to near-transparent.
     */
    val glassOverlayDark: Brush
        @Composable get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF252E42).copy(alpha = 0.55f),  // surfaceVariant dark, top highlight
                Color(0xFF1B2437).copy(alpha = 0.08f)   // surface dark, barely there at bottom
            )
        )
    /**
     * Subtle glass overlay gradient
     * Use for: Glass card backgrounds
     */
    val glassOverlayHigh: Brush
        @Composable get() = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.75f)
            )
        )

    /**
     * Border gradient for glass cards
     */
    val glassBorder: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.1f)
            )
        )

    /**
     * Dark blue glass, for nav bar and floating surfaces in dark mode.
     * Deep navy with a subtle blue tint, ~93% opaque so the background
     * colour bleeds through just enough to feel layered.
     */
    val glassNavDark: Brush
        @Composable get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1C2D50).copy(alpha = 0.93f),  // deep navy top
                Color(0xFF141F3A).copy(alpha = 0.97f)   // darker navy bottom
            )
        )

    /**
     * Soft blue border for dark-mode floating surfaces.
     */
    val glassBorderDark: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF667EEA).copy(alpha = 0.45f),  // brand blue
                Color(0xFF4A6CF7).copy(alpha = 0.12f)   // fade to near-invisible
            )
        )

    // ==================== ACCENT GRADIENTS ====================

    /**
     * Subtle accent for stat cards
     */
    @Composable
    fun accentBar(color: Color): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.6f)
            )
        )
    }

    /**
     * Radial gradient for badge backgrounds
     */
    @Composable
    fun radialGlow(color: Color): Brush {
        return Brush.radialGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.7f),
                color.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Extension function to get gradient brush for GoalCategory
 */
@Composable
fun GoalCategory.gradient(): Brush = LifePlannerGradients.forCategory(this)
