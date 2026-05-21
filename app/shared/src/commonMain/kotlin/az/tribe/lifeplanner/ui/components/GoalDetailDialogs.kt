package az.tribe.lifeplanner.ui.components

import androidx.compose.runtime.Composable
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.goal.AddMilestoneDialog
import az.tribe.lifeplanner.ui.goal.AllMilestonesCompletedDialog
import az.tribe.lifeplanner.ui.goal.CompleteGoalDialog
import az.tribe.lifeplanner.ui.goal.DeleteGoalDialog
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.goal.NotesDialog

@Composable
fun GoalDetailDialogs(
    goal: Goal,
    goalId: String,
    viewModel: GoalViewModel,
    showDeleteDialog: Boolean,
    showNotesDialog: Boolean,
    showAddMilestoneDialog: Boolean,
    showCompleteConfirmDialog: Boolean,
    showAllMilestonesCompletedDialog: Boolean,
    onDismissDelete: () -> Unit,
    onDismissNotes: () -> Unit,
    onDismissAddMilestone: () -> Unit,
    onCompleteConfirmed: () -> Unit,
    onDismissComplete: () -> Unit,
    onAllMilestonesConfirmed: () -> Unit,
    onDismissAllMilestonesCompleted: () -> Unit,
    onBackClick: () -> Unit
) {
    if (showDeleteDialog) {
        DeleteGoalDialog(
            goalTitle = goal.title,
            onConfirm = {
                viewModel.deleteGoal(goalId)
                onBackClick()
            },
            onDismiss = onDismissDelete
        )
    }

    if (showNotesDialog) {
        NotesDialog(
            currentNotes = goal.notes,
            onSave = { newNotes ->
                viewModel.updateGoalNotes(goalId, newNotes)
                onDismissNotes()
            },
            onDismiss = onDismissNotes
        )
    }

    if (showAddMilestoneDialog) {
        AddMilestoneDialog(
            onAdd = { title, dueDate ->
                viewModel.addMilestone(goalId, title, dueDate)
                onDismissAddMilestone()
            },
            onDismiss = onDismissAddMilestone
        )
    }

    if (showCompleteConfirmDialog) {
        CompleteGoalDialog(
            goalTitle = goal.title,
            onConfirm = {
                viewModel.updateGoalStatus(goalId, GoalStatus.COMPLETED)
                onCompleteConfirmed()
            },
            onDismiss = onDismissComplete
        )
    }

    if (showAllMilestonesCompletedDialog) {
        AllMilestonesCompletedDialog(
            goalTitle = goal.title,
            onConfirm = {
                viewModel.updateGoalStatus(goalId, GoalStatus.COMPLETED)
                onAllMilestonesConfirmed()
            },
            onDismiss = onDismissAllMilestonesCompleted
        )
    }
}