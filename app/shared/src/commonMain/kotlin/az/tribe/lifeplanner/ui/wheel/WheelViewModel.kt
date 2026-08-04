package az.tribe.lifeplanner.ui.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.ComparisonPeriod
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
)

class WheelViewModel(
    private val repository: WheelRepository,
) : ViewModel() {

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
}
