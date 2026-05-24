package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.ui.goals.GoalDetailScreen
import az.tribe.lifeplanner.ui.navigation.Screen

/** D7 — the redesigned Goal Detail; opened from the redesigned Goals tab. Edit reuses the existing flow. */
internal fun NavGraphBuilder.appNavGoalDetailRedesign(navController: NavController) {
    composable(
        route = Screen.GoalDetailRedesign.route,
        arguments = listOf(navArgument("goalId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val goalId = backStackEntry.arguments?.read { getStringOrNull("goalId") } ?: return@composable
        GoalDetailScreen(
            goalId = goalId,
            onBackClick = { navController.popBackStack() },
            onEdit = { navController.navigate("edit_goal/$goalId") { launchSingleTop = true } },
        )
    }
}
