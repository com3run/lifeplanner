package az.tribe.lifeplanner.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddGoal : Screen("add_goal")
    object Analytics : Screen("analytics")
    object EditGoal : Screen("edit_goal/{goalId}")
}