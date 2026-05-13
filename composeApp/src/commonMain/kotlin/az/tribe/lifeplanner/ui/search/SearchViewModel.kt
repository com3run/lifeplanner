package az.tribe.lifeplanner.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.repository.BuiltinCoachStore
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import az.tribe.lifeplanner.ui.habit.HabitWithStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// ── Filter enum ───────────────────────────────────────────────────────────────

enum class SearchFilter(val label: String, val emoji: String) {
    GOALS("Goals", "🎯"),
    HABITS("Habits", "🔄"),
    JOURNAL("Journal", "📖"),
    COACHES("Coaches", "🤖"),
}

// ── Result models ─────────────────────────────────────────────────────────────

data class MilestoneResult(val goal: Goal, val milestone: Milestone)

data class SettingsItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val keywords: List<String>,
    val destination: SearchDestination,
)

enum class SearchDestination {
    GOALS, HABITS, JOURNAL, AI_COACH, REMINDERS, LIFE_BALANCE,
    ACHIEVEMENTS, HEALTH, FOCUS, RETROSPECTIVE, BACKUP,
}

data class SearchResults(
    val goals: List<Goal> = emptyList(),
    val milestones: List<MilestoneResult> = emptyList(),
    val habits: List<HabitWithStatus> = emptyList(),
    val journals: List<JournalEntry> = emptyList(),
    val coaches: List<CoachPersona> = emptyList(),
    val settings: List<SettingsItem> = emptyList(),
) {
    val isEmpty get() = goals.isEmpty() && milestones.isEmpty() && habits.isEmpty()
            && journals.isEmpty() && coaches.isEmpty() && settings.isEmpty()
}

// ── Static settings catalogue ─────────────────────────────────────────────────

// Goals, Habits, Journal, Coaches are now filter chips — kept here only as external nav shortcuts
private val ALL_SETTINGS = listOf(
    SettingsItem("AI Coach", "Chat with your personal coach", "🤖",
        listOf("coach", "ai", "chat", "assistant", "advice", "luna"), SearchDestination.AI_COACH),
    SettingsItem("Reminders", "Notification schedule", "🔔",
        listOf("reminder", "notification", "alert", "notify", "remind"), SearchDestination.REMINDERS),
    SettingsItem("Life Balance", "Track all areas of your life", "⚖️",
        listOf("balance", "life", "wellbeing", "area", "wheel", "pillar"), SearchDestination.LIFE_BALANCE),
    SettingsItem("Achievements", "Badges & rewards earned", "🏆",
        listOf("achievement", "badge", "reward", "trophy", "earned", "unlock"), SearchDestination.ACHIEVEMENTS),
    SettingsItem("Health", "Steps, sleep & fitness data", "❤️",
        listOf("health", "steps", "sleep", "fitness", "activity", "exercise", "walk"), SearchDestination.HEALTH),
    SettingsItem("Focus Timer", "Deep work sessions", "⏱️",
        listOf("focus", "timer", "pomodoro", "work", "session", "deep", "concentrate"), SearchDestination.FOCUS),
    SettingsItem("Retrospective", "Weekly & monthly review", "🔍",
        listOf("retrospective", "review", "reflect", "weekly", "monthly", "recap"), SearchDestination.RETROSPECTIVE),
    SettingsItem("Backup", "Export & restore your data", "💾",
        listOf("backup", "export", "restore", "data", "import", "save"), SearchDestination.BACKUP),
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val journalRepository: JournalRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilters = MutableStateFlow<Set<SearchFilter>>(emptySet())
    val selectedFilters: StateFlow<Set<SearchFilter>> = _selectedFilters.asStateFlow()

    fun onQueryChange(value: String) { _query.value = value }

    fun setFilter(filter: SearchFilter?) {
        _selectedFilters.value = if (filter != null) setOf(filter) else emptySet()
    }

    fun toggleFilter(filter: SearchFilter) {
        val current = _selectedFilters.value
        val new = if (filter in current) current - filter else current + filter
        // Selecting all individual filters is equivalent to "All"
        _selectedFilters.value = if (new.size == SearchFilter.entries.size) emptySet() else new
    }

    val results: StateFlow<SearchResults> = combine(_query.debounce(250), _selectedFilters) { q, filters ->
        q to filters
    }
        .flatMapLatest { (q, filters) ->
            // Nothing to show: no query typed and no filter chip selected
            if (q.isBlank() && filters.isEmpty()) return@flatMapLatest flowOf(SearchResults())

            val lower = q.lowercase().trim()

            combine(
                goalRepository.observeAllGoals(),
                habitRepository.observeHabitsWithTodayStatus().map { pairs ->
                    pairs.map { (habit, done) -> HabitWithStatus(habit = habit, isCompletedToday = done) }
                },
                journalRepository.observeAllEntries(),
            ) { goals, habits, journals ->
                SearchResults(
                    goals = when {
                        filters.isNotEmpty() && SearchFilter.GOALS !in filters -> emptyList()
                        q.isBlank() -> goals
                        else -> goals.filter { it.matchesQuery(lower) }
                    },
                    milestones = when {
                        // Don't show all milestones in browse mode — too noisy
                        filters.isNotEmpty() && SearchFilter.GOALS !in filters -> emptyList()
                        q.isBlank() -> emptyList()
                        else -> goals.flatMap { goal ->
                            goal.milestones
                                .filter { it.title.lowercase().contains(lower) }
                                .map { MilestoneResult(goal, it) }
                        }
                    },
                    habits = when {
                        filters.isNotEmpty() && SearchFilter.HABITS !in filters -> emptyList()
                        q.isBlank() -> habits
                        else -> habits.filter {
                            it.habit.title.lowercase().contains(lower) ||
                                    it.habit.description.lowercase().contains(lower)
                        }
                    },
                    journals = when {
                        filters.isNotEmpty() && SearchFilter.JOURNAL !in filters -> emptyList()
                        q.isBlank() -> journals.take(20)
                        else -> journals.filter {
                            it.title.lowercase().contains(lower) ||
                                    it.content.lowercase().contains(lower) ||
                                    it.tags.any { tag -> tag.lowercase().contains(lower) }
                        }.take(10)
                    },
                    coaches = when {
                        filters.isNotEmpty() && SearchFilter.COACHES !in filters -> emptyList()
                        q.isBlank() -> BuiltinCoachStore.getAll()
                        else -> BuiltinCoachStore.getAll().filter {
                            it.name.lowercase().contains(lower) ||
                                    it.title.lowercase().contains(lower)
                        }
                    },
                    // Hide navigation shortcuts when a content filter is active or browsing
                    settings = when {
                        filters.isNotEmpty() -> emptyList()
                        q.isBlank() -> emptyList()
                        else -> ALL_SETTINGS.filter { item ->
                            item.title.lowercase().contains(lower) ||
                                    item.keywords.any { kw -> kw.contains(lower) || lower.contains(kw) }
                        }
                    },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())
}

private fun Goal.matchesQuery(lower: String): Boolean =
    title.lowercase().contains(lower) ||
            description.lowercase().contains(lower) ||
            category.name.lowercase().contains(lower)
