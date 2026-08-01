package az.tribe.lifeplanner.ui.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.FeedItem
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.LifeBalanceRepository
import az.tribe.lifeplanner.domain.model.LifeBalanceReport
import az.tribe.lifeplanner.ui.today.PlanItem
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.health.GetHealthHabitProgressUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Backs the redesigned **For You** home feed. Builds a ranked feed from the app's existing engines
 * (knowledge, causal insights, becoming, possibilities, momentum) and lets the user act inline.
 */
class ForYouViewModel(
    private val feedBuilder: HomeFeedBuilder,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val gamificationRepository: GamificationRepository,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val getHealthHabitProgress: GetHealthHabitProgressUseCase,
    private val lifeBalanceRepository: LifeBalanceRepository,
) : ViewModel() {

    private val _feed = MutableStateFlow<List<FeedItem>>(emptyList())
    val feed: StateFlow<List<FeedItem>> = _feed.asStateFlow()

    /**
     * Just-checked-in feedback per habit, so the card can confirm the tap *in place* and hold there
     * for a beat before the feed re-ranks and the card moves. Count habits accumulate here across
     * taps ("2 of 3") and only re-rank once complete, so a tap never yanks the card away mid-progress.
     */
    data class CheckinPulse(val count: Int, val target: Int, val done: Boolean)
    private val _checkinPulse = MutableStateFlow<Map<String, CheckinPulse>>(emptyMap())
    val checkinPulse: StateFlow<Map<String, CheckinPulse>> = _checkinPulse.asStateFlow()
    private val holdJobs = mutableMapOf<String, Job>()
    private val holdMillis = 3000L

    /**
     * Today's plan, the planner on the home screen: incomplete milestones due today or overdue across
     * active goals, soonest first. A compact strip so the concrete things scheduled for today live on
     * the front door instead of being buried in the Journal hub's Planner sub-tab.
     */
    val todayPlan: StateFlow<List<PlanItem>> =
        goalRepository.observeAllGoals()
            .map { goals ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                goals.asSequence()
                    .filter { it.status != GoalStatus.COMPLETED }
                    .flatMap { g ->
                        g.milestones.asSequence()
                            .filter { !it.isCompleted && (it.dueDate?.let { d -> d <= today } == true) }
                            .map { m -> PlanItem(g.id, m.id, g.title, m.title, m.dueDate!!, m.dueDate!! < today) }
                    }
                    .sortedBy { it.dueDate }
                    .toList()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Habits wired to a health metric, with today's reading against their target. These sit in
     * Today's plan next to the milestones: they are things due today that the user cannot tick by
     * hand, so showing the number is the only way the row means anything.
     */
    private val _healthHabits = MutableStateFlow<List<GetHealthHabitProgressUseCase.Progress>>(emptyList())
    val healthHabits: StateFlow<List<GetHealthHabitProgressUseCase.Progress>> = _healthHabits.asStateFlow()

    /**
     * Life balance, for the strip on the feed. Reads the last saved report rather than
     * recalculating: the feed should not pay for a full recompute on every open, and the score
     * only moves as goals and habits do.
     */
    private val _balance = MutableStateFlow<LifeBalanceReport?>(null)
    val balance: StateFlow<LifeBalanceReport?> = _balance.asStateFlow()

    private val _progress = MutableStateFlow<UserProgress?>(null)
    val progress: StateFlow<UserProgress?> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** XP a just-finished action earned, one-shot, so the feed can confirm the reward in a snackbar. */
    private val _xpEvent = MutableSharedFlow<Int>()
    val xpEvent: SharedFlow<Int> = _xpEvent.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                _progress.value = gamificationRepository.getUserProgress().first()
            }.onFailure { Logger.w("ForYouViewModel") { "Progress load failed: ${it.message}" } }

            runCatching { lifeBalanceRepository.getLatestReport() ?: lifeBalanceRepository.calculateCurrentBalance() }
                .onSuccess { _balance.value = it }
                .onFailure { Logger.w("ForYouViewModel") { "Balance load failed: ${it.message}" } }

            runCatching { getHealthHabitProgress() }
                .onSuccess { _healthHabits.value = it }
                .onFailure { Logger.w("ForYouViewModel") { "Health habit progress failed: ${it.message}" } }

            runCatching { feedBuilder.build() }
                .onSuccess { _feed.value = it }
                .onFailure { Logger.w("ForYouViewModel") { "Feed build failed: ${it.message}" } }
            _isLoading.value = false
        }
    }

    /** Planner-style: tick a plan item (milestone) done right on the Today feed, no navigation. */
    fun completePlanItem(milestoneId: String) {
        viewModelScope.launch {
            runCatching {
                goalRepository.toggleMilestoneCompletion(milestoneId, true)
                gamificationRepository.awardXp(XpRewards.MILESTONE_COMPLETED.toLong())
                _xpEvent.emit(XpRewards.MILESTONE_COMPLETED)
            }.onFailure { Logger.w("ForYouViewModel") { "Complete plan item failed: ${it.message}" } }
        }
    }

    /**
     * Inline check-in from a "Do next" card. Each tap adds one toward the habit's target (the data
     * layer counts up), so a 3-a-day habit takes three taps rather than completing at once. The card
     * confirms in place via [checkinPulse] and only after the hold elapses (and only once the habit
     * is actually complete) does the feed re-rank, so completion feels registered instead of snatched.
     */
    fun checkInHabit(habitId: String) {
        viewModelScope.launch {
            val target = runCatching { habitRepository.getHabitById(habitId)?.targetCount ?: 1 }.getOrDefault(1)
            val checkIn = runCatching {
                val c = checkInHabitUseCase(habitId)
                gamificationRepository.awardXp(XpRewards.HABIT_CHECK_IN.toLong())
                _xpEvent.emit(XpRewards.HABIT_CHECK_IN)
                c
            }.onFailure { Logger.w("ForYouViewModel") { "Check-in failed: ${it.message}" } }.getOrNull()

            val count = checkIn?.count ?: 1
            val done = checkIn?.completed ?: (count >= target)
            _checkinPulse.value = _checkinPulse.value + (habitId to CheckinPulse(count, target, done))

            // Reset the hold on every tap: a burst of count taps keeps the card put and progressing.
            holdJobs.remove(habitId)?.cancel()
            holdJobs[habitId] = viewModelScope.launch {
                delay(holdMillis)
                _checkinPulse.value = _checkinPulse.value - habitId
                holdJobs.remove(habitId)
                if (done) refresh() // only re-rank once it's fully done and the confirmation has been seen
            }
        }
    }
}
