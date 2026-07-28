package az.tribe.lifeplanner.ui.trajectory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.trajectory.BalancePastReconstructor
import az.tribe.lifeplanner.domain.model.TrajectoryPoint
import az.tribe.lifeplanner.domain.repository.LifeBalanceRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backs the explorable life-balance trajectory graph. Loads the real current balance score and
 * reconstructs the past from activity; the screen owns the "effort" slider and recomputes the
 * projected lines via [az.tribe.lifeplanner.domain.service.TrajectoryProjector] as it moves.
 */
class TrajectoryViewModel(
    private val lifeBalanceRepository: LifeBalanceRepository,
    private val reconstructor: BalancePastReconstructor,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val currentScore: Int = 0,
        val past: List<TrajectoryPoint> = emptyList(),
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState(loading = true)
            // Rule-based area scores, fast and AI-free (calculateCurrentBalance can hit the AI proxy).
            val scores = runCatching { lifeBalanceRepository.getAllAreaScores() }
                .onFailure { Logger.w("TrajectoryViewModel") { "area scores failed: ${it.message}" } }
                .getOrNull()
            val score = scores?.takeIf { it.isNotEmpty() }?.let { it.sumOf { s -> s.score } / it.size }
                ?: DEFAULT_SCORE
            val past = runCatching { reconstructor.weeklyPast(PAST_WEEKS, score.toFloat()) }
                .getOrDefault(listOf(TrajectoryPoint(0, score.toFloat())))
            _state.value = UiState(loading = false, currentScore = score, past = past)
        }
    }

    companion object {
        const val PAST_WEEKS = 8
        const val HORIZON_WEEKS = 12
        const val IDEAL_SCORE = 85f
        private const val DEFAULT_SCORE = 50
    }
}
