package az.tribe.lifeplanner

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import az.tribe.lifeplanner.ui.GoalViewModel
import az.tribe.lifeplanner.ui.HomeScreen
import az.tribe.lifeplanner.ui.Screen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import az.tribe.lifeplanner.ui.AddGoalScreen
import az.tribe.lifeplanner.ui.EditGoalScreen
import az.tribe.lifeplanner.ui.theme.LifePlannerTheme
import az.tribe.lifeplanner.ui.theme.LifePlannerTypography

@Composable
@Preview
fun App(viewModel: GoalViewModel = koinInject()) {
    LifePlannerTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onGoalClick = { goal ->
                        navController.navigate("edit_goal/${goal.id}")
                    },
                    onAddGoalClick = {
                        navController.navigate(Screen.AddGoal.route)
                    }
                )
            }

            composable(Screen.AddGoal.route) {
                AddGoalScreen(
                    viewModel = viewModel,
                    onGoalSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditGoal.route,
                arguments = listOf(navArgument("goalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getString("goalId") ?: return@composable
                EditGoalScreen(
                    goalId = goalId,
                    viewModel = viewModel,
                    onGoalUpdated = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack()}
                )
            }
        }
    }
}