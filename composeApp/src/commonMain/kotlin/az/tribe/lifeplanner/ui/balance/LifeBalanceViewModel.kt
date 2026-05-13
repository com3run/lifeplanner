package az.tribe.lifeplanner.ui.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.network.AiProxyService
import co.touchlab.kermit.Logger
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import az.tribe.lifeplanner.domain.model.BalanceInsight
import az.tribe.lifeplanner.domain.model.BalanceRecommendation
import az.tribe.lifeplanner.data.repository.BuiltinCoachStore
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.LifeArea
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import az.tribe.lifeplanner.domain.model.LifeBalanceReport
import az.tribe.lifeplanner.domain.model.ManualAssessment
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.model.toGoalCategory
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeBalanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ─── Discovery Domain Models ──────────────────────────────────────────────────

data class DiscoveryExchange(
    val question: String,
    val area: LifeArea,
    val answer: String = "",
    val reflection: String = ""
)

data class DiscoveryGoal(
    val id: String,
    val type: String, // "goal" or "habit"
    val title: String,
    val description: String,
    val area: LifeArea,
    val timeline: GoalTimeline = GoalTimeline.SHORT_TERM,
    val milestones: List<String> = emptyList()
)

enum class DiscoveryPhase {
    IDLE, GENERATING_QUESTION, ACTIVE, REFLECTING, GENERATING_OUTCOME, DONE
}

data class DiscoveryState(
    val phase: DiscoveryPhase = DiscoveryPhase.IDLE,
    val exchanges: List<DiscoveryExchange> = emptyList(),
    val currentQuestion: String = "",
    val currentArea: LifeArea? = null,
    val streamedText: String = "",
    val generatedGoals: List<DiscoveryGoal> = emptyList(),
    val addedIds: Set<String> = emptySet()
)

// ─── UI State ─────────────────────────────────────────────────────────────────

