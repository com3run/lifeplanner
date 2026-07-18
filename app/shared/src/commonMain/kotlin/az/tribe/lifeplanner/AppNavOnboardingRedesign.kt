package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingViewModel
import az.tribe.lifeplanner.ui.onboarding.IntroFlow
import az.tribe.lifeplanner.ui.onboarding.OnboardingFlowScreen
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject

/**
 * D11 intro: the first half of first run. Finishing it marks the intro complete and hands off to
 * the coach flow, which collects the name, picks the coach, and seeds the first goals and habits.
 *
 * The same route is also reachable from Profile's "Preview" section by an existing user, so the
 * destination is decided by whether the coach flow is already done rather than being hardcoded:
 * a returning previewer must not be dropped back into coach onboarding they finished long ago.
 *
 * @param homeRoute where to land when there is no coach flow left to run.
 */
internal fun NavGraphBuilder.appNavOnboardingRedesign(
    navController: NavController,
    homeRoute: String,
) {
    composable(Screen.OnboardingRedesign.route) {
        val settings: Settings = koinInject()
        OnboardingFlowScreen(
            onFinish = {
                IntroFlow.markComplete(settings)
                val next =
                    if (CoachOnboardingViewModel.isComplete(settings)) homeRoute
                    else Screen.CoachOnboarding.route
                navController.navigate(next) {
                    // Clear the intro off the back stack: pressing back from the coach flow must
                    // not return to a completed intro.
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}
