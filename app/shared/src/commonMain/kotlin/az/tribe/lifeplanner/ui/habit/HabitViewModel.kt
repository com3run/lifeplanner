package az.tribe.lifeplanner.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.mapper.createNewHabit
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.HabitType
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.SmartReminderManager
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.habit.CreateHabitUseCase
import az.tribe.lifeplanner.usecases.habit.DeleteHabitUseCase
import az.tribe.lifeplanner.core.FeatureFlags
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import az.tribe.lifeplanner.usecases.habit.UncheckHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UpdateHabitUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val weeklyCompletions: List<Boolean> = emptyList(),
    val todayCount: Int = 0
)

/**
 * Represents a habit that was just checked in, for showing reflection prompt
 */
data class RecentCheckIn(
    val habit: Habit,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)

class HabitViewModel(
    private val habitRepository: HabitRepository,
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val uncheckHabitUseCase: UncheckHabitUseCase,
    private val smartReminderManager: SmartReminderManager,
    private val awardAbilityXpUseCase: AwardAbilityXpUseCase,
    private val gamificationRepository: GamificationRepository,
    private val settings: Settings
) : ViewModel() {


    // Smart reminder events (one-shot, collected by UI for snackbar)
    private val _reminderEvent = MutableSharedFlow<String>()
    val reminderEvent: SharedFlow<String> = _reminderEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val habits: StateFlow<List<HabitWithStatus>> = habitRepository.observeHabitsWithTodayStatus()
        .map { pairs ->
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today = now.date
            val nowMinutes = now.hour * 60 + now.minute

            // Calculate Monday of this week
            val daysFromMonday = (today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
            val monday = today.minus(daysFromMonday, DateTimeUnit.DAY)

            // Single batched query for all habits — avoids N separate DB calls per reactive update
            val allWeeklyCheckIns = try {
                habitRepository.getAllCheckInsInRange(monday, today)
            } catch (_: Exception) {
                emptyList()
            }
            val weeklyCheckInsByHabitId = allWeeklyCheckIns
                .filter { it.completed }
                .groupBy { it.habitId }

            val todayCheckInByHabitId = allWeeklyCheckIns
                .filter { it.date == today }
                .associateBy { it.habitId }

            pairs.map { (habit, isCompleted) ->
                val completedDates = weeklyCheckInsByHabitId[habit.id]
                    ?.map { it.date }
                    ?.toSet()
                    ?: emptySet()
                val weeklyCompletions = (0..6).map { dayOffset ->
                    val day = monday.plus(dayOffset, DateTimeUnit.DAY)
                    if (day <= today) completedDates.contains(day) else false
                }
                val todayCount = todayCheckInByHabitId[habit.id]?.count ?: 0
                HabitWithStatus(habit, isCompleted, weeklyCompletions, todayCount)
            }.sortedWith(timeAwareHabitComparator(nowMinutes))
        }
        .onEach { _isLoading.value = false }
        .catch { e ->
            _error.value = "Failed to load habits: ${e.message}"
            _isLoading.value = false
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddHabitDialog = MutableStateFlow(false)
    val showAddHabitDialog: StateFlow<Boolean> = _showAddHabitDialog.asStateFlow()

    // Track recently checked-in habit for reflection prompt
    private val _recentCheckIn = MutableStateFlow<RecentCheckIn?>(null)
    val recentCheckIn: StateFlow<RecentCheckIn?> = _recentCheckIn.asStateFlow()

    private var isCreatingHabit = false

    fun createHabit(
        title: String,
        description: String,
        category: GoalCategory,
        frequency: HabitFrequency,
        targetCount: Int = 1,
        linkedGoalId: String? = null,
        reminderTime: String? = null,
        type: HabitType = HabitType.BUILD
    ) {
        if (isCreatingHabit) return

        // Prevent duplicate: skip if a habit with the same title already exists
        if (habits.value.any { it.habit.title.equals(title, ignoreCase = true) }) return

        viewModelScope.launch {
            isCreatingHabit = true
            try {
                val habit = createNewHabit(
                    title = title,
                    description = description,
                    category = category,
                    frequency = frequency,
                    targetCount = targetCount,
                    linkedGoalId = linkedGoalId,
                    reminderTime = reminderTime,
                    type = type
                )
                createHabitUseCase(habit)
                Analytics.habitCreated(frequency.name, linkedGoalId != null)
                val result = smartReminderManager.syncRemindersForHabit(habit)
                if (result.hasChanges) {
                    _reminderEvent.emit("Reminder set for \"${habit.title}\"")
                }
                _showAddHabitDialog.value = false
            } catch (e: Exception) {
                _error.value = "Failed to create habit: ${e.message}"
            } finally {
                isCreatingHabit = false
            }
        }
    }

    fun checkInHabit(habitId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                checkInHabitUseCase(habitId, today, notes)
                gamificationRepository.awardXp(XpRewards.HABIT_CHECK_IN.toLong())
                if (FeatureFlags.ABILITIES_ENABLED) {
                    awardAbilityXpUseCase(habitId)
                }

                // Perfect day bonus: all habits completed after this check-in (once per day)
                val allCheckIns = habitRepository.getAllCheckInsInRange(today, today)
                val completedTodayIds = allCheckIns.filter { it.completed }.map { it.habitId }.toSet()
                val allHabitIds = habits.value.map { it.habit.id }.toSet()
                val todayStr = today.toString()
                if (allHabitIds.isNotEmpty() &&
                    allHabitIds == completedTodayIds &&
                    settings.getStringOrNull(PREF_PERFECT_DAY_DATE) != todayStr
                ) {
                    gamificationRepository.awardXp(XpRewards.PERFECT_DAY_BONUS.toLong())
                    settings.putString(PREF_PERFECT_DAY_DATE, todayStr)
                }

                // Track check-in
                val streak = habitRepository.getHabitById(habitId)?.currentStreak ?: 0
                Analytics.habitCheckedIn(habitId, streak.toInt())

                // Read updated habit for reflection prompt
                val updatedHabit = habitRepository.getHabitById(habitId)

                // Emit recent check-in for reflection prompt
                updatedHabit?.let {
                    _recentCheckIn.value = RecentCheckIn(it)
                }
            } catch (e: Exception) {
                _error.value = "Failed to check in: ${e.message}"
            }
        }
    }

    fun incrementCheckIn(habitId: String) {
        viewModelScope.launch {
            try {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val checkIn = habitRepository.incrementCount(habitId, today)
                if (checkIn.completed) {
                    gamificationRepository.awardXp(XpRewards.HABIT_CHECK_IN.toLong())
                    if (FeatureFlags.ABILITIES_ENABLED) {
                        awardAbilityXpUseCase(habitId)
                    }
                    val allCheckIns = habitRepository.getAllCheckInsInRange(today, today)
                    val completedTodayIds = allCheckIns.filter { it.completed }.map { it.habitId }.toSet()
                    val allHabitIds = habits.value.map { it.habit.id }.toSet()
                    val todayStr = today.toString()
                    if (allHabitIds.isNotEmpty() &&
                        allHabitIds == completedTodayIds &&
                        settings.getStringOrNull(PREF_PERFECT_DAY_DATE) != todayStr
                    ) {
                        gamificationRepository.awardXp(XpRewards.PERFECT_DAY_BONUS.toLong())
                        settings.putString(PREF_PERFECT_DAY_DATE, todayStr)
                    }
                    habitRepository.getHabitById(habitId)?.let { _recentCheckIn.value = RecentCheckIn(it) }
                }
            } catch (e: Exception) {
                _error.value = "Failed to increment: ${e.message}"
            }
        }
    }

    fun clearRecentCheckIn() {
        _recentCheckIn.value = null
    }

    fun uncheckInHabit(habitId: String) {
        viewModelScope.launch {
            try {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                uncheckHabitUseCase(habitId, today)
            } catch (e: Exception) {
                _error.value = "Failed to uncheck: ${e.message}"
            }
        }
    }

    fun toggleCheckIn(habitId: String) {
        val habitWithStatus = habits.value.find { it.habit.id == habitId }
        if (habitWithStatus != null) {
            if (habitWithStatus.isCompletedToday) {
                uncheckInHabit(habitId)
            } else {
                checkInHabit(habitId)
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                smartReminderManager.cleanupRemindersForDeletedHabit(habitId)
                deleteHabitUseCase(habitId)
            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                updateHabitUseCase(habit)
                smartReminderManager.syncRemindersForHabit(habit)
            } catch (e: Exception) {
                _error.value = "Failed to update habit: ${e.message}"
            }
        }
    }

    fun showAddHabitDialog() {
        _showAddHabitDialog.value = true
    }

    fun hideAddHabitDialog() {
        _showAddHabitDialog.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun getTodayCompletedCount(): Int {
        return habits.value.count { it.isCompletedToday }
    }

    fun getTotalHabitsCount(): Int {
        return habits.value.size
    }

    fun getStreakLeader(): Habit? {
        return habits.value.maxByOrNull { it.habit.currentStreak }?.habit
    }

    companion object {
        private const val PREF_PERFECT_DAY_DATE = "gamification_perfect_day_date"
    }
}

/**
 * Sorts habits so that upcoming scheduled ones appear first, then unscheduled,
 * then past-scheduled (time already passed today), then completed.
 *
 * This way, at night, morning habits sink to the bottom and evening habits
 * float to the top — showing what's actually still relevant right now.
 */
private fun timeAwareHabitComparator(nowMinutes: Int): Comparator<HabitWithStatus> {
    return Comparator { a, b ->
        // Completed always go to the bottom
        val completedCmp = a.isCompletedToday.compareTo(b.isCompletedToday)
        if (completedCmp != 0) return@Comparator completedCmp

        // Both completed — keep stable order
        if (a.isCompletedToday) return@Comparator 0

        // Both incomplete — assign a time group
        val aGroup = timeGroup(a.habit.reminderTime, nowMinutes)
        val bGroup = timeGroup(b.habit.reminderTime, nowMinutes)

        val groupCmp = aGroup.compareTo(bGroup)
        if (groupCmp != 0) return@Comparator groupCmp

        // Within "upcoming" group: sort by reminder time ascending (soonest first)
        if (aGroup == 0) {
            val aMins = parseReminderMinutes(a.habit.reminderTime)
            val bMins = parseReminderMinutes(b.habit.reminderTime)
            if (aMins != null && bMins != null) return@Comparator aMins.compareTo(bMins)
        }

        // Within other groups: sort by streak descending
        b.habit.currentStreak.compareTo(a.habit.currentStreak)
    }
}

/** 0 = upcoming, 1 = no scheduled time, 2 = time already passed today */
private fun timeGroup(reminderTime: String?, nowMinutes: Int): Int {
    val mins = parseReminderMinutes(reminderTime) ?: return 1
    return if (mins > nowMinutes) 0 else 2
}

private fun parseReminderMinutes(reminderTime: String?): Int? {
    reminderTime ?: return null
    val parts = reminderTime.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val m = parts.getOrNull(1)?.toIntOrNull() ?: return null
    return h * 60 + m
}
