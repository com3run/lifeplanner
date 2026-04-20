package az.tribe.lifeplanner.ui.retrospective

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.domain.repository.RetrospectiveRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number

data class RetrospectiveUiState(
    val selectedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val snapshot: DaySnapshot? = null,
    val todaySnapshot: DaySnapshot? = null,
    val isLoading: Boolean = false,
    val activeDates: Set<LocalDate> = emptySet(),
    val aiRecap: String? = null,
    val isGeneratingRecap: Boolean = false,
    val error: String? = null
)

class RetrospectiveViewModel(
    private val repository: RetrospectiveRepository,
    private val aiProxy: AiProxyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RetrospectiveUiState())
    val uiState: StateFlow<RetrospectiveUiState> = _uiState.asStateFlow()

    private val today: LocalDate
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    init {
        viewModelScope.launch {
            // Always load today's snapshot eagerly for compare section
            val todaySnap = safeGetSnapshot(today)
            _uiState.update { it.copy(todaySnapshot = todaySnap) }

            // Start on yesterday if it has activity, otherwise today
            val yesterday = today.minus(DatePeriod(days = 1))
            val yesterdaySnapshot = safeGetSnapshot(yesterday)
            val startDate = if (yesterdaySnapshot?.hasAnyActivity == true) yesterday else today
            _uiState.update { it.copy(selectedDate = startDate) }
            loadSnapshotAndRecap(startDate)
            loadActiveDatesForMonth(startDate)
        }
    }

    fun selectDate(date: LocalDate) {
        if (date > today) return
        _uiState.update { it.copy(selectedDate = date, aiRecap = null) }
        loadSnapshotAndRecap(date)
        loadActiveDatesForMonth(date)
    }

    fun goToPreviousDay() {
        val prev = _uiState.value.selectedDate.minus(DatePeriod(days = 1))
        selectDate(prev)
    }

    fun goToNextDay() {
        val next = _uiState.value.selectedDate.plus(DatePeriod(days = 1))
        if (next <= today) selectDate(next)
    }

    fun generateRecap() {
        val snapshot = _uiState.value.snapshot ?: return
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingRecap = true) }
            try {
                val prompt = buildRecapPrompt(snapshot, date)
                val recap = aiProxy.generateText(
                    prompt = prompt,
                    systemPrompt = "You are a warm, encouraging personal life coach. Write short, personal day recaps."
                )
                repository.saveDayRecap(date, recap)
                _uiState.update { it.copy(aiRecap = recap, isGeneratingRecap = false) }
            } catch (e: Exception) {
                Logger.e("RetrospectiveViewModel") { "generateRecap failed: ${e.message}" }
                _uiState.update { it.copy(isGeneratingRecap = false) }
            }
        }
    }

    fun loadActiveDatesForMonth(date: LocalDate) {
        viewModelScope.launch {
            val monthStart = LocalDate(date.year, date.month.number, 1)
            val monthEnd = if (date.month.number == 12) {
                LocalDate(date.year + 1, 1, 1).minus(DatePeriod(days = 1))
            } else {
                LocalDate(date.year, date.month.number + 1, 1).minus(DatePeriod(days = 1))
            }
            val dates = repository.getDatesWithActivity(monthStart, monthEnd)
            _uiState.update { it.copy(activeDates = dates) }
        }
    }

    private fun loadSnapshotAndRecap(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val snapshot = repository.getDaySnapshot(date)
                val cachedRecap = repository.getDayRecap(date)

                // If today has data and no recap yet, auto-generate
                val shouldAutoGenerate = cachedRecap == null && snapshot.hasAnyActivity
                if (shouldAutoGenerate) {
                    _uiState.update { it.copy(snapshot = snapshot, isLoading = false) }
                    generateRecap()
                } else {
                    _uiState.update { it.copy(snapshot = snapshot, aiRecap = cachedRecap, isLoading = false) }
                }

                // Refresh today snapshot if viewing today
                if (date == today) {
                    _uiState.update { it.copy(todaySnapshot = snapshot) }
                }
            } catch (e: Exception) {
                Logger.e("RetrospectiveViewModel") { "loadSnapshot failed: ${e.message}" }
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun safeGetSnapshot(date: LocalDate): DaySnapshot? {
        return try { repository.getDaySnapshot(date) } catch (_: Exception) { null }
    }

    private fun buildRecapPrompt(snapshot: DaySnapshot, date: LocalDate): String {
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val dateStr = "$month ${date.day}, ${date.year}"

        val habitsLine = if (snapshot.habitSummary.totalHabits > 0) {
            "${snapshot.habitSummary.completedHabits} of ${snapshot.habitSummary.totalHabits} habits completed"
        } else "no habits tracked"

        val moodLine = snapshot.dominantMood?.let { "dominant mood: ${it.displayName}" } ?: "no journal entries"
        val focusLine = if (snapshot.totalFocusMinutes > 0) "${snapshot.totalFocusMinutes} minutes of focused work" else "no focus sessions"
        val journalLine = if (snapshot.journalEntries.isNotEmpty()) {
            "wrote ${snapshot.journalEntries.size} journal ${if (snapshot.journalEntries.size == 1) "entry" else "entries"}"
        } else "no journal entries"
        val badgesLine = if (snapshot.badgesEarned.isNotEmpty()) {
            "earned ${snapshot.badgesEarned.size} ${if (snapshot.badgesEarned.size == 1) "badge" else "badges"}: ${snapshot.badgesEarned.joinToString { it.type.displayName }}"
        } else ""
        val goalChanges = if (snapshot.goalChanges.isNotEmpty()) {
            "made ${snapshot.goalChanges.size} goal ${if (snapshot.goalChanges.size == 1) "update" else "updates"}"
        } else ""

        val extras = listOfNotNull(
            badgesLine.ifEmpty { null },
            goalChanges.ifEmpty { null }
        ).joinToString(", ")

        return """
            Here is a summary of the user's day on $dateStr:
            - Habits: $habitsLine
            - Mood: $moodLine
            - Focus: $focusLine
            - Journal: $journalLine
            ${if (extras.isNotEmpty()) "- Also: $extras" else ""}

            Write a warm, personal 2–3 sentence recap of their day. Be specific about what they did.
            If the day was productive, celebrate it. If not, be encouraging and forward-looking.
            Keep it under 80 words. Do not use bullet points. Write as if speaking directly to them.
        """.trimIndent()
    }
}
