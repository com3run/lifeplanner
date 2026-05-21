package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.ui.auth.AuthBottomSheet
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.signInAsGuest
import az.tribe.lifeplanner.util.PlatformBackHandler
import com.russhwolf.settings.Settings
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private data class ChatMessage(
    val isCoach: Boolean, val text: String,
    val coachName: String = "Luna", val coachEmoji: String = "🌟"
)

private fun OnboardingPhase.isBefore(other: OnboardingPhase) = this.ordinal < other.ordinal

@Composable
private fun rememberChatHistory(
    phase: OnboardingPhase,
    vm: CoachOnboardingViewModel
): List<ChatMessage> {
    return remember(phase, vm.userName, vm.topPriorities, vm.mindDump,
        vm.stressLevel, vm.sleepQuality, vm.topPriority,
        vm.analysisQuestions, vm.analysisAnswers, vm.currentQuestionIndex) {
        buildChatHistory(phase, vm)
    }
}

private fun buildChatHistory(phase: OnboardingPhase, vm: CoachOnboardingViewModel): List<ChatMessage> {
    val messages = mutableListOf<ChatMessage>()

    fun coachMsg(text: String, coachName: String = "Luna", coachEmoji: String = "🌟") =
        messages.add(ChatMessage(isCoach = true, text = text, coachName = coachName, coachEmoji = coachEmoji))
    fun userMsg(text: String) =
        messages.add(ChatMessage(isCoach = false, text = text))

    val luna = "🌟"
    val specialistEmoji = when (vm.specialistCoachId) {
        "alex_career" -> "💼"
        "morgan_finance" -> "💰"
        "kai_fitness" -> "💪"
        "sam_social" -> "👥"
        "river_wellness" -> "🧘"
        "jamie_family" -> "🏡"
        else -> "🌟"
    }
    val specialistName = runCatching { CoachPersona.getById(vm.specialistCoachId).name }.getOrElse { "Your coach" }

    if (OnboardingPhase.LUNA_INTRO.isBefore(phase)) {
        coachMsg("Hi, I'm Luna — your life coach! Before we dive in, I'd love to get to know you a little. It'll only take 2 minutes.", "Luna", luna)
        userMsg("Let's do it!")
    }
    if (OnboardingPhase.LUNA_NAME.isBefore(phase)) {
        coachMsg("Let's start with the basics. What's your name, and how old are you?", "Luna", luna)
        val nameAge = buildString {
            if (vm.userName.isNotBlank()) append(vm.userName)
            if (vm.userAge != null) {
                if (isNotEmpty()) append(", ")
                append("${vm.userAge}")
            }
        }
        userMsg(if (nameAge.isNotBlank()) nameAge else "Skipped")
    }
    if (OnboardingPhase.LUNA_PRIORITY.isBefore(phase)) {
        coachMsg("Which areas of life matter most to you right now? Select at least 3.", "Luna", luna)
        val cats = vm.topPriorities.joinToString(" · ") { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        userMsg(if (cats.isNotBlank()) cats else "Skipped")
    }
    if (OnboardingPhase.LUNA_WELLBEING.isBefore(phase)) {
        coachMsg("How are you feeling lately? Be honest — this helps me calibrate your goals.", "Luna", luna)
        userMsg("Stress: ${vm.stressLevel}/10 · Sleep: ${vm.sleepQuality}/10")
    }
    if (OnboardingPhase.SPECIALIST_INTRO.isBefore(phase)) {
        val area = vm.topPriorities.firstOrNull()?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            ?: vm.topPriority?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            ?: "your goals"
        coachMsg("I'm bringing in $specialistName — our $area specialist.", "Luna", luna)
        userMsg("Let's meet them!")
    }
    if (OnboardingPhase.SPECIALIST_Q1.isBefore(phase)) {
        coachMsg(specialistQSummary(vm, 1), specialistName, specialistEmoji); userMsg(vm.specialistQ1Answer())
    }
    if (OnboardingPhase.SPECIALIST_Q2.isBefore(phase)) {
        coachMsg(specialistQSummary(vm, 2), specialistName, specialistEmoji); userMsg(vm.specialistQ2Answer())
    }
    if (OnboardingPhase.SPECIALIST_Q3.isBefore(phase)) {
        coachMsg(specialistQSummary(vm, 3), specialistName, specialistEmoji); userMsg(vm.specialistQ3Answer())
    }
    if (OnboardingPhase.SPECIALIST_Q4.isBefore(phase)) {
        coachMsg(specialistQSummary(vm, 4), specialistName, specialistEmoji); userMsg(vm.specialistQ4Answer())
    }
    if (OnboardingPhase.MIND_DUMP.isBefore(phase)) {
        coachMsg("What do you actually want to change or achieve right now?", "Luna", luna)
        val dump = vm.mindDump.trim()
        userMsg(if (dump.isNotBlank()) dump.take(60) + if (dump.length > 60) "…" else "" else "Skipped")
    }
    // Show answered clarifying questions in chat history
    if (OnboardingPhase.MIND_QUESTIONS.isBefore(phase) || phase == OnboardingPhase.MIND_VALIDATION) {
        vm.analysisQuestions.forEachIndexed { idx, question ->
            coachMsg(question, "Luna", luna)
            val answer = vm.analysisAnswers.getOrNull(idx)
            if (!answer.isNullOrBlank()) userMsg(answer)
        }
    } else if (phase == OnboardingPhase.MIND_QUESTIONS && vm.currentQuestionIndex > 0) {
        // Show already-answered questions while still on MIND_QUESTIONS
        for (idx in 0 until vm.currentQuestionIndex) {
            val q = vm.analysisQuestions.getOrNull(idx) ?: continue
            val a = vm.analysisAnswers.getOrNull(idx)
            coachMsg(q, "Luna", luna)
            if (!a.isNullOrBlank()) userMsg(a)
        }
    }

    return messages
}

private fun specialistQSummary(vm: CoachOnboardingViewModel, q: Int): String {
    val id = vm.specialistCoachId
    return when (q) {
        1 -> when (id) {
            "alex_career" -> "What's your work situation?"
            "morgan_finance" -> "What's your rough income range?"
            "kai_fitness" -> "How active are you day-to-day?"
            "sam_social" -> "Are you more of an introvert or extrovert?"
            "river_wellness" -> "What are your top values?"
            "jamie_family" -> "What best describes your family situation?"
            else -> "Quick question"
        }
        2 -> when (id) {
            "alex_career" -> "What's your current role?"
            "morgan_finance" -> "How consistent are your savings?"
            "kai_fitness" -> "How many hours do you sleep?"
            "sam_social" -> "How big is your close circle?"
            "river_wellness" -> "Do you have a mindfulness practice?"
            "jamie_family" -> "What's your biggest family challenge?"
            else -> ""
        }
        3 -> when (id) {
            "alex_career" -> "Years of experience?"
            "morgan_finance" -> "Any debt you're managing?"
            "kai_fitness" -> "How's your energy level?"
            "sam_social" -> "What's your relationship status?"
            "river_wellness" -> "What's your long-term vision?"
            "jamie_family" -> "What would a happier home look like?"
            else -> ""
        }
        else -> when (id) {
            "alex_career" -> "What's your main career ambition?"
            "morgan_finance" -> "What's your main financial goal?"
            else -> ""
        }
    }
}

private fun CoachOnboardingViewModel.specialistQ1Answer() = when (specialistCoachId) {
    "alex_career" -> employmentStatus?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "morgan_finance" -> incomeBand?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "kai_fitness" -> activityLevel?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "sam_social" -> socialEnergy?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "river_wellness" -> if (topValues.isNotEmpty()) topValues.joinToString(", ") else "Skipped"
    "jamie_family" -> familyRole.takeIf { it.isNotBlank() } ?: "Skipped"
    else -> "Done"
}

private fun CoachOnboardingViewModel.specialistQ2Answer() = when (specialistCoachId) {
    "alex_career" -> jobRole.takeIf { it.isNotBlank() } ?: "Skipped"
    "morgan_finance" -> savingsHabit?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "kai_fitness" -> "${sleepHours.toInt()} hrs"
    "sam_social" -> closeCircleSize?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "river_wellness" -> mindfulnessPractice?.let { if (it) "Yes" else "Not yet" } ?: "Skipped"
    "jamie_family" -> familyChallenge.takeIf { it.isNotBlank() }?.take(60) ?: "Skipped"
    else -> "Done"
}

private fun CoachOnboardingViewModel.specialistQ3Answer() = when (specialistCoachId) {
    "alex_career" -> yearsExperience?.let { "$it years" } ?: "Skipped"
    "morgan_finance" -> hasDebt?.let { if (it) "Has debt" else "No debt" } ?: "Skipped"
    "kai_fitness" -> "Energy: $energyRating/10"
    "sam_social" -> relationshipStatus?.name?.lowercase()?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Skipped"
    "river_wellness" -> longTermVision.takeIf { it.isNotBlank() }?.take(60) ?: "Skipped"
    "jamie_family" -> familyVision.takeIf { it.isNotBlank() }?.take(60) ?: "Skipped"
    else -> "Done"
}

private fun CoachOnboardingViewModel.specialistQ4Answer() = when (specialistCoachId) {
    "alex_career" -> careerGoal.takeIf { it.isNotBlank() }?.take(60) ?: "Skipped"
    "morgan_finance" -> financialGoal.takeIf { it.isNotBlank() }?.take(60) ?: "Skipped"
    else -> "Done"
}

// ─── Main screen ──────────────────────────────────────────────────────────────

@Composable
fun CoachOnboardingScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: CoachOnboardingViewModel = koinViewModel()
) {
    val authViewModel: AuthViewModel = koinInject()
    val authState by authViewModel.authState.collectAsState()
    val settings: Settings = koinInject()

    val phase by viewModel.phase.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var showSignInSheet by remember { mutableStateOf(false) }
    var isLoadingGuest by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated, is AuthState.Guest -> {
                val freshGuestAuth = isLoadingGuest
                isLoadingGuest = false
                if (freshGuestAuth) {
                    viewModel.resetForNewSession()
                } else if (CoachOnboardingViewModel.isComplete(settings)) {
                    onComplete()
                }
            }
            is AuthState.Error, is AuthState.Unauthenticated -> isLoadingGuest = false
            else -> {}
        }
    }

    // ── Auth gate ─────────────────────────────────────────────────────────────
    if (authState is AuthState.Unauthenticated || authState is AuthState.Error) {
        AuthGate(
            isLoading = isLoadingGuest,
            onGetStarted = {
                Analytics.signUpStarted("guest")
                isLoadingGuest = true
                authViewModel.signInAsGuest()
            },
            onSignIn = { showSignInSheet = true }
        )
        if (showSignInSheet) {
            AuthBottomSheet(
                isSignUp = false,
                authViewModel = authViewModel,
                authState = authState,
                onDismiss = { showSignInSheet = false },
                onSuccess = { showSignInSheet = false }
            )
        }
        return
    }

    if (authState is AuthState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ── Normal onboarding flow ────────────────────────────────────────────────

    // Luna video intro replaces the chat scaffold entirely for the first phase
    if (phase == OnboardingPhase.LUNA_INTRO) {
        LunaVideoIntro(onContinue = { viewModel.advance() })
        return
    }

    val completeness = viewModel.overallCompleteness()

    PlatformBackHandler(enabled = true) { viewModel.back() }

    val chatHistory = rememberChatHistory(phase, viewModel)
    val listState = rememberLazyListState()

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            ChatTopBar(
                completeness = completeness,
                showBack = true,
                onBack = { viewModel.back() }
            )

            // Chat history
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(chatHistory) { msg ->
                    if (msg.isCoach) {
                        CoachChatBubble(
                            text = msg.text,
                            coachName = msg.coachName,
                            coachEmoji = msg.coachEmoji
                        )
                    } else {
                        UserChatBubble(text = msg.text)
                    }
                }
            }

            // Bottom dock with current step
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                tonalElevation = 2.dp
            ) {
                AnimatedContent(
                    targetState = phase,
                    transitionSpec = {
                        (slideInVertically { it / 4 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 4 } + fadeOut())
                    }
                ) { currentPhase ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = phaseMessage(currentPhase, viewModel),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        PhaseContent(
                            phase = currentPhase,
                            viewModel = viewModel,
                            onAdvance = {
                                when (currentPhase) {
                                    OnboardingPhase.COMPLETE -> viewModel.completeOnboarding(onComplete)
                                    OnboardingPhase.HABIT_SUGGEST -> viewModel.finishOnboarding(onComplete)
                                    else -> viewModel.advance()
                                }
                            },
                            onSkipToHome = { viewModel.finishOnboarding(onComplete) },
                            onConfirmInterpretation = { viewModel.confirmInterpretation(onComplete) },
                            isAnalyzing = isAnalyzing,
                            isSaving = isSaving
                        )
                    }
                }
            }
        }
    }
}

// ─── Chat bubbles ─────────────────────────────────────────────────────────────

@Composable
private fun CoachChatBubble(text: String, coachName: String, coachEmoji: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = coachEmoji, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = coachName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(40.dp))
    }
}

@Composable
private fun UserChatBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(40.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
private fun ChatTopBar(
    completeness: Float,
    showBack: Boolean,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Text(
                text = "Life Planner",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        LinearProgressIndicator(
            progress = { completeness },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ─── Auth gate ────────────────────────────────────────────────────────────────

@Composable
private fun AuthGate(
    isLoading: Boolean,
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Life Planner",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Goals. Habits. Life.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { if (!isLoading) onGetStarted() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedButton(
                onClick = onSignIn,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Sign In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
