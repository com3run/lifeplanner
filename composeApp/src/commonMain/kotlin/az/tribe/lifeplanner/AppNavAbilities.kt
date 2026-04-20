package az.tribe.lifeplanner

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.ui.ability.AbilityDetailScreen
import az.tribe.lifeplanner.ui.ability.AbilityScreen
import az.tribe.lifeplanner.ui.ability.CreateAbilityScreen
import az.tribe.lifeplanner.ui.navigation.Screen

internal fun NavGraphBuilder.appNavAbilities(
    navController: NavController,
    tabIndex: Map<String, Int>,
    slideOffset: (Int) -> Int
) {
    // Abilities Tab Screen (bottom nav)
    composable(
        Screen.Abilities.route,
        enterTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideInHorizontally(tween(300)) { w ->
                    if (fromIndex > toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeIn(tween(300))
            } else fadeIn(tween(300))
        },
        exitTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideOutHorizontally(tween(300)) { w ->
                    if (fromIndex < toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeOut(tween(300))
            } else fadeOut(tween(300))
        },
        popEnterTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideInHorizontally(tween(300)) { w ->
                    if (fromIndex > toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeIn(tween(300))
            } else fadeIn(tween(300))
        },
        popExitTransition = {
            val fromIndex = tabIndex[initialState.destination.route]
            val toIndex = tabIndex[targetState.destination.route]
            if (fromIndex != null && toIndex != null) {
                slideOutHorizontally(tween(300)) { w ->
                    if (fromIndex < toIndex) -slideOffset(w) else slideOffset(w)
                } + fadeOut(tween(300))
            } else fadeOut(tween(300))
        }
    ) {
        AbilityScreen(
            onAbilityClick = { abilityId ->
                navController.navigate("ability_detail/$abilityId")
            },
            onCreateAbility = {
                navController.navigate(Screen.CreateAbility.route)
            }
        )
    }

    composable(
        route = Screen.AbilityDetail.route,
        arguments = listOf(navArgument("abilityId") { type = NavType.StringType })
    ) { backStackEntry ->
        val abilityId = backStackEntry.arguments?.read { getStringOrNull("abilityId") }
            ?: return@composable
        AbilityDetailScreen(
            abilityId = abilityId,
            onBackClick = { navController.popBackStack() },
            onGoalClick = { goalId ->
                navController.navigate("goal_detail/$goalId") {
                    launchSingleTop = true
                }
            }
        )
    }

    composable(Screen.CreateAbility.route) {
        CreateAbilityScreen(
            onAbilityCreated = { abilityId ->
                // Pop creation screen, then open detail so back returns to the list
                navController.popBackStack()
                navController.navigate("ability_detail/$abilityId")
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
