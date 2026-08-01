package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.possibility.PossibilityModeScreen

/** Pillar 6 (TRI-20) — Possibility Mode, opened for a stuck goal from Goal Detail or the For You feed. */
internal fun NavGraphBuilder.appNavPossibilityMode(navController: NavController) {
    composable(
        route = Screen.PossibilityMode.route,
        arguments = listOf(navArgument("goalId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val goalId = backStackEntry.arguments?.read { getStringOrNull("goalId") } ?: return@composable
        PossibilityModeScreen(
            goalId = goalId,
            onBackClick = { navController.popBackStack() },
            onOpenGoal = { id ->
                navController.navigate("goal_detail_redesign/$id") {
                    popUpTo(Screen.PossibilityMode.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onOpenDecision = { id ->
                navController.navigate("decision_detail/$id") {
                    popUpTo(Screen.PossibilityMode.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onTalkToCoach = { coachId, message ->
                // Seed the opener so chat auto-sends it and the matched coach reacts to this situation.
                az.tribe.lifeplanner.ui.chat.InsightMessageHolder.pendingMessage = message
                navController.navigate("ai_chat/$coachId") {
                    popUpTo(Screen.PossibilityMode.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}
