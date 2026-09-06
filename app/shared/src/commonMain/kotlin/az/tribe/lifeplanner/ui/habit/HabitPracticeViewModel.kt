package az.tribe.lifeplanner.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.domain.service.targetSeconds
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.ui.UiText
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.xp_earned
import kotlin.time.Clock

/**
 * Backs the practice ground: somewhere to actually *do* a habit rather than just assert you did.
 *
 * A DURATION habit ("Plank 30 sec") counts down its target. A COUNT habit ("50 pushups") counts
 * reps up. Either way, reaching the target checks the habit in and awards XP once, so finishing
 * here needs no second trip to the habit list.
 */
class HabitPracticeViewModel(
    private val habitId: String,
    private val habitRepository: HabitRepository,
    private val awardHabitCompletion: AwardHabitCompletionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HabitPracticeState())
    val state: StateFlow<HabitPracticeState> = _state.asStateFlow()

    private val _events = Channel<HabitPracticeEvent>()
    val events = _events.receiveAsFlow()

    private var ticker: Job? = null

    init {
        load()
    }

    fun onAction(action: HabitPracticeAction) {
        when (action) {
            HabitPracticeAction.OnBackClick -> viewModelScope.launch { _events.send(HabitPracticeEvent.NavigateBack) }
            HabitPracticeAction.OnStartClick -> start()
            HabitPracticeAction.OnPauseClick -> pause()
            HabitPracticeAction.OnResetClick -> reset()
            HabitPracticeAction.OnAddRepClick -> addRep()
            HabitPracticeAction.OnCompleteClick -> complete()
        }
    }

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun load() {
        viewModelScope.launch {
            val habit = runCatching { habitRepository.getHabitById(habitId) }
                .onFailure { Logger.w(TAG) { "load failed: ${it.message}" } }
                .getOrNull()
            if (habit == null) {
                _state.update { HabitPracticeState(isLoading = false) }
                return@launch
            }
            val existing = runCatching { habitRepository.getCheckInByHabitAndDate(habitId, today()) }.getOrNull()
            val seconds = habit.targetSeconds ?: 0
            _state.update {
                HabitPracticeState(
                    habit = habit,
                    mode = habit.trackMode,
                    totalSeconds = seconds,
                    remainingSeconds = seconds,
                    // Pick up reps already tapped today so practising twice does not restart the count.
                    reps = existing?.count ?: 0,
                    targetReps = habit.targetCount.coerceAtLeast(1),
                    done = existing?.completed == true,
                    isLoading = false,
                )
            }
        }
    }

    /** Start or resume the countdown. No-op for count habits, which have nothing to run. */
    private fun start() {
        val s = _state.value
        if (s.mode != HabitTrackMode.DURATION || s.isRunning || s.remainingSeconds <= 0) return
        _state.update { it.copy(isRunning = true) }
        ticker = viewModelScope.launch {
            while (isActive && _state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1000)
                if (!_state.value.isRunning) break
                val next = (_state.value.remainingSeconds - 1).coerceAtLeast(0)
                _state.update { it.copy(remainingSeconds = next) }
                if (next == 0) complete()
            }
        }
    }

    private fun pause() {
        ticker?.cancel()
        ticker = null
        _state.update { it.copy(isRunning = false) }
    }

    /** Back to a full timer without touching today's check-in. */
    private fun reset() {
        pause()
        _state.update { it.copy(remainingSeconds = it.totalSeconds) }
    }

    /** One rep. Reaching the target completes the habit. */
    private fun addRep() {
        val s = _state.value
        if (s.mode != HabitTrackMode.COUNT || s.done) return
        viewModelScope.launch {
            runCatching { habitRepository.incrementCount(habitId, today()) }
                .onSuccess { checkIn ->
                    _state.update { it.copy(reps = checkIn.count, done = checkIn.completed) }
                    if (checkIn.completed) award()
                }
                .onFailure { Logger.w(TAG) { "increment failed: ${it.message}" } }
        }
    }

    /** Finish now, however far in. Used by the timer hitting zero and by "mark done". */
    private fun complete() {
        if (_state.value.done) return
        pause()
        viewModelScope.launch {
            runCatching { habitRepository.checkIn(habitId, today(), notes = PRACTICE_NOTE) }
                .onFailure { Logger.w(TAG) { "check-in failed: ${it.message}" } }
            _state.update { it.copy(done = true, remainingSeconds = 0) }
            award()
        }
    }

    private suspend fun award() {
        runCatching { awardHabitCompletion(habitId, today()) }
            .onSuccess { xp ->
                if (xp > 0) {
                    _events.send(HabitPracticeEvent.ShowSnackbar(UiText.StringResource(Res.string.xp_earned, arrayOf(xp))))
                }
            }
            .onFailure { Logger.w(TAG) { "award failed: ${it.message}" } }
    }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }

    private companion object {
        const val PRACTICE_NOTE = "Completed in practice"
        const val TAG = "HabitPractice"
    }
}