data class LifeBalanceUiState(
    val isLoading: Boolean = false,
    val report: LifeBalanceReport? = null,
    val selectedArea: LifeArea? = null,
    val showAssessmentDialog: Boolean = false,
    val assessmentArea: LifeArea? = null,
    val error: String? = null,
    val isPreGenerating: Boolean = false,
    val goalCreatedFeedback: String? = null,
    val createdGoalIds: Set<String> = emptySet(),
    val showCoachSheet: Boolean = false,
    val selectedInsight: BalanceInsight? = null,
    val relevantCoaches: List<CoachPersona> = emptyList(),
    val discovery: DiscoveryState = DiscoveryState()
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalUuidApi::class)
class LifeBalanceViewModel(
    private val repository: LifeBalanceRepository,
    private val goalRepository: GoalRepository,
    private val aiProxy: AiProxyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LifeBalanceUiState())
    val uiState: StateFlow<LifeBalanceUiState> = _uiState.asStateFlow()

    companion object {
        private const val MAX_EXCHANGES = 2
        private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
    }

    init {
        loadBalance()
    }

    fun loadBalance(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val report = repository.calculateCurrentBalance(forceRefresh)
                Analytics.lifeBalanceChecked(report.overallScore.toFloat())
                _uiState.value = _uiState.value.copy(isLoading = false, report = report)
                preGenerateGoals(report)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to calculate balance: ${e.message}"
                )
            }
        }
    }

    private fun preGenerateGoals(report: LifeBalanceReport) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPreGenerating = true)
            try {
                val updatedRecs = repository.preGenerateGoalsForRecommendations(
                    report.recommendations, report.areaScores
                )
                _uiState.value = _uiState.value.copy(
                    report = report.copy(recommendations = updatedRecs),
                    isPreGenerating = false
                )
            } catch (e: Exception) {
                Logger.e("LifeBalanceViewModel") { "preGenerateGoals failed: ${e.message}" }
                _uiState.value = _uiState.value.copy(isPreGenerating = false)
            }
        }
    }

    fun createGoalFromRecommendation(recommendation: BalanceRecommendation) {
        val goal = recommendation.preGeneratedGoal ?: return
        viewModelScope.launch {
            try {
                goalRepository.insertGoal(goal)
                _uiState.value = _uiState.value.copy(
                    goalCreatedFeedback = "Goal \"${goal.title}\" added!",
                    createdGoalIds = _uiState.value.createdGoalIds + recommendation.targetArea.name
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    goalCreatedFeedback = "Failed to create goal: ${e.message}"
                )
            }
        }
    }

    fun clearGoalFeedback() {
        _uiState.value = _uiState.value.copy(goalCreatedFeedback = null)
    }

    fun showCoachSheetForInsight(insight: BalanceInsight) {
        val coaches = if (insight.relatedAreas.isNotEmpty()) {
            val matched = insight.relatedAreas.map { it.toGoalCategory() }
                .toSet().map { CoachPersona.getByCategory(it) }.toSet()
            (matched + CoachPersona.getGeneral()).toList()
        } else {
            BuiltinCoachStore.getAll()
        }
        _uiState.value = _uiState.value.copy(
            showCoachSheet = true, selectedInsight = insight, relevantCoaches = coaches
        )
    }

    fun hideCoachSheet() {
        _uiState.value = _uiState.value.copy(
            showCoachSheet = false, selectedInsight = null, relevantCoaches = emptyList()
        )
    }

    fun buildInsightMessage(insight: BalanceInsight): String {
        val areasText = if (insight.relatedAreas.isNotEmpty())
            " Related areas: ${insight.relatedAreas.joinToString(", ") { it.displayName }}." else ""
        return "I'd like your advice on this insight from my Life Balance assessment: " +
                "**${insight.title}** — ${insight.description}$areasText What steps do you recommend?"
    }

    fun selectArea(area: LifeArea?) { _uiState.value = _uiState.value.copy(selectedArea = area) }

    fun showAssessmentDialog(area: LifeArea) {
        _uiState.value = _uiState.value.copy(showAssessmentDialog = true, assessmentArea = area)
    }

    fun hideAssessmentDialog() {
        _uiState.value = _uiState.value.copy(showAssessmentDialog = false, assessmentArea = null)
    }

    fun saveManualAssessment(area: LifeArea, score: Int, notes: String?) {
        viewModelScope.launch {
            repository.saveManualAssessment(
                ManualAssessment(area = area, score = score, notes = notes,
                    assessedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
            )
            hideAssessmentDialog()
            loadBalance()
        }
    }

    fun getAreaScore(area: LifeArea): LifeAreaScore? =
        _uiState.value.report?.areaScores?.find { it.area == area }

    fun saveCurrentReport() {
        viewModelScope.launch {
            _uiState.value.report?.let { repository.saveBalanceReport(it) }
        }
    }

    // ─── Discovery ────────────────────────────────────────────────────────────

    fun startDiscovery() {
        val report = _uiState.value.report ?: return
        viewModelScope.launch {
            updateDiscovery { copy(phase = DiscoveryPhase.GENERATING_QUESTION) }
            try {
                val area = report.weakestAreas.firstOrNull()
                    ?: report.areaScores.minByOrNull { it.score }?.area
                    ?: LifeArea.CAREER
                val question = generateQuestion(report, emptyList(), area)
                updateDiscovery {
                    copy(phase = DiscoveryPhase.ACTIVE, currentQuestion = question, currentArea = area)
                }
            } catch (e: Exception) {
                Logger.e("Discovery") { "startDiscovery failed: ${e.message}" }
                val area = report.weakestAreas.firstOrNull() ?: LifeArea.CAREER
                updateDiscovery {
                    copy(phase = DiscoveryPhase.ACTIVE,
                        currentQuestion = fallbackQuestion(area),
                        currentArea = area)
                }
            }
        }
    }

    fun submitDiscoveryAnswer(answer: String) {
        val discovery = _uiState.value.discovery
        if (discovery.phase != DiscoveryPhase.ACTIVE) return
        val report = _uiState.value.report ?: return
        val newExchange = DiscoveryExchange(
            question = discovery.currentQuestion,
            area = discovery.currentArea ?: LifeArea.CAREER,
            answer = answer
        )
        val allExchanges = discovery.exchanges + newExchange

        viewModelScope.launch {
            updateDiscovery {
                copy(phase = DiscoveryPhase.REFLECTING, exchanges = allExchanges, streamedText = "")
            }

            // Stream reflection
            val reflection = streamReflection(answer)
            val completedExchanges = allExchanges.map {
                if (it == newExchange) it.copy(reflection = reflection) else it
            }
            updateDiscovery { copy(exchanges = completedExchanges) }

            if (completedExchanges.size >= MAX_EXCHANGES) {
                // Generate final outcome
                updateDiscovery { copy(phase = DiscoveryPhase.GENERATING_OUTCOME) }
                try {
                    val goals = generateOutcome(completedExchanges, report)
                    updateDiscovery { copy(phase = DiscoveryPhase.DONE, generatedGoals = goals) }
                } catch (e: Exception) {
                    Logger.e("Discovery") { "generateOutcome failed: ${e.message}" }
                    updateDiscovery { copy(phase = DiscoveryPhase.DONE, generatedGoals = emptyList()) }
                }
            } else {
                // Generate next question
                updateDiscovery { copy(phase = DiscoveryPhase.GENERATING_QUESTION) }
                try {
                    val usedAreas = completedExchanges.map { it.area }.toSet()
                    val nextArea = report.weakestAreas.firstOrNull { it !in usedAreas }
                        ?: report.areaScores.filter { it.area !in usedAreas }
                            .minByOrNull { it.score }?.area
                        ?: LifeArea.WELLBEING
                    val question = generateQuestion(report, completedExchanges, nextArea)
                    updateDiscovery {
                        copy(phase = DiscoveryPhase.ACTIVE, currentQuestion = question, currentArea = nextArea)
                    }
                } catch (e: Exception) {
                    val usedAreas = completedExchanges.map { it.area }.toSet()
                    val nextArea = LifeArea.entries.firstOrNull { it !in usedAreas } ?: LifeArea.WELLBEING
                    updateDiscovery {
                        copy(phase = DiscoveryPhase.ACTIVE,
                            currentQuestion = fallbackQuestion(nextArea),
                            currentArea = nextArea)
                    }
                }
            }
        }
    }

    fun addDiscoveryGoal(dGoal: DiscoveryGoal) {
        if (dGoal.type != "goal") return
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val daysToAdd = when (dGoal.timeline) {
                    GoalTimeline.SHORT_TERM -> 30L
                    GoalTimeline.MID_TERM -> 90L
                    GoalTimeline.LONG_TERM -> 365L
                }
                val milestones = dGoal.milestones.mapIndexed { i, title ->
                    val offset = ((i + 1) * (daysToAdd / (dGoal.milestones.size + 1)))
                    Milestone(id = Uuid.random().toString(), title = title, isCompleted = false,
                        dueDate = now.date.plus(offset, DateTimeUnit.DAY))
                }
                val goal = Goal(
                    id = Uuid.random().toString(),
                    category = dGoal.area.toGoalCategory(),
                    title = dGoal.title,
                    description = dGoal.description,
                    status = GoalStatus.IN_PROGRESS,
                    timeline = dGoal.timeline,
                    dueDate = now.date.plus(daysToAdd, DateTimeUnit.DAY),
                    milestones = milestones,
                    createdAt = now
                )
                goalRepository.insertGoal(goal)
                updateDiscovery { copy(addedIds = addedIds + dGoal.id) }
                _uiState.value = _uiState.value.copy(goalCreatedFeedback = "Goal \"${dGoal.title}\" added!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(goalCreatedFeedback = "Failed to add goal.")
            }
        }
    }

    fun resetDiscovery() {
        updateDiscovery { DiscoveryState() }
    }

    private fun updateDiscovery(block: DiscoveryState.() -> DiscoveryState) {
        _uiState.value = _uiState.value.copy(discovery = _uiState.value.discovery.block())
    }

    private suspend fun generateQuestion(
        report: LifeBalanceReport,
        previousExchanges: List<DiscoveryExchange>,
        area: LifeArea
    ): String {
        val areaScore = report.areaScores.find { it.area == area }
        val context = buildString {
            appendLine("Life balance context:")
            report.areaScores.forEach { appendLine("  ${it.area.displayName}: ${it.score}/100 (${it.activeGoals} goals, ${it.habitCount} habits)") }
            appendLine("Focus: ${area.displayName} (score: ${areaScore?.score ?: 0}/100)")
            if (previousExchanges.isNotEmpty()) {
                appendLine("Previous conversation:")
                previousExchanges.forEach { appendLine("  Q: ${it.question}\n  A: ${it.answer}") }
            }
        }
        val prompt = "$context\nAsk ONE powerful open-ended question about this person's ${area.displayName} life. Be specific and personal. Return ONLY the question."
        return aiProxy.generateText(prompt,
            "You are a perceptive life coach. Ask warm, insightful, specific questions that make people reflect deeply. One question only."
        ).trim().removePrefix("\"").removeSuffix("\"")
    }

    private suspend fun streamReflection(answer: String): String {
        val messages = listOf(AiProxyService.ChatMessage("user",
            "The user answered: \"$answer\"\n\nAcknowledge this in 1-2 warm, insightful sentences. Be specific to what they shared. No questions yet."
        ))
        var full = ""
        aiProxy.chatStream(messages,
            "You are a wise, warm life coach. Reflect genuinely on the user's answer. 1-2 sentences max. Be specific, not generic."
        ).collect { event ->
            when (event) {
                is AiProxyService.StreamEvent.TextChunk -> {
                    full += event.text
                    updateDiscovery { copy(streamedText = full) }
                }
                is AiProxyService.StreamEvent.Done -> full = event.fullText
                is AiProxyService.StreamEvent.Error -> {}
            }
        }
        return full
    }

    private suspend fun generateOutcome(
        exchanges: List<DiscoveryExchange>,
        report: LifeBalanceReport
    ): List<DiscoveryGoal> {
        val conversation = exchanges.joinToString("\n") { "Q: ${it.question}\nA: ${it.answer}" }
        val scores = report.areaScores.joinToString(", ") { "${it.area.name}:${it.score}" }
        val prompt = """
            Life coaching conversation:
            $conversation

            Balance scores: $scores

            Based on this conversation, suggest exactly 2-3 personalized goals or habits. Return JSON only (no markdown):
            {"items":[{"type":"goal","title":"...","description":"...","area":"CAREER","timeline":"SHORT_TERM","milestones":["...","...","..."]},{"type":"habit","title":"...","description":"...","area":"BODY"}]}

            Rules: area must be one of CAREER,MONEY,BODY,PEOPLE,WELLBEING,PURPOSE. timeline: SHORT_TERM, MID_TERM, or LONG_TERM.
        """.trimIndent()

        val raw = aiProxy.generateText(prompt)
        return parseOutcome(raw)
    }

    private fun parseOutcome(raw: String): List<DiscoveryGoal> {
        return try {
            val clean = raw.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val response = jsonParser.decodeFromString<DiscoveryOutcomeResponse>(clean)
            response.items.mapNotNull { item ->
                val area = try { LifeArea.valueOf(item.area) } catch (_: Exception) { null } ?: return@mapNotNull null
                val timeline = when (item.timeline) {
                    "MID_TERM" -> GoalTimeline.MID_TERM
                    "LONG_TERM" -> GoalTimeline.LONG_TERM
                    else -> GoalTimeline.SHORT_TERM
                }
                DiscoveryGoal(
                    id = Uuid.random().toString(),
                    type = item.type,
                    title = item.title,
                    description = item.description,
                    area = area,
                    timeline = timeline,
                    milestones = item.milestones
                )
            }
        } catch (e: Exception) {
            Logger.e("Discovery") { "parseOutcome failed: ${e.message}, raw: $raw" }
            emptyList()
        }
    }

    private fun fallbackQuestion(area: LifeArea): String = when (area) {
        LifeArea.CAREER -> "What's one thing in your career that you keep pushing off, even though you know it matters?"
        LifeArea.MONEY -> "When you think about your finances, what's the one change that would bring you the most peace of mind?"
        LifeArea.BODY -> "What does your ideal relationship with your health and energy look like day-to-day?"
        LifeArea.PEOPLE -> "Which relationship in your life would benefit most from more of your presence and attention?"
        LifeArea.WELLBEING -> "When do you feel most at ease and mentally clear — what makes those moments different?"
        LifeArea.PURPOSE -> "What activity makes you lose track of time, and how often does it show up in your week?"
    }
}

// ─── Serialization helpers ────────────────────────────────────────────────────

@Serializable
private data class DiscoveryOutcomeResponse(val items: List<DiscoveryItemRaw> = emptyList())

@Serializable
private data class DiscoveryItemRaw(
    val type: String = "goal",
    val title: String = "",
    val description: String = "",
    val area: String = "CAREER",
    val timeline: String = "SHORT_TERM",
    val milestones: List<String> = emptyList()
)
