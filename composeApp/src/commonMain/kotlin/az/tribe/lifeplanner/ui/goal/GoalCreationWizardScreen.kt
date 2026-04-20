package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.data.repository.CoachOrchestrator
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.model.UserSituation
import az.tribe.lifeplanner.domain.repository.UserRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import az.tribe.lifeplanner.util.PlatformBackHandler
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.add
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ─── Step enum ────────────────────────────────────────────────────────────────

enum class GoalWizardStep { INTENT, QUESTIONS, GENERATING, SELECTION, DETAILS, TIMELINE, MILESTONES }

// ─── Data classes ─────────────────────────────────────────────────────────────

internal data class WizardQuestion(val question: String, val options: List<String>)

internal data class GoalOption(
    val focus: String,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val timeline: GoalTimeline,
    val milestones: List<String>,
    val reasoning: String
)

// ─── Goal idea prompts ────────────────────────────────────────────────────────

internal val GOAL_IDEAS = listOf(
    "Run a marathon",
    "Save money for a house",
    "Start my own business",
    "Learn a new language",
    "Get fit and lose weight",
    "Read 24 books this year",
    "Travel to a new country",
    "Meditate every day",
    "Get a promotion at work",
    "Build an emergency fund"
)

// ─── Main composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun GoalCreationWizardScreen(
    viewModel: GoalViewModel,
    onGoalCreated: (goalId: String) -> Unit,
    onBackClick: () -> Unit,
    aiProxy: AiProxyService = koinInject(),
    userSituationRepo: UserSituationRepository = koinInject(),
    userRepo: UserRepository = koinInject(),
    orchestrator: CoachOrchestrator = koinInject()
) {
    val scope = rememberCoroutineScope()

    var userSituation by remember { mutableStateOf<UserSituation?>(null) }
    LaunchedEffect(Unit) {
        userRepo.getCurrentUser()?.id?.let { userId ->
            userSituation = userSituationRepo.getOrCreate(userId)
        }
    }

    var step by remember { mutableStateOf(GoalWizardStep.INTENT) }
    var isAiPath by remember { mutableStateOf(false) }

    var intentText by remember { mutableStateOf("") }
    var detectedCategory by remember { mutableStateOf(GoalCategory.WELLBEING) }
    var generationError by remember { mutableStateOf<String?>(null) }

    var aiGoalOptions by remember { mutableStateOf<List<GoalOption>>(emptyList()) }

    var aiGeneratedQuestions by remember { mutableStateOf<List<WizardQuestion>>(emptyList()) }
    val questionAnswers = remember { mutableStateListOf<List<String>>() }
    var isGeneratingQuestions by remember { mutableStateOf(false) }

    var goalTitle by remember { mutableStateOf("") }
    var goalDescription by remember { mutableStateOf("") }
    var goalCategory by remember { mutableStateOf(GoalCategory.CAREER) }
    var aiReasoning by remember { mutableStateOf<String?>(null) }

    var goalTimeline by remember { mutableStateOf(GoalTimeline.SHORT_TERM) }
    var goalDueDate by remember {
        mutableStateOf(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                .plus(DatePeriod(months = 3))
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val aiMilestones = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val customMilestones = remember { mutableStateListOf<String>() }
    var customMilestoneInput by remember { mutableStateOf("") }
    var councilNotes by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(intentText) {
        detectedCategory = detectCategoryFromText(intentText)
    }

    LaunchedEffect(goalTimeline) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        goalDueDate = when (goalTimeline) {
            GoalTimeline.SHORT_TERM -> today.plus(DatePeriod(months = 3))
            GoalTimeline.MID_TERM -> today.plus(DatePeriod(months = 6))
            GoalTimeline.LONG_TERM -> today.plus(DatePeriod(months = 18))
        }
    }

    PlatformBackHandler(
        enabled = step != GoalWizardStep.INTENT && step != GoalWizardStep.GENERATING
    ) {
        step = when (step) {
            GoalWizardStep.QUESTIONS -> {
                aiGeneratedQuestions = emptyList()
                questionAnswers.clear()
                GoalWizardStep.INTENT
            }
            GoalWizardStep.SELECTION -> GoalWizardStep.QUESTIONS
            GoalWizardStep.DETAILS -> if (isAiPath) GoalWizardStep.SELECTION else GoalWizardStep.INTENT
            GoalWizardStep.TIMELINE -> GoalWizardStep.DETAILS
            GoalWizardStep.MILESTONES -> GoalWizardStep.TIMELINE
            else -> GoalWizardStep.INTENT
        }
    }

    fun goToQuestions() {
        isAiPath = true
        generationError = null
        isGeneratingQuestions = true
        scope.launch {
            try {
                val profileContext = userSituation?.let {
                    val coach = runCatching { CoachPersona.getByCategory(detectedCategory) }.getOrNull()
                    orchestrator.buildSituationContext(it, coach).takeIf { c -> c.isNotBlank() }
                } ?: ""
                val prompt = """
                    The user wants to achieve: "$intentText"
                    ${if (profileContext.isNotBlank()) "\n$profileContext\nUse this profile to SKIP questions about information already known. Do NOT ask about known facts." else ""}

                    Generate exactly 9 clarifying questions to deeply understand and personalise their goal.
                    Every question must be directly specific to their stated intent — not generic filler.
                    Each question must allow multiple answers and have 5–6 options tailored to their goal.
                    For each question include at least one "tricky" or unexpected option that reveals hidden priorities or challenges.
                    Always include "None of the above" as the final option for every question.
                    Cover: motivation/why, prior experience, current obstacles, timeline preferences,
                    support system, definition of success, measurement approach, lifestyle factors, commitment level.
                """.trimIndent()

                val schema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("questions") {
                            put("type", "array")
                            putJsonObject("items") {
                                put("type", "object")
                                putJsonObject("properties") {
                                    putJsonObject("question") { put("type", "string") }
                                    putJsonObject("options") {
                                        put("type", "array")
                                        putJsonObject("items") { put("type", "string") }
                                    }
                                }
                                putJsonArray("required") {
                                    add(JsonPrimitive("question"))
                                    add(JsonPrimitive("options"))
                                }
                            }
                        }
                    }
                    putJsonArray("required") { add(JsonPrimitive("questions")) }
                }

                val responseText = withContext(Dispatchers.IO) {
                    aiProxy.generateStructuredJson(prompt, schema)
                }

                val json = Json { ignoreUnknownKeys = true }
                val obj = json.parseToJsonElement(responseText).jsonObject
                val parsed = obj["questions"]?.jsonArray?.mapNotNull { el ->
                    val qObj = el.jsonObject
                    val q = qObj["question"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val opts = qObj["options"]?.jsonArray
                        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                        ?: return@mapNotNull null
                    if (opts.isEmpty()) null else WizardQuestion(q, opts)
                } ?: emptyList()

                aiGeneratedQuestions = parsed
                if (parsed.isEmpty()) {
                    generationError = "Couldn't generate questions. Check your connection and try again."
                    isGeneratingQuestions = false
                    return@launch
                }
            } catch (_: Exception) {
                generationError = "Couldn't generate questions. Check your connection and try again."
                isGeneratingQuestions = false
                return@launch
            }
            questionAnswers.clear()
            repeat(aiGeneratedQuestions.size) { questionAnswers.add(emptyList()) }
            isGeneratingQuestions = false
            step = GoalWizardStep.QUESTIONS
        }
    }

    fun proceedManually() {
        isAiPath = false
        goalTitle = ""
        goalDescription = ""
        goalCategory = detectedCategory
        aiMilestones.clear()
        aiReasoning = null
        step = GoalWizardStep.DETAILS
    }

    fun goBack() {
        when (step) {
            GoalWizardStep.INTENT -> { onBackClick(); return }
            GoalWizardStep.QUESTIONS -> {
                aiGeneratedQuestions = emptyList()
                questionAnswers.clear()
                step = GoalWizardStep.INTENT
            }
            GoalWizardStep.SELECTION -> step = GoalWizardStep.QUESTIONS
            GoalWizardStep.DETAILS -> step = if (isAiPath) GoalWizardStep.SELECTION else GoalWizardStep.INTENT
            GoalWizardStep.TIMELINE -> step = GoalWizardStep.DETAILS
            GoalWizardStep.MILESTONES -> step = GoalWizardStep.TIMELINE
            else -> step = GoalWizardStep.INTENT
        }
    }

    fun generateWithAi() {
        generationError = null
        step = GoalWizardStep.GENERATING
        scope.launch {
            try {
                val contextLines = buildString {
                    aiGeneratedQuestions.forEachIndexed { idx, q ->
                        val selected = questionAnswers.getOrNull(idx)
                        if (!selected.isNullOrEmpty()) appendLine("${q.question}: ${selected.joinToString(", ")}")
                    }
                }
                val profileContext = userSituation?.let {
                    val coach = runCatching { CoachPersona.getByCategory(detectedCategory) }.getOrNull()
                    orchestrator.buildSituationContext(it, coach).takeIf { c -> c.isNotBlank() }
                } ?: ""
                val prompt = """
                    The user wants to achieve: "$intentText"
                    ${contextLines.trim()}
                    ${if (profileContext.isNotBlank()) "\n$profileContext\nCalibrate ambition and timeline to the user's stress level, sleep quality, and life stage shown above." else ""}

                    Generate exactly 3 goal options for the user — each with a different ambition level:
                    - "Quick win": SHORT_TERM (1-3 months), achievable fast, builds confidence
                    - "Steady climb": MID_TERM (3-9 months), balanced effort and reward
                    - "Full transformation": LONG_TERM (9+ months), ambitious and life-changing

                    Each option must:
                    - Have a title starting with an action verb, max 60 chars, motivating and specific
                    - Be deeply personalised to the user's answers above
                    - Include 4-5 concrete, measurable milestones in chronological order
                    - Choose the GoalCategory that best fits
                """.trimIndent()

                val optionSchema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("focus") { put("type", "string") }
                        putJsonObject("title") { put("type", "string") }
                        putJsonObject("description") { put("type", "string") }
                        putJsonObject("category") {
                            put("type", "string")
                            putJsonArray("enum") { GoalCategory.entries.forEach { add(it.name) } }
                        }
                        putJsonObject("timeline") {
                            put("type", "string")
                            putJsonArray("enum") { GoalTimeline.entries.forEach { add(it.name) } }
                        }
                        putJsonObject("milestones") {
                            put("type", "array")
                            putJsonObject("items") { put("type", "string") }
                        }
                        putJsonObject("reasoning") { put("type", "string") }
                    }
                    putJsonArray("required") {
                        listOf("focus", "title", "description", "category", "timeline", "milestones", "reasoning")
                            .forEach { add(JsonPrimitive(it)) }
                    }
                }
                val schema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("options") {
                            put("type", "array")
                            put("items", optionSchema)
                        }
                    }
                    putJsonArray("required") { add(JsonPrimitive("options")) }
                }

                val responseText = withContext(Dispatchers.IO) {
                    aiProxy.generateStructuredJson(prompt, schema)
                }

                val json = Json { ignoreUnknownKeys = true }
                val obj = json.parseToJsonElement(responseText).jsonObject
                val parsed = obj["options"]?.jsonArray?.mapNotNull { el ->
                    val o = el.jsonObject
                    val title = o["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val description = o["description"]?.jsonPrimitive?.contentOrNull ?: ""
                    val focus = o["focus"]?.jsonPrimitive?.contentOrNull ?: ""
                    val category = o["category"]?.jsonPrimitive?.contentOrNull
                        ?.let { name -> GoalCategory.entries.find { it.name == name } } ?: detectedCategory
                    val timeline = o["timeline"]?.jsonPrimitive?.contentOrNull
                        ?.let { name -> GoalTimeline.entries.find { it.name == name } } ?: GoalTimeline.MID_TERM
                    val milestones = o["milestones"]?.jsonArray
                        ?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                    val reasoning = o["reasoning"]?.jsonPrimitive?.contentOrNull ?: ""
                    GoalOption(focus, title, description, category, timeline, milestones, reasoning)
                } ?: emptyList()

                aiGoalOptions = parsed.take(3)
                step = GoalWizardStep.SELECTION
            } catch (_: Exception) {
                generationError = "Couldn't generate goal. Check your connection and try again."
                step = GoalWizardStep.QUESTIONS
            }
        }
    }

    fun selectGoalOption(option: GoalOption) {
        goalTitle = option.title
        goalDescription = option.description
        goalCategory = option.category
        goalTimeline = option.timeline
        aiReasoning = option.reasoning
        aiMilestones.clear()
        option.milestones.forEach { aiMilestones.add(it to true) }
        councilNotes = buildCouncilNotes(userSituation, option, option.category)
        step = GoalWizardStep.DETAILS
    }

    @Suppress("NAME_SHADOWING")
    fun createGoal() {
        val milestones = aiMilestones.filter { it.second }.map { it.first } +
                customMilestones.filter { it.isNotBlank() }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val goalId = Uuid.random().toString()
        val goal = Goal(
            id = goalId,
            category = goalCategory,
            title = goalTitle.trim(),
            description = goalDescription.trim(),
            status = GoalStatus.NOT_STARTED,
            timeline = goalTimeline,
            dueDate = goalDueDate,
            progress = 0,
            milestones = milestones.map { title ->
                Milestone(
                    id = Uuid.random().toString(),
                    title = title.trim(),
                    dueDate = null,
                    isCompleted = false
                )
            },
            notes = "",
            createdAt = now,
            completionRate = 0f,
            isArchived = false,
            aiReasoning = if (isAiPath) aiReasoning else null
        )
        viewModel.createGoal(goal)
        onGoalCreated(goalId)
    }

    val progress = when (step) {
        GoalWizardStep.INTENT -> 0.08f
        GoalWizardStep.QUESTIONS -> 0.25f
        GoalWizardStep.GENERATING -> 0.45f
        GoalWizardStep.SELECTION -> 0.55f
        GoalWizardStep.DETAILS -> 0.68f
        GoalWizardStep.TIMELINE -> 0.82f
        GoalWizardStep.MILESTONES -> 1f
    }
    val stepLabel = when (step) {
        GoalWizardStep.INTENT -> "New Goal"
        GoalWizardStep.QUESTIONS -> "Tell us more"
        GoalWizardStep.GENERATING -> "AI magic"
        GoalWizardStep.SELECTION -> "Choose your path"
        GoalWizardStep.DETAILS -> "Details"
        GoalWizardStep.TIMELINE -> "Timeline"
        GoalWizardStep.MILESTONES -> "Milestones"
    }

    Scaffold(
        topBar = {
            WizardTopBar(
                stepLabel = stepLabel,
                progress = progress,
                showBack = step != GoalWizardStep.GENERATING,
                onBackClick = { goBack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { currentStep ->
            when (currentStep) {
                GoalWizardStep.INTENT -> IntentStep(
                    intentText = intentText,
                    onIntentChange = { intentText = it },
                    detectedCategory = detectedCategory,
                    error = generationError,
                    isGeneratingQuestions = isGeneratingQuestions,
                    onGenerateClick = { goToQuestions() },
                    onManualClick = { proceedManually() }
                )

                GoalWizardStep.QUESTIONS -> QuestionsStep(
                    questions = aiGeneratedQuestions,
                    answers = questionAnswers,
                    onAnswerToggle = { idx, answer ->
                        val current = questionAnswers.getOrElse(idx) { emptyList() }
                        questionAnswers[idx] = if (answer in current) current - answer else current + answer
                    },
                    error = generationError,
                    onContinue = { generateWithAi() }
                )

                GoalWizardStep.GENERATING -> GeneratingStep(category = detectedCategory)

                GoalWizardStep.SELECTION -> SelectionStep(
                    options = aiGoalOptions,
                    councilNotes = councilNotes,
                    onSelect = { selectGoalOption(it) }
                )

                GoalWizardStep.DETAILS -> DetailsStep(
                    isAiPath = isAiPath,
                    goalTitle = goalTitle,
                    onTitleChange = { goalTitle = it },
                    goalDescription = goalDescription,
                    onDescriptionChange = { goalDescription = it },
                    goalCategory = goalCategory,
                    onCategoryChange = { goalCategory = it },
                    canProceed = goalTitle.isNotBlank() && goalDescription.isNotBlank(),
                    onNext = { step = GoalWizardStep.TIMELINE }
                )

                GoalWizardStep.TIMELINE -> TimelineStep(
                    selectedTimeline = goalTimeline,
                    onTimelineSelect = { goalTimeline = it },
                    dueDate = goalDueDate,
                    onDatePickerClick = { showDatePicker = true },
                    onNext = { step = GoalWizardStep.MILESTONES }
                )

                GoalWizardStep.MILESTONES -> MilestonesStep(
                    isAiPath = isAiPath,
                    aiMilestones = aiMilestones,
                    onToggleAiMilestone = { idx ->
                        aiMilestones[idx] = aiMilestones[idx].copy(second = !aiMilestones[idx].second)
                    },
                    customMilestones = customMilestones,
                    onRemoveCustom = { customMilestones.removeAt(it) },
                    customInput = customMilestoneInput,
                    onCustomInputChange = { customMilestoneInput = it },
                    onAddCustom = {
                        if (customMilestoneInput.isNotBlank()) {
                            customMilestones.add(customMilestoneInput.trim())
                            customMilestoneInput = ""
                        }
                    },
                    onCreateGoal = { createGoal() }
                )
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            goalDueDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

internal fun buildCouncilNotes(
    situation: UserSituation?,
    option: GoalOption,
    category: GoalCategory
): List<Pair<String, String>> {
    if (situation == null) return emptyList()
    val notes = mutableListOf<Pair<String, String>>()
    val body = situation.body
    val meta = situation.meta
    val lowerTitle = option.title.lowercase()

    if ((body.sleepHours != null && body.sleepHours < 6f) || (body.energyRating != null && body.energyRating < 5)) {
        notes.add("Kai 💪" to "Your energy and sleep are limited right now. I'd start at 60% of whatever pace feels right — recovery is how you win long-term.")
    }
    if (category == GoalCategory.CAREER && (meta.stressLevel != null && meta.stressLevel >= 7)) {
        notes.add("Luna ✨" to "Your stress is high. Let's pace this goal so it doesn't add to your load — sustainable > aggressive.")
    }
    if (category != GoalCategory.MONEY && (lowerTitle.contains("promot") || lowerTitle.contains("job") || lowerTitle.contains("career") || lowerTitle.contains("salary"))) {
        notes.add("Morgan 💰" to "A career move often comes with a pay jump. Want me to open a parallel money goal to capture that?")
    }
    if (lowerTitle.contains("network") || lowerTitle.contains("speak") || lowerTitle.contains("outreach") || lowerTitle.contains("connect")) {
        notes.add("Sam 🤝" to "This goal needs people. I can help you build a relationship strategy that doesn't feel forced.")
    }
    if (situation.purpose.topValues.isNotEmpty() && meta.stressLevel != null && meta.stressLevel >= 8) {
        notes.add("River 🧘" to "High stress + an ambitious goal is a tricky combo. Check your values: does this goal feed or drain you?")
    }
    return notes.take(2)
}
