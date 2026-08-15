package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.domain.model.TodayWeather
import az.tribe.lifeplanner.ui.foryou.ForYouScreen
import az.tribe.lifeplanner.ui.navigation.Screen

/**
 * The "For You" feed is the home tab (a root, no back arrow). Cards deep-link into the full screens
 * (causal insights, becoming, patterns, habit/goal detail), so the reflective You functions live on
 * the front door instead of being hidden in a tab.
 */
internal fun NavGraphBuilder.appNavForYou(
    navController: NavController,
    onOpenWeather: (TodayWeather) -> Unit,
) {
    composable(Screen.ForYou.route) {
        ForYouScreen(
            onOpenRoute = { route -> navController.navigate(route) { launchSingleTop = true } },
            onOpenWeather = onOpenWeather,
        )
    }
}
