package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import az.tribe.lifeplanner.data.analytics.FacebookAnalytics
import az.tribe.lifeplanner.ui.auth.OnboardingScreen
import az.tribe.lifeplanner.ui.auth.SignInScreen
import az.tribe.lifeplanner.ui.auth.WelcomeScreen
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingScreen
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingViewModel
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject

internal fun NavGraphBuilder.appNavAuth(navController: NavController) {
    // Welcome Screen (video background + auth buttons)
    composable(Screen.Welcome.route) {
        val settings: Settings = koinInject()
        val authViewModel: AuthViewModel = koinInject()
        val hasCompletedOnboarding by authViewModel.hasCompletedOnboarding.collectAsState()
        WelcomeScreen(
            onComplete = {
                // Existing users (hasCompletedOnboarding=true) skip coach onboarding — mark as done
                if (hasCompletedOnboarding == true && !CoachOnboardingViewModel.isComplete(settings)) {
                    settings.putBoolean(CoachOnboardingViewModel.COACH_ONBOARDING_KEY, true)
                }
                val next = if (CoachOnboardingViewModel.isComplete(settings)) Screen.Home.route
                else Screen.CoachOnboarding.route
                navController.navigate(next) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
        )
    }

    // Onboarding Screen (kept for existing users)
    composable(Screen.Onboarding.route) {
        OnboardingScreen(
            onOnboardingComplete = {
                FacebookAnalytics.logCompleteTutorial()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        )
    }

    // Coach Onboarding — Luna profile questionnaire (shown once after first auth)
    composable(Screen.CoachOnboarding.route) {
        CoachOnboardingScreen(
            onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CoachOnboarding.route) { inclusive = true }
                }
            }
        )
    }

    // Sign In Screen
    composable("sign_in") {
        SignInScreen(
            onSignInSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo("sign_in") { inclusive = true }
                }
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
