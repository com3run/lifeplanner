package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.auth.SignInScreen
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingScreen

internal fun NavGraphBuilder.appNavAuth(navController: NavController) {
    // Coach Onboarding, entry point for unauthenticated users; embeds auth gate
    composable(Screen.CoachOnboarding.route) {
        CoachOnboardingScreen(
            onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CoachOnboarding.route) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    // Sign In Screen, standalone, accessible from other flows
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
