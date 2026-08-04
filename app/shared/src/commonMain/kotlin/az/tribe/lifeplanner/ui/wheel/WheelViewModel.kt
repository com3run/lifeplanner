package az.tribe.lifeplanner.ui.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.ComparisonPeriod
import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelComparison
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.repository.WheelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class WheelUiState(
    val report: WheelReport? = null,
    val selected: WheelArea? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val period: ComparisonPeriod = ComparisonPeriod.WEEK,
    val comparison: WheelComparison? = null,
    /**
     * How many days we have on record. Distinguishes "nothing to compare against yet" from
     * "compared, and nothing moved" — the same empty list on screen, but a very different thing
     * to tell the user.
     */
    val snapshotCount: Int = 0,
    val comparisonLoading: Boolean = false,
    val suggestion: az.tribe.lifeplanner.domain.service.WheelSuggestion? = null,
    /** Offer the user the chance to replace our predictions with their own nine numbers. */
    val showSetupPrompt: Boolean = false,
)

class WheelViewModel(
    private val repository: WheelRepository,
    private val settings: com.russhwolf.settings.Settings,
) : ViewModel() {

    /**
     * Whether to offer the "set your own nine" prompt.
     *
     * Anyone who signed up before the wheel moved into registration has a wheel made entirely of
     * our predictions, and no reason to know that. This offers them the same nine questions once,
     * so the numbers become theirs rather than ours.
     *
     * Shown only when nothing on the wheel is user-set, and never again once dismissed. A prompt
     * that comes back is the furniture problem, and this one sits on top of the thing it is about.
     */
    private fun shouldPrompt(report: WheelReport): Boolean =
        !settings.getBoolean(KEY_SETUP_PROMPT_DONE, false) &&
            // An empty report is a wheel that has not been computed yet, not one full of guesses.
            // Offering to correct nothing is the app talking to itself.
            report.scores.isNotEmpty() &&
            report.scores.none { it.source == ScoreSource.USER }

    fun dismissSetupPrompt() {
        settings.putBoolean(KEY_SETUP_PROMPT_DONE, true)
        _state.value = _state.value.copy(showSetupPrompt = false)
    }

    /**
     * Takes the whole set from the prompt at once, rather than one write per area, so the wheel
     * does not animate through nine intermediate states on the way to the user's actual answer.
     */
    fun setScores(scores: Map<WheelArea, Double>) {
        viewModelScope.launch {
            scores.forEach { (area, score) -> repository.setScore(area, score, note = "Set from the wheel") }
            settings.putBoolean(KEY_SETUP_PROMPT_DONE, true)
            _state.value = _state.value.copy(showSetupPrompt = false)
            repository.captureSnapshot()
            loadComparison(_state.value.period)
        }
    }

    private val _state = MutableStateFlow(WheelUiState())
    val state: StateFlow<WheelUiState> = _state.asStateFlow()

    init {
        // Capture on open, and before the first comparison, so today is on record. A past wheel
        // cannot be recomputed, so a day the user opens the screen and we fail to record is a day
        // of history gone for good.
        viewModelScope.launch {
            repository.captureSnapshot()
            loadComparison(_state.value.period)
        }

        viewModelScope.launch {
            repository.observeWheel()
                .catch { e -> _state.value = _state.value.copy(isLoading = false, error = e.message) }
                .collect { report ->
                    _state.value = _state.value.copy(
                        report = report,
                        isLoading = false,
                        error = null,
                        suggestion = suggestionFor(report),
                        showSetupPrompt = shouldPrompt(report),
                    )
                }
        }
    }

    /** Tapping the already-selected slice closes it, so the wheel is never stuck open. */
    fun select(area: WheelArea?) {
        _state.value = _state.value.copy(
            selected = if (area == _state.value.selected) null else area
        )
    }

    fun setScore(area: WheelArea, score: Double) {
        viewModelScope.launch {
            repository.setScore(area, score)
            // Today's snapshot is now out of date, and the comparison is measured against it.
            repository.captureSnapshot()
            loadComparison(_state.value.period)
        }
    }

    /** Hands the area back to the predictor. */
    fun clearScore(area: WheelArea) {
        viewModelScope.launch {
            repository.clearScore(area)
            repository.captureSnapshot()
            loadComparison(_state.value.period)
        }
    }

    fun setPeriod(period: ComparisonPeriod) {
        if (period == _state.value.period) return
        _state.value = _state.value.copy(period = period)
        viewModelScope.launch { loadComparison(period) }
    }

    /**
     * The suggestion for the weakest area, or null to stay quiet.
     *
     * Rotation is the day number, so the wording holds for a day and changes tomorrow. Rotating per
     * recomposition would reshuffle the card while the user reads it.
     */
    private fun suggestionFor(report: az.tribe.lifeplanner.domain.model.WheelReport):
        az.tribe.lifeplanner.domain.service.WheelSuggestion? {
        val area = az.tribe.lifeplanner.domain.service.WheelNudgePicker.pick(report) ?: return null
        val urgency = az.tribe.lifeplanner.domain.service.WheelNudgePicker.urgency(report, area)
        val day = report.generatedAt.date.toEpochDays().toInt()
        return az.tribe.lifeplanner.domain.service.WheelSuggestions.forArea(area, urgency, day)
    }

    private suspend fun loadComparison(period: ComparisonPeriod) {
        _state.value = _state.value.copy(comparisonLoading = true)
        val comparison = runCatching { repository.compareTo(period) }.getOrNull()
        val count = runCatching { repository.snapshots().size }.getOrDefault(0)
        _state.value = _state.value.copy(
            comparison = comparison,
            snapshotCount = count,
            comparisonLoading = false,
        )
    }

    private companion object {
        const val KEY_SETUP_PROMPT_DONE = "wheel_setup_prompt_done"
    }
}
