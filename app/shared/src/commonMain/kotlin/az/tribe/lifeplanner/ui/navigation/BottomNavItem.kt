package az.tribe.lifeplanner.ui.navigation

import az.tribe.lifeplanner.core.FeatureFlags

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.ArrowsClockwise
import com.adamglin.phosphoricons.fill.Flag
import com.adamglin.phosphoricons.fill.Flower
import com.adamglin.phosphoricons.fill.House
import com.adamglin.phosphoricons.fill.Lifebuoy
import com.adamglin.phosphoricons.fill.SquaresFour
import com.adamglin.phosphoricons.fill.Star
import com.adamglin.phosphoricons.fill.User
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Flower
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.Lifebuoy
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.User

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.ForYou.route,
        title = "Today",
        selectedIcon = PhosphorIcons.Fill.House,
        unselectedIcon = PhosphorIcons.Regular.House
    )

    data object Goals : BottomNavItem(
        route = Screen.Goals.route,
        title = "Goals",
        selectedIcon = PhosphorIcons.Fill.Flag,
        unselectedIcon = PhosphorIcons.Regular.Flag
    )

    data object Habits : BottomNavItem(
        route = Screen.HabitTracker.route,
        title = "Habits",
        selectedIcon = PhosphorIcons.Fill.ArrowsClockwise,
        unselectedIcon = PhosphorIcons.Regular.ArrowsClockwise
    )

    data object Hub : BottomNavItem(
        // 2026-07-21: reverted to the v2 middle tab, the Journal hub whose sub-tabs are
        // Goals / Habits / Journal / Planner. v3 had pointed this at the standalone
        // GoalsRedesign screen.
        route = Screen.Journal.route,
        title = "Artifact",
        selectedIcon = PhosphorIcons.Fill.SquaresFour,
        unselectedIcon = PhosphorIcons.Regular.SquaresFour
    )

    data object Abilities : BottomNavItem(
        route = Screen.Abilities.route,
        title = "Abilities",
        selectedIcon = PhosphorIcons.Fill.Star,
        unselectedIcon = PhosphorIcons.Regular.Star
    )

    data object Profile : BottomNavItem(
        // 2026-07-21: reverted to v2. Third tab is the legacy Profile, whose pillar rows are
        // FeatureFlags-gated (so they vanish with the pillars). The redesigned YouScreen links
        // to Becoming/Wiring/Causal without guards, which would be dead taps once pillars are off.
        route = Screen.Profile.route,
        title = "You",
        selectedIcon = PhosphorIcons.Fill.User,
        unselectedIcon = PhosphorIcons.Regular.User
    )

    companion object {
        val items = listOf(Home, Hub, Profile)
    }
}
