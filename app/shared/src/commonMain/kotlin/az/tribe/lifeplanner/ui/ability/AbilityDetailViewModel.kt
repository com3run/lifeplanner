package az.tribe.lifeplanner.ui.ability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.model.Ability
import az.tribe.lifeplanner.domain.model.AbilityGoalLink
import az.tribe.lifeplanner.domain.model.AbilityHabitLink
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class AbilityDetailViewModel(
    private val abilityId: String,
    private val abilityRepository: AbilityRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val aiProxyService: AiProxyService
) : ViewModel() {

    private val _ability = MutableStateFlow<Ability?>(null)
    val ability: StateFlow<Ability?> = _ability.asStateFlow()

    private val _linkedHabits = MutableStateFlow<List<Pair<Habit, AbilityHabitLink>>>(emptyList())
    val linkedHabits: StateFlow<List<Pair<Habit, AbilityHabitLink>>> = _linkedHabits.asStateFlow()

    private val _allHabitsForLinking = MutableStateFlow<List<Habit>>(emptyList())
    val allHabitsForLinking: StateFlow<List<Habit>> = _allHabitsForLinking.asStateFlow()

    private val _linkedGoals = MutableStateFlow<List<Pair<Goal, AbilityGoalLink>>>(emptyList())
    val linkedGoals: StateFlow<List<Pair<Goal, AbilityGoalLink>>> = _linkedGoals.asStateFlow()

    private val _allGoalsForLinking = MutableStateFlow<List<Goal>>(emptyList())
    val allGoalsForLinking: StateFlow<List<Goal>> = _allGoalsForLinking.asStateFlow()

    private val _supervisionInsight = MutableStateFlow("")
    val supervisionInsight: StateFlow<String> = _supervisionInsight.asStateFlow()

    private val _isGeneratingInsight = MutableStateFlow(false)
    val isGeneratingInsight: StateFlow<Boolean> = _isGeneratingInsight.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                _ability.value = abilityRepository.getAbilityById(abilityId)
                refreshLinkedHabits()
                refreshLinkedGoals()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateTitle(newTitle: String) {
        val ab = _ability.value ?: return
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            try {
                val updated = ab.copy(title = newTitle.trim())
                abilityRepository.updateAbility(updated)
                _ability.value = updated
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private suspend fun refreshLinkedHabits() {
        val links = abilityRepository.getLinksForAbility(abilityId)
        val linkedIds = links.map { it.habitId }.toSet()
        val allHabits = habitRepository.observeHabitsWithTodayStatus().first()
            .map { it.first }
        val linked = allHabits.filter { it.id in linkedIds }.mapNotNull { habit ->
            links.firstOrNull { it.habitId == habit.id }?.let { habit to it }
        }
        _linkedHabits.value = linked
        _allHabitsForLinking.value = allHabits.filter { it.id !in linkedIds }
    }

    private suspend fun refreshLinkedGoals() {
        val links = abilityRepository.getGoalLinksForAbility(abilityId)
        val linkedIds = links.map { it.goalId }.toSet()
        val allGoals = goalRepository.getAllGoals()
        val linked = allGoals.filter { it.id in linkedIds }.mapNotNull { goal ->
            links.firstOrNull { it.goalId == goal.id }?.let { goal to it }
        }
        _linkedGoals.value = linked
        _allGoalsForLinking.value = allGoals.filter { it.id !in linkedIds }
    }

    fun linkHabit(habitId: String) {
        viewModelScope.launch {
            abilityRepository.linkHabit(abilityId, habitId)
            refreshLinkedHabits()
        }
    }

    fun unlinkHabit(habitId: String) {
        viewModelScope.launch {
            abilityRepository.unlinkHabit(abilityId, habitId)
            refreshLinkedHabits()
        }
    }

    fun linkGoal(goalId: String) {
        viewModelScope.launch {
            abilityRepository.linkGoal(abilityId, goalId)
            refreshLinkedGoals()
        }
    }

    fun unlinkGoal(goalId: String) {
        viewModelScope.launch {
            abilityRepository.unlinkGoal(abilityId, goalId)
            refreshLinkedGoals()
        }
    }

    fun generateSupervisionInsight() {
        val ab = _ability.value ?: return
        val habits = _linkedHabits.value
        if (habits.isEmpty()) return

        viewModelScope.launch {
            _isGeneratingInsight.value = true
            _supervisionInsight.value = ""

            val habitList = habits.joinToString("\n") { (habit, _) ->
                "- ${habit.title} (${habit.type.displayName}, streak: ${habit.currentStreak})"
            }
            val goalList = _linkedGoals.value.joinToString("\n") { (goal, _) ->
                "- ${goal.title} (${goal.status})"
            }

            val prompt = """
Ability: ${ab.title}, Level ${ab.currentLevel}, ${ab.totalXp} XP
Linked habits:
$habitList
${if (goalList.isNotBlank()) "\nContributing to goals:\n$goalList" else ""}

Give 2-3 sentences of actionable coaching insight to help build this ability faster.
""".trimIndent()

            val messages = listOf(AiProxyService.ChatMessage(role = "user", content = prompt))

            try {
                aiProxyService.chatStream(messages).collect { event ->
                    when (event) {
                        is AiProxyService.StreamEvent.TextChunk -> _supervisionInsight.value += event.text
                        is AiProxyService.StreamEvent.Done -> _isGeneratingInsight.value = false
                        is AiProxyService.StreamEvent.Error -> _isGeneratingInsight.value = false
                    }
                }
            } catch (_: Exception) {
                _isGeneratingInsight.value = false
            }
        }
    }

    fun refreshAbility() {
        viewModelScope.launch {
            _ability.value = abilityRepository.getAbilityById(abilityId)
        }
    }
}
