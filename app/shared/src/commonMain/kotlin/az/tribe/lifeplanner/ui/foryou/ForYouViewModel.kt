package az.tribe.lifeplanner.ui.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.FeedItem
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Backs the redesigned **For You** home feed. Builds a ranked feed from the app's existing engines
 * (knowledge, causal insights, becoming, possibilities, momentum) and lets the user act inline.
 */
class ForYouViewModel(
    private val feedBuilder: HomeFeedBuilder,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    private val _feed = MutableStateFlow<List<FeedItem>>(emptyList())
    val feed: StateFlow<List<FeedItem>> = _feed.asStateFlow()

    private val _progress = MutableStateFlow<UserProgress?>(null)
    val progress: StateFlow<UserProgress?> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                _progress.value = gamificationRepository.getUserProgress().first()
            }.onFailure { Logger.w("ForYouViewModel") { "Progress load failed: ${it.message}" } }

            runCatching { feedBuilder.build() }
                .onSuccess { _feed.value = it }
                .onFailure { Logger.w("ForYouViewModel") { "Feed build failed: ${it.message}" } }
            _isLoading.value = false
        }
    }

    /** Inline check-in from a "Do next" card. Rebuilds the feed so it reflects the new state. */
    fun checkInHabit(habitId: String) {
        viewModelScope.launch {
            runCatching {
                checkInHabitUseCase(habitId)
                gamificationRepository.awardXp(az.tribe.lifeplanner.domain.model.XpRewards.HABIT_CHECK_IN.toLong())
            }.onFailure { Logger.w("ForYouViewModel") { "Check-in failed: ${it.message}" } }
            refresh()
        }
    }
}
