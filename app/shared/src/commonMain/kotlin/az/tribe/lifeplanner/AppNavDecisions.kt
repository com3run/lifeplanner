package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.ui.decision.DecisionDetailScreen
import az.tribe.lifeplanner.ui.decision.DecisionJournalScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavDecisions(navController: NavController) {
    composable(Screen.DecisionJournal.route) {
        DecisionJournalScreen(
            onBackClick = { navController.popBackStack() },
            onDecisionClick = { id ->
                navController.navigate("decision_detail/$id") { launchSingleTop = true }
            }
        )
    }

    composable(
        route = Screen.DecisionDetail.route,
        arguments = listOf(navArgument("decisionId") { type = NavType.StringType })
    ) { backStackEntry ->
        val decisionId = backStackEntry.arguments?.read { getStringOrNull("decisionId") }
            ?: return@composable
        DecisionDetailScreen(
            decisionId = decisionId,
            onBackClick = { navController.popBackStack() }
        )
    }
}
