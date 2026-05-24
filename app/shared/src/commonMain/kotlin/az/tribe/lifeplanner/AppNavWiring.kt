package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.wiring.YourWiringScreen

internal fun NavGraphBuilder.appNavWiring(navController: NavController) {
    composable(Screen.YourWiring.route) {
        YourWiringScreen(onBackClick = { navController.popBackStack() })
    }
}
