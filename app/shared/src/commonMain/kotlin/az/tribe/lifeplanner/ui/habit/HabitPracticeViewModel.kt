package az.tribe.lifeplanner.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.domain.service.targetSeconds
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
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

    data class State(
        val habit: Habit? = null,
        val mode: HabitTrackMode = HabitTrackMode.SINGLE,
        /** Countdown target; 0 for count habits. */
        val totalSeconds: Int = 0,
        val remainingSeconds: Int = 0,
        val isRunning: Boolean = false,
        /** Reps done today for a count habit, including any tapped before this session. */
        val reps: Int = 0,
        val targetReps: Int = 0,
        val done: Boolean = false,
        val loading: Boolean = true,
    ) {
        /** 0f..1f for the ring. Duration counts down, count counts up; both fill the same way. */
        val progress: Float
            get() = when {
                mode == HabitTrackMode.DURATION && totalSeconds > 0 ->
                    ((totalSeconds - remainingSeconds).toFloat() / totalSeconds).coerceIn(0f, 1f)
                targetReps > 0 -> (reps.toFloat() / targetReps).coerceIn(0f, 1f)
                else -> 0f
            }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    /** XP awarded on completion, one-shot, so the screen can celebrate it. */
    private val _xpEvent = MutableSharedFlow<Int>()
    val xpEvent: SharedFlow<Int> = _xpEvent.asSharedFlow()

    private var ticker: Job? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val habit = runCatching { habitRepository.getHabitById(habitId) }
                .onFailure { Logger.w(TAG) { "load failed: ${it.message}" } }
                .getOrNull()
            if (habit == null) {
                _state.value = State(loading = false)
                return@launch
            }
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = runCatching { habitRepository.getCheckInByHabitAndDate(habitId, today) }.getOrNull()
            val seconds = habit.targetSeconds ?: 0
            _state.value = State(
                habit = habit,
                mode = habit.trackMode,
                totalSeconds = seconds,
                remainingSeconds = seconds,
                // Pick up reps already tapped today so practising twice does not restart the count.
                reps = existing?.count ?: 0,
                targetReps = habit.targetCount.coerceAtLeast(1),
                done = existing?.completed == true,
                loading = false,
            )
        }
    }

    /** Start or resume the countdown. No-op for count habits, which have nothing to run. */
    fun start() {
        val s = _state.value
        if (s.mode != HabitTrackMode.DURATION || s.isRunning || s.remainingSeconds <= 0) return
        _state.value = s.copy(isRunning = true)
        ticker = viewModelScope.launch {
            while (isActive && _state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1000)
                if (!_state.value.isRunning) break
                val next = (_state.value.remainingSeconds - 1).coerceAtLeast(0)
                _state.value = _state.value.copy(remainingSeconds = next)
                if (next == 0) complete()
            }
        }
    }

    fun pause() {
        ticker?.cancel()
        ticker = null
        _state.value = _state.value.copy(isRunning = false)
    }

    /** Back to a full timer without touching today's check-in. */
    fun reset() {
        pause()
        _state.value = _state.value.copy(remainingSeconds = _state.value.totalSeconds)
    }

    /** One rep. Reaching the target completes the habit. */
    fun addRep() {
        val s = _state.value
        if (s.mode != HabitTrackMode.COUNT || s.done) return
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            runCatching { habitRepository.incrementCount(habitId, today) }
                .onSuccess { checkIn ->
                    _state.value = _state.value.copy(reps = checkIn.count, done = checkIn.completed)
                    if (checkIn.completed) award(alreadyCheckedIn = true)
                }
                .onFailure { Logger.w(TAG) { "increment failed: ${it.message}" } }
        }
    }

    /** Finish now, however far in. Used by the timer hitting zero and by "mark done". */
    fun complete() {
        val s = _state.value
        if (s.done) return
        pause()
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            runCatching { habitRepository.checkIn(habitId, today, notes = PRACTICE_NOTE) }
                .onFailure { Logger.w(TAG) { "check-in failed: ${it.message}" } }
            _state.value = _state.value.copy(done = true, remainingSeconds = 0)
            award(alreadyCheckedIn = true)
        }
    }

    private suspend fun award(alreadyCheckedIn: Boolean) {
        if (!alreadyCheckedIn) return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        runCatching { awardHabitCompletion(habitId, today) }
            .onSuccess { _xpEvent.emit(it) }
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
