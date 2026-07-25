package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.domain.service.KnowledgeLibrary
import az.tribe.lifeplanner.ui.foryou.KnowledgeDetailScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavKnowledge(navController: NavController) {
    composable(
        route = Screen.KnowledgeDetail.route,
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.read { getStringOrNull("id") } ?: return@composable
        val bit = KnowledgeLibrary.byId(id) ?: return@composable
        KnowledgeDetailScreen(
            bit = bit,
            onBackClick = { navController.popBackStack() },
            onStartHabit = { navController.navigate(Screen.AddHabit.route) { launchSingleTop = true } },
            onSetGoal = { navController.navigate(Screen.GoalWizard.route) { launchSingleTop = true } },
        )
    }
}
