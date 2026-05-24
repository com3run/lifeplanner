package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.goals.GoalsScreen
import az.tribe.lifeplanner.ui.navigation.Screen

/** The redesigned Goals canvas — now the second bottom-tab (a root). New-goal / open-goal reuse existing flows. */
internal fun NavGraphBuilder.appNavGoalsRedesign(navController: NavController) {
    composable(Screen.GoalsRedesign.route) {
        GoalsScreen(
            onBackClick = {},
            showBack = false,
            onNewGoal = { navController.navigate(Screen.GoalWizard.route) { launchSingleTop = true } },
            onOpenGoal = { id -> navController.navigate("goal_detail/$id") { launchSingleTop = true } },
        )
    }
}
