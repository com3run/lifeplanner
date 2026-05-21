package az.tribe.lifeplanner

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import az.tribe.lifeplanner.data.analytics.FacebookAnalytics
import az.tribe.lifeplanner.ui.goal.AddGoalFromTemplateScreen
import az.tribe.lifeplanner.ui.gamification.AchievementsScreen
import az.tribe.lifeplanner.ui.analytics.AnalyticsDashboard
import az.tribe.lifeplanner.ui.goal.DependencyGraphScreen
import az.tribe.lifeplanner.ui.goal.EditGoalScreen
import az.tribe.lifeplanner.ui.goal.GoalCreationWizardScreen
import az.tribe.lifeplanner.ui.goal.GoalDetailScreen
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.goal.GoalsScreen
import az.tribe.lifeplanner.ui.goal.SmartGoalGeneratorScreen
import az.tribe.lifeplanner.ui.goal.TemplatePickerScreen
import az.tribe.lifeplanner.ui.navigation.Screen
import androidx.compose.runtime.LaunchedEffect

internal fun NavGraphBuilder.appNavGoals(
    navController: NavController,
    viewModel: GoalViewModel,
    onHubTabSelected: (Int) -> Unit
) {
    // Goals Screen (Goal List)
    composable(Screen.Goals.route) {
        GoalsScreen(
            viewModel = viewModel,
            onGoalClick = { goal ->
                navController.navigate("goal_detail/${goal.id}")
            },
            onAddGoalClick = {
                navController.navigate(Screen.GoalWizard.route)
            },
            onAiGenerateClick = {
                navController.navigate(Screen.AiGoalGeneration.route) {
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    // Goal Detail Screen
    composable(
        route = "goal_detail/{goalId}",
        arguments = listOf(navArgument("goalId") { type = NavType.StringType })
    ) { backStackEntry ->
        val goalId = backStackEntry.arguments?.read { getStringOrNull("goalId") }
            ?: return@composable
        LaunchedEffect(goalId) { FacebookAnalytics.logViewContent(goalId, "goal") }
        GoalDetailScreen(
            goalId = goalId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onEditClick = { navController.navigate("edit_goal/$goalId") },
            onViewDependencyGraph = { id ->
                navController.navigate("dependency_graph/$id") {
                    launchSingleTop = true
                }
            },
            onNavigateToGoal = { id ->
                navController.navigate("goal_detail/$id") {
                    launchSingleTop = true
                }
            },
            onNavigateToJournal = { entryId ->
                navController.navigate("journal_entry_detail/$entryId") {
                    launchSingleTop = true
                }
            },
            onReflectOnGoal = { id ->
                navController.navigate("journal_wizard?goalId=$id") {
                    launchSingleTop = true
                }
            },
            onCoachClick = { coachId ->
                navController.navigate("coach_profile/$coachId") {
                    launchSingleTop = true
                }
            },
            onAbilityClick = { abilityId ->
                navController.navigate("ability_detail/$abilityId") {
                    launchSingleTop = true
                }
            }
        )
    }

    // Goal Creation Wizard (primary flow)
    composable(Screen.GoalWizard.route) {
        GoalCreationWizardScreen(
            viewModel = viewModel,
            onGoalCreated = { goalId ->
                onHubTabSelected(1) // Goals tab in hub
                navController.navigate("goal_detail/$goalId") {
                    popUpTo(Screen.GoalWizard.route) { inclusive = true }
                }
            },
            onBackClick = { navController.popBackStack() }
        )
    }

    // Edit Goal Screen
    composable(
        route = Screen.EditGoal.route,
        arguments = listOf(navArgument("goalId") { type = NavType.StringType })
    ) { backStackEntry ->
        val goalId = backStackEntry.arguments?.read { getStringOrNull("goalId") }
            ?: return@composable
        EditGoalScreen(
            goalId = goalId,
            viewModel = viewModel,
            onGoalSaved = { navController.popBackStack() },
            onBackClick = { navController.popBackStack() }
        )
    }

    // Analytics Dashboard
    composable(Screen.Analytics.route) {
        AnalyticsDashboard(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
        )
    }

    // AI Goal Generation Screen
    composable(Screen.AiGoalGeneration.route) {
        SmartGoalGeneratorScreen(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onComplete = {
                navController.navigate(Screen.Goals.route) {
                    popUpTo(Screen.AiGoalGeneration.route) { inclusive = true }
                }
            }
        )
    }

    // Achievements Screen
    composable(Screen.Achievements.route) {
        AchievementsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Dependency Graph Screen
    composable(Screen.DependencyGraph.route) {
        DependencyGraphScreen(
            onNavigateBack = { navController.popBackStack() },
            onGoalClick = { goalId ->
                navController.navigate("goal_detail/$goalId")
            }
        )
    }

    // Dependency Graph for specific Goal
    composable(
        route = Screen.DependencyGraphForGoal.route,
        arguments = listOf(navArgument("goalId") { type = NavType.StringType })
    ) { backStackEntry ->
        val goalId = backStackEntry.arguments?.read { getStringOrNull("goalId") }
            ?: return@composable
        DependencyGraphScreen(
            focusGoalId = goalId,
            onNavigateBack = { navController.popBackStack() },
            onGoalClick = { id ->
                navController.navigate("goal_detail/$id")
            }
        )
    }

    // Templates Screen
    composable(Screen.Templates.route) {
        TemplatePickerScreen(
            onBackClick = { navController.popBackStack() },
            onTemplateSelected = { template ->
                navController.navigate("add_goal_from_template/${template.id}") {
                    launchSingleTop = true
                }
            }
        )
    }

    // Add Goal from Template
    composable(
        route = Screen.AddGoalFromTemplate.route,
        arguments = listOf(navArgument("templateId") { type = NavType.StringType })
    ) { backStackEntry ->
        val templateId = backStackEntry.arguments?.read { getStringOrNull("templateId") }
            ?: return@composable
        AddGoalFromTemplateScreen(
            templateId = templateId,
            viewModel = viewModel,
            onGoalSaved = {
                navController.navigate(Screen.Goals.route) {
                    popUpTo(Screen.Templates.route) { inclusive = true }
                }
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
