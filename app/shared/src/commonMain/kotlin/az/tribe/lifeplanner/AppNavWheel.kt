package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.wheel.WheelScreen
import az.tribe.lifeplanner.ui.wheel.WheelViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun NavGraphBuilder.appNavWheel(navController: NavController) {
    composable(Screen.WheelOfLife.route) {
        val viewModel: WheelViewModel = koinViewModel()
        WheelScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
    }
}
