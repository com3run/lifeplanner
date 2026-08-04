package az.tribe.lifeplanner.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.analytics.FacebookAnalytics
import az.tribe.lifeplanner.data.mapper.createNewMilestone
import az.tribe.lifeplanner.data.model.GoalTypeQuestions
import az.tribe.lifeplanner.data.model.UserQuestionnaireAnswers
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.GoalAnalytics
import az.tribe.lifeplanner.domain.model.GoalChange
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.enum.GoalFilter
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GeminiRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.service.SmartReminderManager
import az.tribe.lifeplanner.usecases.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

enum class QuestionnaireStep { INPUT, ANSWERING, GENERATING, RESULTS }

class GoalViewModel(
    goalRepository: GoalRepository,
    internal val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val getGoalByIdUseCase: GetGoalByIdUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val updateGoalStatusUseCase: UpdateGoalStatusUseCase,
    private val updateGoalNotesUseCase: UpdateGoalNotesUseCase,

    // Milestone Use Cases
    private val addMilestoneUseCase: AddMilestoneUseCase,
    private val toggleMilestoneCompletionUseCase: ToggleMilestoneCompletionUseCase,

    // Analytics and History Use Cases
    private val getGoalAnalyticsUseCase: GetGoalAnalyticsUseCase,
    private val getGoalHistoryUseCase: GetGoalHistoryUseCase,
    private val logGoalChangeUseCase: LogGoalChangeUseCase,

    internal val generateAiQuestionnaireUseCase: GenerateAiQuestionnaireUseCase,
    internal val generateAiGoalsUseCase: GenerateAiGoalsUseCase,
    internal val geminiRepository: GeminiRepository,
    internal val smartReminderManager: SmartReminderManager,
    private val gamificationRepository: GamificationRepository,
    private val lifeValueRepository: LifeValueRepository
) : ViewModel() {

    // Smart reminder events (one-shot, collected by UI for snackbar)
    private val _reminderEvent = MutableSharedFlow<String>()
    val reminderEvent: SharedFlow<String> = _reminderEvent.asSharedFlow()

    // State Management
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(GoalFilter.ALL)
    val selectedFilter: StateFlow<GoalFilter> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    internal val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Reactive goals: auto-updates from DB, with client-side search/filter
    val goals: StateFlow<List<Goal>> = combine(
        goalRepository.observeAllGoals(),
        _searchQuery,
        _selectedFilter
    ) { allGoals, query, filter ->
        var result = allGoals
        if (query.isNotBlank()) {
            result = result.filter { goal ->
                goal.title.contains(query, ignoreCase = true) ||
                    goal.description.contains(query, ignoreCase = true)
            }
        }
        when (filter) {
            GoalFilter.ALL -> result
            GoalFilter.ACTIVE -> result.filter { it.status != GoalStatus.COMPLETED }
            GoalFilter.COMPLETED -> result.filter { it.status == GoalStatus.COMPLETED }
        }
    }
        .onEach { _isLoading.value = false }
        .catch { e ->
            _error.value = "Failed to load goals: ${e.message}"
            _isLoading.value = false
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active life values for the "Why this goal?" picker (Pillar 1)
    val lifeValues: StateFlow<List<LifeValue>> =
        lifeValueRepository.observeAllLifeValues()
            .map { values -> values.filter { it.isActive } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _analytics = MutableStateFlow<GoalAnalytics?>(null)
    val analytics: StateFlow<GoalAnalytics?> = _analytics

    private val _goalHistory = MutableStateFlow<List<GoalChange>>(emptyList())
    val goalHistory: StateFlow<List<GoalChange>> = _goalHistory.asStateFlow()

    internal val _userPrompt = MutableStateFlow("")
    val userPrompt: StateFlow<String> = _userPrompt.asStateFlow()

    internal val _questions = MutableStateFlow<List<GoalTypeQuestions>>(emptyList())
    val questions: StateFlow<List<GoalTypeQuestions>> = _questions.asStateFlow()

    internal val _userAnswers = MutableStateFlow(UserQuestionnaireAnswers(emptyList()))
    val userAnswers: StateFlow<UserQuestionnaireAnswers> = _userAnswers.asStateFlow()

    internal val _questionnaireStep = MutableStateFlow(QuestionnaireStep.INPUT)
    val questionnaireStep: StateFlow<QuestionnaireStep> = _questionnaireStep.asStateFlow()

    internal val _isLoadingQuestions = MutableStateFlow(false)
    val isLoadingQuestions: StateFlow<Boolean> = _isLoadingQuestions.asStateFlow()

    internal val _isGeneratingPersonalizedGoals = MutableStateFlow(false)
    val isGeneratingPersonalizedGoals: StateFlow<Boolean> = _isGeneratingPersonalizedGoals.asStateFlow()

    internal val _generatedGoalsFromAI = MutableStateFlow<List<Goal>>(emptyList())
    val generatedGoalsFromAI: StateFlow<List<Goal>> = _generatedGoalsFromAI.asStateFlow()

    // Event to prompt user to complete goal when all milestones are done
    private val _promptCompleteGoal = MutableStateFlow<String?>(null)
    val promptCompleteGoal: StateFlow<String?> = _promptCompleteGoal.asStateFlow()

    fun clearCompleteGoalPrompt() {
        _promptCompleteGoal.value = null
    }

    // Search and Filter Functions (reactive: combine auto-re-evaluates)
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length >= 3) {
            FacebookAnalytics.logSearch(query, "goal")
        }
    }

    fun updateFilter(filter: GoalFilter) {
        _selectedFilter.value = filter
    }

    // Goal CRUD Operations
    fun createGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                // The "why", set for free. The wheel area is the one that matters: it is always
                // resolvable from the category the user picked, and it is what ties this goal to a
                // score they gave us. The free-text value is kept for the identity statements that
                // still use it, but it is no longer what the Why-Chain leads with.
                val withArea = if (goal.wheelArea == null) {
                    goal.copy(
                        wheelArea = az.tribe.lifeplanner.domain.service.GoalWheelAreaInferrer.infer(
                            category = goal.category, title = goal.title, description = goal.description,
                        )
                    )
                } else goal
                val finalGoal = if (withArea.valueId == null) {
                    val values = runCatching { lifeValueRepository.getActiveLifeValues() }.getOrDefault(emptyList())
                    withArea.copy(valueId = az.tribe.lifeplanner.domain.service.GoalValueInferrer.infer(
                        category = withArea.category, title = withArea.title, description = withArea.description, values = values,
                    ))
                } else withArea
                createGoalUseCase(finalGoal)
                gamificationRepository.awardXp(XpRewards.GOAL_CREATED.toLong())
                Analytics.goalCreated(finalGoal.category.name, "manual")
                val result = smartReminderManager.syncRemindersForGoal(finalGoal)
                if (result.hasChanges) {
                    _reminderEvent.emit("${result.total} smart reminder${if (result.total > 1) "s" else ""} set for \"${finalGoal.title}\"")
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to create goal: ${e.message}"
            }
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                val oldGoal = getGoalByIdUseCase(goal.id)
                updateGoalUseCase(goal)
                if (oldGoal != null && oldGoal.valueId != goal.valueId) {
                    logGoalChangeUseCase(
                        goalId = goal.id,
                        field = "valueId",
                        oldValue = oldGoal.valueId,
                        newValue = goal.valueId ?: ""
                    )
                }
                val result = smartReminderManager.syncRemindersForGoal(goal)
                if (result.created > 0) {
                    _reminderEvent.emit("Reminders updated for \"${goal.title}\"")
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update goal: ${e.message}"
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            try {
                val oldGoal = getGoalByIdUseCase(id)
                smartReminderManager.cleanupRemindersForDeletedGoal(id)
                deleteGoalUseCase(id)
                if (oldGoal != null) {
                    Analytics.goalAbandoned(id, oldGoal.category.name, oldGoal.progress?.toInt() ?: 0)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete goal: ${e.message}"
            }
        }
    }

    // Goal Progress and Status Updates
    fun updateGoalProgress(id: String, newProgress: Int) {
        viewModelScope.launch {
            try {
                val oldGoal = getGoalByIdUseCase(id)
                updateGoalProgressUseCase(id, newProgress)
                Analytics.goalProgressUpdated(id, newProgress)

                // Log the change
                if (oldGoal != null) {
                    logGoalChangeUseCase(
                        goalId = id,
                        field = "progress",
                        oldValue = oldGoal.progress?.toString() ?: "0",
                        newValue = newProgress.toString()
                    )
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update progress: ${e.message}"
            }
        }
    }

    fun updateGoalStatus(id: String, newStatus: GoalStatus) {
        viewModelScope.launch {
            try {
                val oldGoal = getGoalByIdUseCase(id)
                val result = updateGoalStatusUseCase(id, newStatus)

                if (result.isSuccess) {
                    // Log the change
                    if (oldGoal != null) {
                        logGoalChangeUseCase(
                            goalId = id,
                            field = "status",
                            oldValue = oldGoal.status.name,
                            newValue = newStatus.name
                        )

                        // Auto-complete milestones when goal is completed
                        if (newStatus == GoalStatus.COMPLETED && oldGoal.milestones.isNotEmpty()) {
                            oldGoal.milestones.filter { !it.isCompleted }.forEach { milestone ->
                                toggleMilestoneCompletionUseCase(milestone.id, true)
                            }
                            val updatedGoal = getGoalByIdUseCase(id)
                            updatedGoal?.let { recalculateAndUpdateProgress(it) }
                        }

                        // Uncheck milestones when reverting from completed
                        if (oldGoal.status == GoalStatus.COMPLETED && newStatus != GoalStatus.COMPLETED && oldGoal.milestones.isNotEmpty()) {
                            oldGoal.milestones.filter { it.isCompleted }.forEach { milestone ->
                                toggleMilestoneCompletionUseCase(milestone.id, false)
                            }
                            val updatedGoal = getGoalByIdUseCase(id)
                            updatedGoal?.let { recalculateAndUpdateProgress(it) }
                        }
                    }
                    // Sync smart reminders based on new status
                    if (newStatus == GoalStatus.COMPLETED) {
                        gamificationRepository.awardXp(XpRewards.GOAL_COMPLETED.toLong())
                        Analytics.goalCompleted(id, oldGoal?.category?.name ?: "", 0)
                        smartReminderManager.cleanupRemindersForCompletedGoal(id)
                    } else if (oldGoal?.status == GoalStatus.COMPLETED) {
                        val refreshed = getGoalByIdUseCase(id)
                        refreshed?.let { smartReminderManager.reactivateRemindersForGoal(it) }
                    }

                    _error.value = null
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update status"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun updateGoalNotes(id: String, notes: String) {
        viewModelScope.launch {
            try {
                val oldGoal = getGoalByIdUseCase(id)
                val result = updateGoalNotesUseCase(id, notes)

                if (result.isSuccess) {
                    // Log the change
                    if (oldGoal != null) {
                        logGoalChangeUseCase(
                            goalId = id,
                            field = "notes",
                            oldValue = oldGoal.notes,
                            newValue = notes
                        )
                    }
                    _error.value = null
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update notes"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update notes: ${e.message}"
            }
        }
    }

    // Milestone Management
    fun addMilestone(goalId: String, milestoneTitle: String, dueDate: LocalDate? = null) {
        viewModelScope.launch {
            try {
                val newMilestone = createNewMilestone(milestoneTitle, dueDate)
                val result = addMilestoneUseCase(goalId, newMilestone)

                if (result.isSuccess) {
                    logGoalChangeUseCase(
                        goalId = goalId,
                        field = "milestone_added",
                        oldValue = null,
                        newValue = milestoneTitle
                    )

                    // Recalculate progress with new milestone
                    val updatedGoal = getGoalByIdUseCase(goalId)
                    updatedGoal?.let {
                        recalculateAndUpdateProgress(it)
                        smartReminderManager.syncRemindersForGoal(it)
                    }

                    _error.value = null
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to add milestone"
                }
            } catch (e: Exception) {
                _error.value = "Failed to add milestone: ${e.message}"
            }
        }
    }

    fun toggleMilestoneCompletion(goalId: String, milestoneId: String) {
        viewModelScope.launch {
            try {
                val goal = getGoalByIdUseCase(goalId) ?: return@launch
                val milestone = goal.milestones.find { it.id == milestoneId } ?: return@launch

                val willBeCompleted = !milestone.isCompleted
                val result = toggleMilestoneCompletionUseCase(milestoneId, willBeCompleted)

                if (result.isSuccess) {
                    if (willBeCompleted) {
                        gamificationRepository.awardXp(XpRewards.MILESTONE_COMPLETED.toLong())
                        Analytics.milestoneCompleted(goalId, milestoneId)
                    }
                    logGoalChangeUseCase(
                        goalId = goalId,
                        field = "milestone_completed",
                        oldValue = milestone.isCompleted.toString(),
                        newValue = willBeCompleted.toString()
                    )

                    val updatedGoal = getGoalByIdUseCase(goalId)
                    updatedGoal?.let { recalculateAndUpdateProgress(it) }

                    if (willBeCompleted && goal.status == GoalStatus.NOT_STARTED) {
                        updateGoalStatusUseCase(goalId, GoalStatus.IN_PROGRESS)
                        logGoalChangeUseCase(
                            goalId = goalId,
                            field = "status",
                            oldValue = GoalStatus.NOT_STARTED.name,
                            newValue = GoalStatus.IN_PROGRESS.name
                        )
                    }

                    val refreshedGoal = getGoalByIdUseCase(goalId)
                    if (refreshedGoal != null &&
                        refreshedGoal.milestones.isNotEmpty() &&
                        refreshedGoal.milestones.all { it.isCompleted } &&
                        refreshedGoal.status != GoalStatus.COMPLETED) {
                        _promptCompleteGoal.value = goalId
                    }

                    _error.value = null
                } else {
                    _error.value =
                        result.exceptionOrNull()?.message ?: "Failed to toggle milestone"
                }
            } catch (e: Exception) {
                _error.value = "Failed to toggle milestone: ${e.message}"
            }
        }
    }

    /**
     * Recalculates goal progress based on completed milestones
     * Progress = (completedMilestones / totalMilestones) * 100
     */
    private suspend fun recalculateAndUpdateProgress(goal: Goal) {
        if (goal.milestones.isEmpty()) return

        val completedCount = goal.milestones.count { it.isCompleted }
        val totalCount = goal.milestones.size
        val newProgress = ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt()

        if (goal.progress?.toInt() != newProgress) {
            updateGoalProgressUseCase(goal.id, newProgress)
            logGoalChangeUseCase(
                goalId = goal.id,
                field = "progress",
                oldValue = goal.progress?.toString() ?: "0",
                newValue = newProgress.toString()
            )
        }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _analytics.value = getGoalAnalyticsUseCase()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load analytics: ${e.message}"
                _analytics.value = null
            }
        }
    }

    fun loadGoalHistory(goalId: String) {
        viewModelScope.launch {
            try {
                _goalHistory.value = getGoalHistoryUseCase(goalId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load goal history: ${e.message}"
            }
        }
    }

    // Utility Methods
    fun getGoalById(id: String): Goal? {
        return goals.value.find { it.id == id }
    }


    // AI generation methods: see GoalViewModelAiExtensions.kt

}