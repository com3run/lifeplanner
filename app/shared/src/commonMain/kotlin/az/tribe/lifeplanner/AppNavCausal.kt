package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.causal.CausalInsightsScreen
import az.tribe.lifeplanner.ui.causal.CustomerCenterScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavCausal(navController: NavController) {
    composable(Screen.CausalInsights.route) {
        CausalInsightsScreen(onBackClick = { navController.popBackStack() })
    }
    composable(Screen.CustomerCenter.route) {
        CustomerCenterScreen(onBackClick = { navController.popBackStack() })
    }
}
