package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.ui.theme.modernColors
import az.tribe.lifeplanner.util.NetworkConnectivityObserver
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Life scenarios for quick selection
data class LifeScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val prompt: String,
    val gradientColors: List<Color>,
    val suggestedCategories: List<GoalCategory>
)

val lifeScenarios = listOf(
    LifeScenario(
        id = "new_year",
        title = "New Year, New Me",
        subtitle = "Fresh start goals",
        icon = "🎯",
        prompt = "I want to start the new year with positive changes in my health, career, and personal growth",
        gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
        suggestedCategories = listOf(GoalCategory.BODY, GoalCategory.CAREER, GoalCategory.WELLBEING)
    ),
    LifeScenario(
        id = "career_growth",
        title = "Level Up Career",
        subtitle = "Professional growth",
        icon = "🚀",
        prompt = "I want to advance my career, learn new skills, and increase my income",
        gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
        suggestedCategories = listOf(GoalCategory.CAREER, GoalCategory.MONEY)
    ),
    LifeScenario(
        id = "wedding_prep",
        title = "Wedding Ready",
        subtitle = "Big day preparation",
        icon = "💒",
        prompt = "I'm getting married and want to look my best, save money, and reduce stress",
        gradientColors = listOf(Color(0xFFFC5C7D), Color(0xFF6A82FB)),
        suggestedCategories = listOf(GoalCategory.BODY, GoalCategory.MONEY, GoalCategory.WELLBEING)
    ),
    LifeScenario(
        id = "financial_freedom",
        title = "Financial Freedom",
        subtitle = "Money mastery",
        icon = "💰",
        prompt = "I want to get out of debt, build savings, and create financial security",
        gradientColors = listOf(Color(0xFFF7971E), Color(0xFFFFD200)),
        suggestedCategories = listOf(GoalCategory.MONEY, GoalCategory.CAREER)
    ),
    LifeScenario(
        id = "health_transformation",
        title = "Health Transformation",
        subtitle = "Mind & body wellness",
        icon = "💪",
        prompt = "I want to lose weight, build strength, improve my diet, and feel more energetic",
        gradientColors = listOf(Color(0xFFED213A), Color(0xFF93291E)),
        suggestedCategories = listOf(GoalCategory.BODY, GoalCategory.WELLBEING)
    ),
    LifeScenario(
        id = "work_life_balance",
        title = "Better Balance",
        subtitle = "Life harmony",
        icon = "⚖️",
        prompt = "I want to reduce stress, spend more time with family, and find better work-life balance",
        gradientColors = listOf(Color(0xFF4776E6), Color(0xFF8E54E9)),
        suggestedCategories = listOf(GoalCategory.PEOPLE, GoalCategory.WELLBEING, GoalCategory.PURPOSE)
    )
)

enum class GeneratorStep {
    SCENARIO_SELECT, CUSTOM_INPUT, QUESTIONS, GENERATING, RESULTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartGoalGeneratorScreen(
    viewModel: GoalViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(GeneratorStep.SCENARIO_SELECT) }
    var customPrompt by remember { mutableStateOf("") }
    val connectivityObserver: NetworkConnectivityObserver = koinInject()
    val isConnected by connectivityObserver.isConnected.collectAsStateWithLifecycle()
    val isOffline = !isConnected

    val generatedGoals by viewModel.generatedGoalsFromAI.collectAsStateWithLifecycle()
    val questionnaireStep by viewModel.questionnaireStep.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val isLoadingQuestions by viewModel.isLoadingQuestions.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Reset when screen opens
    LaunchedEffect(Unit) {
        viewModel.resetQuestionnaire()
    }

    // Watch ViewModel state for transitions
    LaunchedEffect(questionnaireStep) {
        when (questionnaireStep) {
            QuestionnaireStep.ANSWERING -> {
                currentStep = GeneratorStep.QUESTIONS
            }
            QuestionnaireStep.GENERATING -> {
                currentStep = GeneratorStep.GENERATING
            }
            QuestionnaireStep.RESULTS -> {
                currentStep = GeneratorStep.RESULTS
            }
            QuestionnaireStep.INPUT -> {
                // Error happened, go back to selection
                if (currentStep == GeneratorStep.GENERATING || currentStep == GeneratorStep.QUESTIONS) {
                    currentStep = GeneratorStep.SCENARIO_SELECT
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error as snackbar
    LaunchedEffect(error) {
        val msg = error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmartGeneratorTopBar(
                currentStep = currentStep,
                onBackClick = {
                    when (currentStep) {
                        GeneratorStep.SCENARIO_SELECT -> onBackClick()
                        GeneratorStep.CUSTOM_INPUT -> currentStep = GeneratorStep.SCENARIO_SELECT
                        GeneratorStep.QUESTIONS -> currentStep = GeneratorStep.SCENARIO_SELECT
                        GeneratorStep.GENERATING -> {} // Can't go back during generation
                        GeneratorStep.RESULTS -> {
                            viewModel.resetQuestionnaire()
                            currentStep = GeneratorStep.SCENARIO_SELECT
                        }
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { step ->
            when (step) {
                GeneratorStep.SCENARIO_SELECT -> ScenarioSelectStep(
                    scenarios = lifeScenarios,
                    isOffline = isOffline,
                    onScenarioSelected = { scenario ->
                        val categoriesText = scenario.suggestedCategories.joinToString(", ") { it.name.lowercase() }
                        customPrompt = "${scenario.prompt}. Focus areas: $categoriesText"
                        currentStep = GeneratorStep.GENERATING
                        viewModel.generateQuestionnaire(customPrompt)
                    },
                    onCustomClick = {
                        currentStep = GeneratorStep.CUSTOM_INPUT
                    }
                )

                GeneratorStep.CUSTOM_INPUT -> CustomInputStep(
                    prompt = customPrompt,
                    isOffline = isOffline,
                    onPromptChange = { customPrompt = it },
                    onGenerate = {
                        currentStep = GeneratorStep.GENERATING
                        viewModel.generateQuestionnaire(customPrompt)
                    }
                )

                GeneratorStep.QUESTIONS -> QuestionsStep(
                    questions = questions,
                    onAnswerQuestion = { questionTitle, answer ->
                        viewModel.answerQuestion(questionTitle, answer)
                    },
                    isQuestionnaireComplete = viewModel.isQuestionnaireComplete(),
                    onSubmitAnswers = {
                        viewModel.generatePersonalizedGoals()
                    }
                )

                GeneratorStep.GENERATING -> GeneratingStep()

                GeneratorStep.RESULTS -> ResultsStep(
                    goals = generatedGoals,
                    onAddGoal = { goal ->
                        viewModel.addGeneratedGoalToList(goal)
                    },
                    onAddAll = {
                        viewModel.addAllGeneratedGoalsToList()
                    },
                    onComplete = onComplete
                )
            }
        }
    }
}
