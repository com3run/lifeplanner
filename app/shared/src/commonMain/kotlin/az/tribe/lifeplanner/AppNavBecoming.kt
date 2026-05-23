package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.becoming.BecomingScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavBecoming(navController: NavController) {
    composable(Screen.Becoming.route) {
        BecomingScreen(onBackClick = { navController.popBackStack() })
    }
}
