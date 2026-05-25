package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.onboarding.OnboardingFlowScreen

/** D11 preview route for the redesigned first-run flow, finishes by landing on the new Today. */
internal fun NavGraphBuilder.appNavOnboardingRedesign(navController: NavController) {
    composable(Screen.OnboardingRedesign.route) {
        OnboardingFlowScreen(
            onFinish = {
                navController.navigate(Screen.Today.route) {
                    popUpTo(Screen.OnboardingRedesign.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}
