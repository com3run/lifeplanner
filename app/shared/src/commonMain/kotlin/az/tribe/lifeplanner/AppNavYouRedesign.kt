package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.you.YouScreen

/** D7 preview route for the redesigned You canvas — rows route to existing destinations. */
internal fun NavGraphBuilder.appNavYouRedesign(navController: NavController) {
    composable(Screen.YouRedesign.route) {
        YouScreen(
            onBackClick = { navController.popBackStack() },
            onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } },
        )
    }
}
