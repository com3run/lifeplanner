package az.tribe.lifeplanner.ui.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.model.AppUsagePattern
import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

enum class InsightPhase {
    LOADING, EMPTY, OVERVIEW, PATTERNS, GENERATING_RECS, RECOMMENDATIONS
}

data class BehaviorRecommendation(
    val title: String,
    val body: String,
    val actionLabel: String? = null,
    val actionRoute: String? = null
)

data class ScreenTimeInsightState(
    val phase: InsightPhase = InsightPhase.LOADING,
    val pattern: AppUsagePattern? = null,
    val recommendations: List<BehaviorRecommendation> = emptyList(),
    val streamingText: String = "",
    val error: String? = null
)

class ScreenTimeInsightViewModel(
    private val behaviorRepository: BehaviorRepository,
    private val aiProxy: AiProxyService
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenTimeInsightState())
    val state: StateFlow<ScreenTimeInsightState> = _state.asStateFlow()

    init {
        loadPattern()
    }

    fun loadPattern() {
        viewModelScope.launch {
            _state.update { it.copy(phase = InsightPhase.LOADING, error = null) }
            try {
                behaviorRepository.refreshPattern()
                val pattern = behaviorRepository.getPattern()
                if (pattern == null || !pattern.hasEnoughData) {
                    _state.update { it.copy(phase = InsightPhase.EMPTY, pattern = pattern) }
                } else {
                    _state.update { it.copy(phase = InsightPhase.OVERVIEW, pattern = pattern) }
                }
            } catch (e: Exception) {
                Logger.e("ScreenTimeInsightVM") { "Load failed: ${e.message}" }
                _state.update { it.copy(phase = InsightPhase.EMPTY, error = e.message) }
            }
        }
    }

    fun advanceToPatterns() {
        _state.update { it.copy(phase = InsightPhase.PATTERNS) }
    }

    fun generateRecommendations() {
        val pattern = _state.value.pattern ?: return
        _state.update { it.copy(phase = InsightPhase.GENERATING_RECS, streamingText = "") }
        viewModelScope.launch {
            try {
                val prompt = buildPrompt(pattern)
                var accumulated = ""
                aiProxy.chatStream(
                    messages = listOf(AiProxyService.ChatMessage("user", prompt)),
                    systemPrompt = RECS_SYSTEM_PROMPT
                ).collect { event ->
                    when (event) {
                        is AiProxyService.StreamEvent.TextChunk -> {
                            accumulated += event.text
                            _state.update { it.copy(streamingText = accumulated) }
                        }
                        is AiProxyService.StreamEvent.Done -> {
                            val recs = parseRecommendations(event.fullText)
                            _state.update {
                                it.copy(
                                    phase = InsightPhase.RECOMMENDATIONS,
                                    recommendations = recs,
                                    streamingText = ""
                                )
                            }
                        }
                        is AiProxyService.StreamEvent.Error -> {
                            _state.update {
                                it.copy(
                                    phase = InsightPhase.RECOMMENDATIONS,
                                    recommendations = fallbackRecommendations(pattern),
                                    streamingText = ""
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("ScreenTimeInsightVM") { "AI recs failed: ${e.message}" }
                _state.update {
                    it.copy(
                        phase = InsightPhase.RECOMMENDATIONS,
                        recommendations = fallbackRecommendations(pattern),
                        streamingText = ""
                    )
                }
            }
        }
    }

    private fun buildPrompt(p: AppUsagePattern): String {
        val topScreens = p.topFeatures(3).joinToString(", ") { pair ->
            "${pair.first} (avg ${pair.second / 1000}s)"
        }
        val peakHour = p.peakHourLabel()
        val days = p.mostActiveDays.take(3).joinToString(", ")
        val avgMin = (p.sessionAvgMinutes * 10).toLong() / 10.0
        val sessWk = (p.sessionsPerWeek * 10).toLong() / 10.0
        return """
            User's in-app behavior (last 30 days):
            - Peak hour: $peakHour
            - Most active days: $days
            - Top screens: $topScreens
            - Avg session: $avgMin min, ~$sessWk sessions/week

            Give 3 personalized recommendations to help this user get more out of the app.
            Each recommendation should be 1 short title and 1-2 sentence body.
            Format strictly as JSON array:
            [{"title":"...","body":"...","actionRoute":"screen_route_or_null"}]
        """.trimIndent()
    }

    private fun parseRecommendations(rawJson: String): List<BehaviorRecommendation> {
        return try {
            val trimmed = rawJson.trim()
            val start = trimmed.indexOf('[')
            val end = trimmed.lastIndexOf(']')
            if (start < 0 || end < 0) return fallbackRecommendations(_state.value.pattern)
            val json = Json { ignoreUnknownKeys = true }
            val array = json.parseToJsonElement(trimmed.substring(start, end + 1)) as? JsonArray
                ?: return fallbackRecommendations(_state.value.pattern)
            array.mapNotNull { el ->
                val obj = el as? JsonObject ?: return@mapNotNull null
                BehaviorRecommendation(
                    title = obj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                    body = obj["body"]?.jsonPrimitive?.content ?: "",
                    actionRoute = obj["actionRoute"]?.jsonPrimitive?.content
                        ?.takeIf { it != "null" && it.isNotBlank() }
                )
            }
        } catch (_: Exception) {
            fallbackRecommendations(_state.value.pattern)
        }
    }

    private fun fallbackRecommendations(p: AppUsagePattern?): List<BehaviorRecommendation> {
        val peak = p?.peakHourLabel() ?: "morning"
        return listOf(
            BehaviorRecommendation(
                title = "Build your peak-hour ritual",
                body = "You're most active in the $peak. Schedule your hardest habit check-ins then for best follow-through.",
                actionRoute = "habit_tracker"
            ),
            BehaviorRecommendation(
                title = "Use your coach at your best time",
                body = "A quick daily chat with your coach during your peak hours can set the tone for the day.",
                actionRoute = "ai_chat"
            ),
            BehaviorRecommendation(
                title = "Set a weekly review reminder",
                body = "Your most active days are the best time for a quick retrospective review of your goals.",
                actionRoute = "retrospective"
            )
        )
    }

    companion object {
        private const val RECS_SYSTEM_PROMPT = """You are a life coaching assistant analyzing in-app usage patterns.
Give concrete, actionable personalized recommendations. Be warm and encouraging.
Always respond with valid JSON array only, no markdown, no extra text."""
    }
}
