package az.tribe.lifeplanner.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.ChatSession
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.model.Story
import az.tribe.lifeplanner.domain.model.GoalDependency
import az.tribe.lifeplanner.domain.repository.ChatRepository
import az.tribe.lifeplanner.domain.repository.FocusRepository
import az.tribe.lifeplanner.domain.repository.GoalDependencyRepository
import az.tribe.lifeplanner.domain.repository.RetrospectiveRepository
import az.tribe.lifeplanner.domain.repository.StoryRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val storyRepository: StoryRepository,
    private val focusRepository: FocusRepository,
    private val chatRepository: ChatRepository,
    private val retrospectiveRepository: RetrospectiveRepository,
    private val goalDependencyRepository: GoalDependencyRepository,
) : ViewModel() {

    private val _remoteStories = MutableStateFlow<List<Story>>(emptyList())
    val remoteStories: StateFlow<List<Story>> = _remoteStories.asStateFlow()

    private val _milestoneFocusMinutes = MutableStateFlow<Map<String, Int>>(emptyMap())
    val milestoneFocusMinutes: StateFlow<Map<String, Int>> = _milestoneFocusMinutes.asStateFlow()

    private val _recentSession = MutableStateFlow<ChatSession?>(null)
    val recentSession: StateFlow<ChatSession?> = _recentSession.asStateFlow()

    private val _recentCoach = MutableStateFlow<CoachPersona?>(null)
    val recentCoach: StateFlow<CoachPersona?> = _recentCoach.asStateFlow()

    val weeklySnapshots: StateFlow<List<DaySnapshot>> = retrospectiveRepository.observeWeeklySnapshots()
        .catch { e -> Logger.e("HomeViewModel") { "Weekly snapshots error: ${e.message}" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalDependencies: StateFlow<List<GoalDependency>> = goalDependencyRepository.getAllDependencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadRemoteStories()
        loadRecentChatSession()
    }

    fun loadRemoteStories() {
        viewModelScope.launch {
            try {
                _remoteStories.value = storyRepository.getActiveStories()
            } catch (e: Exception) {
                Logger.e("HomeViewModel") { "Failed to load remote stories: ${e.message}" }
            }
        }
    }

    fun loadMilestoneFocusMinutes(milestones: List<Pair<Goal, Milestone>>) {
        viewModelScope.launch {
            try {
                _milestoneFocusMinutes.value = milestones.associate { (_, milestone) ->
                    val sessions = focusRepository.getSessionsByMilestoneId(milestone.id)
                    milestone.id to (sessions.sumOf { it.actualDurationSeconds } / 60)
                }
            } catch (e: Exception) {
                Logger.e("HomeViewModel") { "Failed to load milestone focus minutes: ${e.message}" }
            }
        }
    }

    private fun loadRecentChatSession() {
        viewModelScope.launch {
            try {
                val sessions = chatRepository.getAllSessions()
                val latest = sessions.maxByOrNull { it.lastMessageAt }
                _recentSession.value = latest
                _recentCoach.value = latest?.let { CoachPersona.getById(it.coachId) }
            } catch (e: Exception) {
                Logger.e("HomeViewModel") { "Failed to load recent chat session: ${e.message}" }
            }
        }
    }

}
