package az.tribe.lifeplanner

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

        // Finishing the reading session is what counts as reading it: that marks it read, pays the
        // XP once, and clears the zone if it was the path's last lesson.
        val readVm: KnowledgeDetailViewModel = koinViewModel()
        val earnedXp by readVm.earnedXp.collectAsStateWithLifecycle()
        val earnedBadge by readVm.earnedBadgeName.collectAsStateWithLifecycle()
        val next by readVm.nextLesson.collectAsStateWithLifecycle()
        LaunchedEffect(id) { readVm.onOpened(id) }

        KnowledgeDetailScreen(
            bit = bit,
            onBackClick = { navController.popBackStack() },
            // Lesson to lesson replaces this entry rather than stacking, so Back is always one step
            // back to the hub however far along a path the reader has walked.
            onOpenLesson = { nextId ->
                navController.navigate("knowledge_detail/$nextId") {
                    popUpTo(Screen.KnowledgeDetail.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            nextLesson = next,
            onCompleted = { readVm.onLessonCompleted(id) },
            earnedXp = earnedXp,
            earnedBadgeName = earnedBadge,
        )
    }
}
