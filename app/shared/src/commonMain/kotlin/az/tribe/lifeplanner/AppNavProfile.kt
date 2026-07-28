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
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingViewModel
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject

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
        val settings: Settings = koinInject()
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
            onNavigateToDecisions = {
                navController.navigate(Screen.DecisionJournal.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToCausalInsights = {
                navController.navigate(Screen.CausalInsights.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToLifeBalance = {
                navController.navigate(Screen.LifeBalance.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToTrajectory = {
                navController.navigate(Screen.Trajectory.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToBecoming = {
                navController.navigate(Screen.Becoming.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToDecisionReview = {
                navController.navigate(Screen.DecisionReview.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToYourWiring = {
                navController.navigate(Screen.YourWiring.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToRetrospective = {
                navController.navigate(Screen.Retrospective.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToScreenTimeInsight = {
                navController.navigate(Screen.ScreenTimeInsight.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToToday = {
                navController.navigate(Screen.Today.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToGoalsRedesign = {
                navController.navigate(Screen.GoalsRedesign.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToYouRedesign = {
                navController.navigate(Screen.YouRedesign.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToOnboardingRedesign = {
                navController.navigate(Screen.OnboardingRedesign.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToAICoach = {
                navController.navigate(Screen.AIChat.route) {
                    launchSingleTop = true
                }
            },
            onContinueCoachChat = { coachId ->
                navController.navigate("ai_chat/$coachId") {
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
            },
            onResetOnboarding = {
                settings.remove(CoachOnboardingViewModel.COACH_ONBOARDING_KEY)
                navController.navigate(Screen.CoachOnboarding.route) {
                    popUpTo(0) { inclusive = true }
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
