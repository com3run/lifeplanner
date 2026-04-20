package az.tribe.lifeplanner

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.domain.service.UpdateState
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.home.HomeScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavHome(
    navController: NavController,
    viewModel: GoalViewModel,
    tabIndex: Map<String, Int>,
    slideOffset: (Int) -> Int,
    softUpdateDismissed: Boolean,
    updateState: UpdateState,
    onHubTabSelected: (Int) -> Unit,
    onSoftUpdateDismissed: () -> Unit
) {
    composable(
        Screen.Home.route,
        enterTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideInHorizontally(tween(300)) { w ->
                    if (fromIndex > toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeIn(tween(300))
            } else fadeIn(tween(300))
        },
        exitTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideOutHorizontally(tween(300)) { w ->
                    if (fromIndex < toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeOut(tween(300))
            } else fadeOut(tween(300))
        },
        popEnterTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideInHorizontally(tween(300)) { w ->
                    if (fromIndex > toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeIn(tween(300))
            } else fadeIn(tween(300))
        },
        popExitTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideOutHorizontally(tween(300)) { w ->
                    if (fromIndex < toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeOut(tween(300))
            } else fadeOut(tween(300))
        }
    ) {
        HomeScreen(
            viewModel = viewModel,
            onGoalClick = { goal ->
                navController.navigate("goal_detail/${goal.id}")
            },
            onAddGoalClick = {
                navController.navigate(Screen.GoalWizard.route)
            },
            goToAnalytics = {
                navController.navigate(Screen.Analytics.route)
            },
            goToAiGeneration = {
                navController.navigate(Screen.AiGoalGeneration.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToHabits = {
                navController.navigate("search?filter=HABITS") { launchSingleTop = true }
            },
            onNavigateToAddHabit = {
                navController.navigate(Screen.AddHabit.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToJournal = {
                navController.navigate(Screen.Journal.route) {
                    popUpTo(navController.graph.startDestinationRoute!!) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onNavigateToGoals = {
                navController.navigate("search?filter=GOALS") { launchSingleTop = true }
            },
            onNavigateToAchievements = {
                navController.navigate(Screen.Achievements.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToFocus = {
                navController.navigate("focus_setup") {
                    launchSingleTop = true
                }
            },
            onNavigateToProfile = {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(navController.graph.startDestinationRoute!!) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onNavigateToRetrospective = {
                navController.navigate(Screen.Retrospective.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToChat = {
                navController.navigate(Screen.AIChat.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToReminders = {
                navController.navigate(Screen.Reminders.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToLifeBalance = {
                navController.navigate(Screen.LifeBalance.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToHealth = {
                navController.navigate(Screen.Health.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToStoryReader = {
                navController.navigate(Screen.StoryReader.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToTemplates = {
                navController.navigate(Screen.Templates.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToAbilities = {
                navController.navigate(Screen.Abilities.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToAbilityDetail = { abilityId ->
                navController.navigate("ability_detail/$abilityId") {
                    launchSingleTop = true
                }
            },
            onContinueChat = { coachId ->
                navController.navigate("ai_chat/$coachId") {
                    launchSingleTop = true
                }
            },
            onStartFocusForMilestone = { goalId, milestoneId ->
                navController.navigate("focus_setup?goalId=$goalId&milestoneId=$milestoneId") {
                    launchSingleTop = true
                }
            },
            onNavigateToJournalEntry = { entryId ->
                navController.navigate("journal_entry_detail/$entryId") {
                    launchSingleTop = true
                }
            },
            showUpdateReminder = softUpdateDismissed && updateState is UpdateState.UpdateRequired,
            onUpdateClick = onSoftUpdateDismissed
        )
    }
}
