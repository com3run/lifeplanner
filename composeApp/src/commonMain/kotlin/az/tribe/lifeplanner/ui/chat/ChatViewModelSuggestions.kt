package az.tribe.lifeplanner.ui.chat

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.CoachSuggestion
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.domain.model.Milestone
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Execute a coach suggestion (create goal, habit, journal entry, or check-in habit)
 */
@OptIn(ExperimentalUuidApi::class)
fun ChatViewModel.executeCoachSuggestion(suggestion: CoachSuggestion) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(executingAction = true, error = null)

        // Find the message containing this suggestion to persist executed state
        val messageWithSuggestion = _uiState.value.messages.find { message ->
            message.metadata?.coachSuggestions?.any { it.id == suggestion.id } == true
        }

        try {
            when (suggestion) {
                is CoachSuggestion.CreateGoal -> {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val dueDate = when (suggestion.timeline) {
                        "SHORT_TERM" -> now.date.plus(30, DateTimeUnit.DAY)
                        "MID_TERM" -> now.date.plus(90, DateTimeUnit.DAY)
                        "LONG_TERM" -> now.date.plus(365, DateTimeUnit.DAY)
                        else -> now.date.plus(90, DateTimeUnit.DAY)
                    }

                    // Convert suggested milestones to Milestone objects
                    val milestones = suggestion.milestones.mapIndexed { index, suggested ->
                        val milestoneDueDate = now.date.plus(
                            (suggested.weekOffset.coerceAtLeast(1) * 7).toLong(),
                            DateTimeUnit.DAY
                        )
                        Milestone(
                            id = Uuid.random().toString(),
                            title = suggested.title,
                            isCompleted = false,
                            dueDate = milestoneDueDate
                        )
                    }

                    val goal = Goal(
                        id = Uuid.random().toString(),
                        category = GoalCategory.entries.find { it.name == suggestion.category }
                            ?: GoalCategory.CAREER,
                        title = suggestion.title,
                        description = suggestion.description,
                        status = GoalStatus.NOT_STARTED,
                        timeline = GoalTimeline.entries.find { it.name == suggestion.timeline }
                            ?: GoalTimeline.MID_TERM,
                        dueDate = dueDate,
                        progress = 0,
                        milestones = milestones,
                        createdAt = now
                    )
                    goalRepository.insertGoal(goal)

                    val milestoneCount = milestones.size
                    val feedback = if (milestoneCount > 0) {
                        "Goal '${suggestion.title}' created with $milestoneCount milestones!"
                    } else {
                        "Goal '${suggestion.title}' created!"
                    }
                    _uiState.value = _uiState.value.copy(
                        executingAction = false,
                        actionFeedback = feedback,
                        executedSuggestionIds = _uiState.value.executedSuggestionIds + suggestion.id
                    )
                }

                is CoachSuggestion.CreateHabit -> {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val habit = Habit(
                        id = Uuid.random().toString(),
                        title = suggestion.title,
                        description = suggestion.description,
                        category = GoalCategory.entries.find { it.name == suggestion.category }
                            ?: GoalCategory.WELLBEING,
                        frequency = if (suggestion.frequency == "WEEKLY") HabitFrequency.WEEKLY else HabitFrequency.DAILY,
                        targetCount = suggestion.targetCount,
                        unit = suggestion.targetUnit,
                        linkedGoalId = suggestion.goalId,
                        createdAt = now
                    )
                    habitRepository.insertHabit(habit)
                    _uiState.value = _uiState.value.copy(
                        executingAction = false,
                        actionFeedback = "Habit '${suggestion.title}' created!",
                        executedSuggestionIds = _uiState.value.executedSuggestionIds + suggestion.id
                    )
                }

                is CoachSuggestion.CreateJournalEntry -> {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val entry = JournalEntry(
                        id = Uuid.random().toString(),
                        title = suggestion.title,
                        content = suggestion.content,
                        mood = suggestion.mood?.let { moodStr ->
                            Mood.entries.find { it.name == moodStr }
                        } ?: Mood.NEUTRAL,
                        date = now.date,
                        createdAt = now
                    )
                    journalRepository.insertEntry(entry)
                    _uiState.value = _uiState.value.copy(
                        executingAction = false,
                        actionFeedback = "Journal entry created!",
                        executedSuggestionIds = _uiState.value.executedSuggestionIds + suggestion.id
                    )
                }

                is CoachSuggestion.CheckInHabit -> {
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    habitRepository.checkIn(suggestion.habitId, today)
                    _uiState.value = _uiState.value.copy(
                        executingAction = false,
                        actionFeedback = "Checked in: ${suggestion.habitTitle}",
                        executedSuggestionIds = _uiState.value.executedSuggestionIds + suggestion.id
                    )
                }

                is CoachSuggestion.AskQuestion -> {
                    // Mark the question as answered — the selected option is sent
                    // as a regular message via the onAnswerQuestion callback in the UI
                    _uiState.value = _uiState.value.copy(
                        executingAction = false,
                        executedSuggestionIds = _uiState.value.executedSuggestionIds + suggestion.id
                    )
                }
            }

            // Persist executed suggestion to database
            messageWithSuggestion?.let { message ->
                chatRepository.markSuggestionExecuted(message.id, suggestion.id)
            }

            // Refresh user context after action
            loadUserContext()

            // Auto-send hidden follow-up so the coach continues the conversation
            val followUp = buildFollowUpMessage(suggestion)
            if (followUp != null) {
                sendHiddenFollowUp(followUp)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                executingAction = false,
                error = e.message ?: "Failed to execute action"
            )
        }
    }
}

fun ChatViewModel.buildFollowUpMessage(suggestion: CoachSuggestion): String? {
    return when (suggestion) {
        is CoachSuggestion.CreateGoal ->
            "I just added the goal \"${suggestion.title}\" to my list. What should I focus on first to get started?"
        is CoachSuggestion.CreateHabit ->
            "I just created the habit \"${suggestion.title}\". Any tips on how to stay consistent with it?"
        is CoachSuggestion.CreateJournalEntry ->
            "I just saved that journal entry. What else should I reflect on?"
        is CoachSuggestion.CheckInHabit ->
            "Done! I checked in for \"${suggestion.habitTitle}\". How am I doing overall?"
        is CoachSuggestion.AskQuestion -> null
    }
}
