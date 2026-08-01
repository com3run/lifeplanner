package az.tribe.lifeplanner.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.CausalInsight
import az.tribe.lifeplanner.domain.model.DecisionStatus
import az.tribe.lifeplanner.domain.model.InsightConfidence
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.LifeBalanceRepository
import az.tribe.lifeplanner.domain.repository.RetrospectiveRepository
import az.tribe.lifeplanner.domain.service.CausalInsightProvider
import az.tribe.lifeplanner.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * One thing that needs the user, stated plainly with a way to act on it. Ranked so the You tab can
 * lead with whatever matters most right now instead of a fixed list of rows.
 *
 * Scores follow the same scale as `HomeFeedBuilder` so the two surfaces agree on urgency.
 */
data class YouAttention(
    val id: String,
    val title: String,
    val body: String,
    val actionLabel: String,
    val route: String,
    val score: Int,
)

/**
 * What the You tab knows about you right now: the live numbers behind each surface, plus the
 * ranked list of things asking for attention. Everything here is derived from local data, no AI
 * call, so the tab stays instant and works offline.
 */
data class YouState(
    val loading: Boolean = true,
    val attention: List<YouAttention> = emptyList(),
    val areaScores: List<LifeAreaScore> = emptyList(),
    val overallBalance: Int = 0,
    val weakestArea: LifeAreaScore? = null,
    val topInsight: CausalInsight? = null,
    val peakHourLabel: String? = null,
)

class YouViewModel(
    private val decisionRepository: DecisionRepository,
    private val lifeBalanceRepository: LifeBalanceRepository,
    private val retrospectiveRepository: RetrospectiveRepository,
    private val behaviorRepository: BehaviorRepository,
    private val causalInsightProvider: CausalInsightProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(YouState())
    val state: StateFlow<YouState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            // Each signal is independently optional: a repository that fails or has no data yet
            // just drops its card rather than blanking the whole tab.
            val toReview = runCatching {
                decisionRepository.getAllDecisions()
                    .count { it.status == DecisionStatus.CONFIRMED && it.outcomeQuality == null }
            }.getOrDefault(0)

            val areaScores = runCatching { lifeBalanceRepository.getAllAreaScores() }
                .getOrDefault(emptyList())
            val overall = if (areaScores.isEmpty()) 0 else areaScores.map { it.score }.average().toInt()
            val weakest = areaScores.minByOrNull { it.score }

            val quietDays = runCatching {
                retrospectiveRepository.observeWeeklySnapshots().first().count { !it.hasAnyActivity }
            }.getOrDefault(0)

            val peakHour = runCatching {
                behaviorRepository.getPattern()?.takeIf { it.hasEnoughData }?.peakHourLabel()
            }.getOrNull()

            // Same convention the home feed uses: a low-confidence correlation is not worth stating.
            val topInsight = runCatching {
                causalInsightProvider.insights().firstOrNull { it.confidence != InsightConfidence.LOW }
            }.getOrNull()

            val attention = buildList {
                if (toReview > 0) {
                    add(
                        YouAttention(
                            id = "decisions_to_review",
                            title = if (toReview == 1) "1 decision is waiting on you"
                            else "$toReview decisions are waiting on you",
                            body = "Grade the reasoning while you still remember it, not just how it turned out.",
                            actionLabel = "Review",
                            route = Screen.DecisionJournal.route,
                            score = 84,
                        )
                    )
                }
                if (quietDays >= 3) {
                    add(
                        YouAttention(
                            id = "quiet_days",
                            title = "$quietDays quiet days this week",
                            body = "Nothing logged on those days. A look back is usually enough to spot why.",
                            actionLabel = "Look back",
                            route = Screen.Retrospective.route,
                            score = 80,
                        )
                    )
                }
                if (weakest != null && weakest.score < BALANCE_ATTENTION_THRESHOLD) {
                    add(
                        YouAttention(
                            id = "weak_area",
                            title = "${weakest.area.displayName} is slipping",
                            body = "It's your lowest area at ${weakest.score}/100. Worth a look before it drifts further.",
                            actionLabel = "Open balance",
                            route = Screen.ForYou.route,
                            score = 76,
                        )
                    )
                }
            }.sortedByDescending { it.score }

            _state.value = YouState(
                loading = false,
                attention = attention,
                areaScores = areaScores,
                overallBalance = overall,
                weakestArea = weakest,
                topInsight = topInsight,
                peakHourLabel = peakHour,
            )
        }
    }

    private companion object {
        /** Below this an area reads as neglected rather than merely uneven. */
        const val BALANCE_ATTENTION_THRESHOLD = 40
    }
}
