package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.auth.SignInScreen
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingScreen

/**
 * @param homeRoute where first run and sign-in land. Both were hardcoded to [Screen.Home], the
 *   legacy home, which is not in `mainRoutes` — so finishing onboarding dropped the user on a
 *   screen with no bottom navigation.
 */
internal fun NavGraphBuilder.appNavAuth(navController: NavController, homeRoute: String) {
    // Coach Onboarding, entry point for unauthenticated users; embeds auth gate
    composable(Screen.CoachOnboarding.route) {
        CoachOnboardingScreen(
            onComplete = {
                navController.navigate(homeRoute) {
                    popUpTo(0) { inclusive = true }
                }
            },
            // D11 chain: past the auth gate but the intro has not run yet.
            onNeedsIntro = {
                navController.navigate(Screen.OnboardingRedesign.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    // Sign In Screen, standalone, accessible from other flows
    composable("sign_in") {
        SignInScreen(
            onSignInSuccess = {
                navController.navigate(homeRoute) {
                    popUpTo("sign_in") { inclusive = true }
                }
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
