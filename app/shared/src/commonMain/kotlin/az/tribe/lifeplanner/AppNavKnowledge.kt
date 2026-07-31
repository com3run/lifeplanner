package az.tribe.lifeplanner

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

        // Reaching the end of the lesson is what counts as reading it: that marks it read, pays the
        // XP once, and clears the zone if it was the path's last lesson.
        val readVm: KnowledgeDetailViewModel = koinViewModel()
        val earnedXp by readVm.earnedXp.collectAsState()
        val earnedBadge by readVm.earnedBadgeName.collectAsState()

        KnowledgeDetailScreen(
            bit = bit,
            onBackClick = { navController.popBackStack() },
            onStartHabit = { navController.navigate(Screen.AddHabit.route) { launchSingleTop = true } },
            onSetGoal = { navController.navigate(Screen.GoalWizard.route) { launchSingleTop = true } },
            onCompleted = { readVm.onLessonCompleted(id) },
            earnedXp = earnedXp,
            earnedBadgeName = earnedBadge,
        )
    }
}
