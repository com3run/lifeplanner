package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.ui.habit.HabitDetailScreen
import az.tribe.lifeplanner.ui.habit.HabitPracticeScreen
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
            onPractice = { id -> navController.navigate("habit_practice/$id") { launchSingleTop = true } },
        )
    }

    // The practice ground: run the timer / tap out the reps, and it checks itself in.
    composable(
        route = Screen.HabitPractice.route,
        arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val habitId = backStackEntry.arguments?.read { getStringOrNull("habitId") } ?: return@composable
        HabitPracticeScreen(
            habitId = habitId,
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
