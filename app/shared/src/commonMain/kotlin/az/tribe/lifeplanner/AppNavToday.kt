package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.today.TodayScreen

internal fun NavGraphBuilder.appNavToday(navController: NavController) {
    composable(Screen.Today.route) {
        // Today is now the first bottom-tab (a root) — no back arrow.
        TodayScreen(
            onBackClick = {},
            showBack = false,
            onOpenHabit = { id -> navController.navigate("habit_detail_redesign/$id") { launchSingleTop = true } },
        )
    }
}
