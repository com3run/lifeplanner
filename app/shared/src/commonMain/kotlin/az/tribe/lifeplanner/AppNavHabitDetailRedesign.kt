package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.ui.habit.HabitDetailScreen
import az.tribe.lifeplanner.ui.navigation.Screen

/** D7, the redesigned Habit Detail; opened by tapping a habit in the tracker. Edit reuses the bottom sheet. */
internal fun NavGraphBuilder.appNavHabitDetailRedesign(navController: NavController) {
    composable(
        route = Screen.HabitDetailRedesign.route,
        arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.read { getStringOrNull("habitId") } ?: return@composable
        HabitDetailScreen(
            habitId = habitId,
            onBackClick = { navController.popBackStack() },
            onOpenLesson = { id -> navController.navigate("knowledge_detail/$id") { launchSingleTop = true } },
        )
    }
}
