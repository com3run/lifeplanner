package az.tribe.lifeplanner

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.profile.ProfileScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavProfile(
    navController: NavController,
    tabIndex: Map<String, Int>,
    slideOffset: (Int) -> Int
) {
    composable(
        Screen.Profile.route,
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
        ProfileScreen(
            onNavigateToAchievements = {
                navController.navigate(Screen.Achievements.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToHealth = {
                navController.navigate(Screen.Health.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToReminders = {
                navController.navigate(Screen.Reminders.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToBackup = {
                navController.navigate(Screen.BackupSettings.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToRetrospective = {
                navController.navigate(Screen.Retrospective.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToAICoach = {
                navController.navigate(Screen.AIChat.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToSignIn = {
                navController.navigate("sign_in") {
                    launchSingleTop = true
                }
            },
            onNavigateToFeedback = {
                navController.navigate(Screen.Feedback.route) {
                    launchSingleTop = true
                }
            }
        )
    }

    // Feedback Screen
    composable(Screen.Feedback.route) {
        az.tribe.lifeplanner.ui.feedback.FeedbackScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
