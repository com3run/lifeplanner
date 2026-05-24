package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.goals.GoalsScreen
import az.tribe.lifeplanner.ui.navigation.Screen

/** D7 preview route for the redesigned Goals canvas — new-goal and open-goal reuse existing flows. */
internal fun NavGraphBuilder.appNavGoalsRedesign(navController: NavController) {
    composable(Screen.GoalsRedesign.route) {
        GoalsScreen(
            onBackClick = { navController.popBackStack() },
            onNewGoal = { navController.navigate(Screen.GoalWizard.route) { launchSingleTop = true } },
            onOpenGoal = { id -> navController.navigate("goal_detail/$id") { launchSingleTop = true } },
        )
    }
}
