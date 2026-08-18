package az.tribe.lifeplanner.ui.navigation

import az.tribe.lifeplanner.core.FeatureFlags
import az.tribe.lifeplanner.domain.service.DayPhase

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.ArrowsClockwise
import com.adamglin.phosphoricons.fill.Flag
import com.adamglin.phosphoricons.fill.Flower
import com.adamglin.phosphoricons.fill.Lifebuoy
import com.adamglin.phosphoricons.fill.Moon
import com.adamglin.phosphoricons.fill.SquaresFour
import com.adamglin.phosphoricons.fill.Star
import com.adamglin.phosphoricons.fill.Sun
import com.adamglin.phosphoricons.fill.SunHorizon
import com.adamglin.phosphoricons.fill.User
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Flower
import com.adamglin.phosphoricons.regular.Lifebuoy
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.SunHorizon
import com.adamglin.phosphoricons.regular.User

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {

    /**
     * The icon to draw right now. Fixed for every tab except [Home], which is named after the
     * present moment and so has to look like it.
     */
    open fun iconFor(phase: DayPhase, selected: Boolean): ImageVector =
        if (selected) selectedIcon else unselectedIcon

    /**
     * The tab the app opens on. It used to be "Today" under a house, which named a date and a
     * building, neither of which is what the screen is about. It is the present: what is happening
     * now, in the hour you opened the app. So the icon is the sky at the hour you open it.
     */
    data object Home : BottomNavItem(
        route = Screen.ForYou.route,
        title = "Present",
        selectedIcon = PhosphorIcons.Fill.Sun,
        unselectedIcon = PhosphorIcons.Regular.Sun
    ) {
        override fun iconFor(phase: DayPhase, selected: Boolean): ImageVector = when (phase) {
            DayPhase.NIGHT -> if (selected) PhosphorIcons.Fill.Moon else PhosphorIcons.Regular.Moon
            DayPhase.DAWN, DayPhase.DUSK ->
                if (selected) PhosphorIcons.Fill.SunHorizon else PhosphorIcons.Regular.SunHorizon
            DayPhase.DAY -> if (selected) PhosphorIcons.Fill.Sun else PhosphorIcons.Regular.Sun
        }
    }

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
