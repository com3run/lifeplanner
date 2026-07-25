package az.tribe.lifeplanner

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.domain.service.KnowledgeLibrary
import az.tribe.lifeplanner.ui.foryou.KnowledgeDetailScreen
import az.tribe.lifeplanner.ui.foryou.KnowledgeDetailViewModel
import az.tribe.lifeplanner.ui.foryou.LearnHubScreen
import az.tribe.lifeplanner.ui.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

internal fun NavGraphBuilder.appNavKnowledge(navController: NavController) {
    composable(Screen.LearnHub.route) {
        LearnHubScreen(
            onBack = { navController.popBackStack() },
            onOpen = { id -> navController.navigate("knowledge_detail/$id") { launchSingleTop = true } },
        )
    }

    composable(
        route = Screen.KnowledgeDetail.route,
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.read { getStringOrNull("id") } ?: return@composable
        val bit = KnowledgeLibrary.byId(id) ?: return@composable

        // Opening the lesson counts as reading it, which advances hub progress.
        val readVm: KnowledgeDetailViewModel = koinViewModel()
        LaunchedEffect(id) { readVm.markRead(id) }

        KnowledgeDetailScreen(
            bit = bit,
            onBackClick = { navController.popBackStack() },
            onStartHabit = { navController.navigate(Screen.AddHabit.route) { launchSingleTop = true } },
            onSetGoal = { navController.navigate(Screen.GoalWizard.route) { launchSingleTop = true } },
        )
    }
}
