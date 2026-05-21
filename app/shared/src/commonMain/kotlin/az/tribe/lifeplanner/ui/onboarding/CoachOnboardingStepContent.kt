package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.ActivityLevel
import az.tribe.lifeplanner.domain.model.CircleSize
import az.tribe.lifeplanner.domain.model.RelationshipStatus
import az.tribe.lifeplanner.domain.model.SavingsHabit
import az.tribe.lifeplanner.domain.model.SocialEnergy

// ─── Phase routing ────────────────────────────────────────────────────────────

@Composable
internal fun PhaseContent(
    phase: OnboardingPhase,
    viewModel: CoachOnboardingViewModel,
    onAdvance: () -> Unit,
    onSkipToHome: () -> Unit = {},
    onConfirmInterpretation: () -> Unit = {},
    isAnalyzing: Boolean = false,
    isSaving: Boolean
) {
    when (phase) {
        OnboardingPhase.LUNA_INTRO -> IntroStep(onAdvance)

        OnboardingPhase.LUNA_NAME -> NameAgeStep(
            name = viewModel.userName,
            age = viewModel.userAge,
            onNameChange = { viewModel.userName = it },
            onAgeChange = { viewModel.userAge = it },
            onContinue = onAdvance
        )

        OnboardingPhase.LUNA_PRIORITY -> PriorityStep(
            selected = viewModel.topPriorities,
            onToggle = { viewModel.togglePriority(it) },
            onContinue = onAdvance
        )

        OnboardingPhase.LUNA_WELLBEING -> WellbeingStep(
            stress = viewModel.stressLevel,
            sleep = viewModel.sleepQuality,
            onStressChange = { viewModel.stressLevel = it },
            onSleepChange = { viewModel.sleepQuality = it },
            onContinue = onAdvance
        )

        OnboardingPhase.SPECIALIST_INTRO -> SpecialistIntroStep(
            coachId = viewModel.specialistCoachId,
            priority = viewModel.topPriority,
            onContinue = onAdvance
        )

        OnboardingPhase.SPECIALIST_Q1 -> SpecialistQ1(viewModel, onAdvance)
        OnboardingPhase.SPECIALIST_Q2 -> SpecialistQ2(viewModel, onAdvance)
        OnboardingPhase.SPECIALIST_Q3 -> SpecialistQ3(viewModel, onAdvance)
        OnboardingPhase.SPECIALIST_Q4 -> SpecialistQ4(viewModel, onAdvance)

        OnboardingPhase.MIND_DUMP -> MindDumpStep(
            value = viewModel.mindDump,
            onChange = { viewModel.mindDump = it },
            onContinue = { viewModel.startMindAnalysis() },
            onSkip = { viewModel.skipMindDump() },
            isLoading = isAnalyzing
        )

        OnboardingPhase.MIND_QUESTIONS -> MindQuestionsStep(
            questions = viewModel.analysisQuestions,
            answers = viewModel.analysisAnswers,
            currentIndex = viewModel.currentQuestionIndex,
            isProcessing = isAnalyzing,
            onAnswer = { viewModel.answerCurrentQuestion(it) }
        )

        OnboardingPhase.MIND_VALIDATION -> MindValidationStep(
            interpretation = viewModel.goalInterpretation,
            qaContext = viewModel.analysisQuestions.zip(viewModel.analysisAnswers)
                .filter { (_, a) -> a.isNotBlank() },
            isProcessing = isSaving,
            onConfirm = onConfirmInterpretation,
            onReject = { viewModel.rejectInterpretation() }
        )

        OnboardingPhase.COMPLETE -> CompleteStep(
            completeness = viewModel.overallCompleteness(),
            onDone = onAdvance,
            isSaving = isSaving
        )

        OnboardingPhase.GOAL_PREVIEW -> GoalPreviewStep(
            goal = viewModel.generatedGoal,
            onContinue = { viewModel.advance() },
            onSkip = onSkipToHome
        )

        OnboardingPhase.HABIT_SUGGEST -> HabitSuggestStep(
            habits = viewModel.habitSuggestions,
            onToggle = { viewModel.toggleHabitSuggestion(it) },
            onContinue = onAdvance,
            isSaving = isSaving
        )
    }
}

// ─── Luna steps ───────────────────────────────────────────────────────────────

@Composable
private fun IntroStep(onContinue: () -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))
    PrimaryButton(text = "Let's do it!", onClick = onContinue)
}

@Composable
private fun NameAgeStep(
    name: String,
    age: Int?,
    onNameChange: (String) -> Unit,
    onAgeChange: (Int?) -> Unit,
    onContinue: () -> Unit
) {
    var ageText by remember(age) { mutableStateOf(age?.toString() ?: "") }

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Your first name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = ageText,
        onValueChange = { v ->
            ageText = v
            onAgeChange(v.toIntOrNull())
        },
        label = { Text("Your age") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(text = "Continue", onClick = onContinue, enabled = name.isNotBlank())
    TextButton(onClick = onContinue) { Text("Skip for now") }
}

@Composable
private fun PriorityStep(
    selected: List<GoalCategory>,
    onToggle: (GoalCategory) -> Unit,
    onContinue: () -> Unit
) {
    val options = listOf(
        GoalCategory.CAREER to ("💼" to "Career"),
        GoalCategory.MONEY to ("💰" to "Money"),
        GoalCategory.BODY to ("💪" to "Body"),
        GoalCategory.PEOPLE to ("👥" to "People"),
        GoalCategory.WELLBEING to ("🧘" to "Wellbeing"),
        GoalCategory.PURPOSE to ("🎯" to "Purpose"),
        GoalCategory.FAMILY to ("🏡" to "Family")
    )

    @Composable
    fun CategoryTile(category: GoalCategory, emoji: String, label: String, modifier: Modifier) {
        val isSelected = category in selected
        Surface(
            modifier = modifier.clickable { onToggle(category) },
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (isSelected) 4.dp else 0.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Row 1: 4 items
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.take(4).forEach { (category, pair) ->
            CategoryTile(category = category, emoji = pair.first, label = pair.second, modifier = Modifier.weight(1f))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    // Row 2: 3 items — center them by adding spacers
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        options.drop(4).forEach { (category, pair) ->
            CategoryTile(category = category, emoji = pair.first, label = pair.second, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.weight(0.5f))
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "${selected.size} of 7 selected · 3 minimum",
        style = MaterialTheme.typography.labelMedium,
        color = if (selected.size >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    PrimaryButton(text = "Continue", onClick = onContinue, enabled = selected.size >= 3)
}

@Composable
private fun WellbeingStep(
    stress: Int,
    sleep: Int,
    onStressChange: (Int) -> Unit,
    onSleepChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    LabeledSlider(label = "Stress level: $stress / 10", value = stress, onValueChange = onStressChange)
    Spacer(modifier = Modifier.height(20.dp))
    LabeledSlider(label = "Sleep quality: $sleep / 10", value = sleep, onValueChange = onSleepChange)
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(text = "Continue", onClick = onContinue)
}

@Composable
private fun SpecialistIntroStep(coachId: String, priority: GoalCategory?, onContinue: () -> Unit) {
    val area = priority?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "your goals"
    Text(
        text = "They'll ask you a few quick questions about $area — 5 max, totally safe to skip any.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(text = "Meet them!", onClick = onContinue)
}

@Composable
private fun MindDumpStep(
    value: String,
    onChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit = onContinue,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("What's on your mind?") },
        placeholder = { Text("e.g. I want to get fit, change jobs, stress less...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 3,
        maxLines = 6
    )
    Spacer(Modifier.height(24.dp))
    PrimaryButton(
        text = if (isLoading) "Thinking…" else "Let's build it!",
        onClick = onContinue,
        enabled = value.isNotBlank() && !isLoading
    )
    TextButton(onClick = onSkip, enabled = !isLoading) { Text("Skip for now") }
}

@Composable
private fun CompleteStep(completeness: Float, onDone: () -> Unit, isSaving: Boolean) {
    Text(
        text = "Your profile is ${(completeness * 100).toInt()}% complete",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Your coaches will now personalize everything for you. You can always update your profile later.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onDone,
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text("Let's start!", fontWeight = FontWeight.SemiBold)
    }
}

// ─── Specialist questions ──────────────────────────────────────────────────────

@Composable
private fun SpecialistQ1(vm: CoachOnboardingViewModel, onAdvance: () -> Unit) {
    when (vm.specialistCoachId) {
        "alex_career" -> ChipQuestion(
            options = employmentStatusOptions(vm.userAge ?: 25),
            selected = vm.employmentStatus?.name,
            onSelect = { label -> vm.employmentStatus = employmentStatusFromLabel(label) },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "morgan_finance" -> ChipQuestion(
            options = incomeBandOptions(),
            selected = vm.incomeBand?.name,
            onSelect = { label -> vm.incomeBand = incomeBandFromLabel(label) },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "kai_fitness" -> ChipQuestion(
            options = activityLevelOptions(),
            selected = vm.activityLevel?.name,
            onSelect = { label -> vm.activityLevel = activityLevelFromLabel(label) },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "sam_social" -> ChipQuestion(
            options = listOf("Introvert", "Ambivert", "Extrovert"),
            selected = vm.socialEnergy?.name,
            onSelect = { label ->
                vm.socialEnergy = when (label) {
                    "Introvert" -> SocialEnergy.INTROVERT
                    "Ambivert" -> SocialEnergy.AMBIVERT
                    else -> SocialEnergy.EXTROVERT
                }
            },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "river_wellness" -> ValuesPickerStep(
            selected = vm.topValues,
            onDone = { vm.topValues = it; onAdvance() },
            onSkip = onAdvance
        )
        "jamie_family" -> ChipQuestion(
            options = listOf(
                "Parent of young kids", "Parent of teens", "Empty nester",
                "Caring for parents", "Single, no kids yet", "Other"
            ),
            selected = vm.familyRole.takeIf { it.isNotBlank() },
            onSelect = { vm.familyRole = it },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        else -> TextInputStep(
            label = "Your main goal",
            value = vm.careerGoal,
            onChange = { vm.careerGoal = it },
            onContinue = onAdvance
        )
    }
}

@Composable
private fun SpecialistQ2(vm: CoachOnboardingViewModel, onAdvance: () -> Unit) {
    when (vm.specialistCoachId) {
        "alex_career" -> TextInputStep(
            label = ageBandedRoleLabel(vm.userAge ?: 25),
            value = vm.jobRole,
            onChange = { vm.jobRole = it },
            onContinue = onAdvance,
            optional = true
        )
        "morgan_finance" -> ChipQuestion(
            options = listOf("None", "Sporadic", "Consistent", "Aggressive"),
            selected = vm.savingsHabit?.name,
            onSelect = { label ->
                vm.savingsHabit = when (label) {
                    "None" -> SavingsHabit.NONE
                    "Sporadic" -> SavingsHabit.SPORADIC
                    "Consistent" -> SavingsHabit.CONSISTENT
                    else -> SavingsHabit.AGGRESSIVE
                }
            },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "kai_fitness" -> {
            var hoursText by remember { mutableStateOf(vm.sleepHours.toInt().toString()) }
            OutlinedTextField(
                value = hoursText,
                onValueChange = { v -> hoursText = v; vm.sleepHours = v.toFloatOrNull() ?: vm.sleepHours },
                label = { Text("Hours of sleep per night") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Continue", onClick = onAdvance)
            TextButton(onClick = onAdvance) { Text("Skip") }
        }
        "sam_social" -> ChipQuestion(
            options = listOf("Solo", "Small (1–3)", "Medium (4–8)", "Large (9+)"),
            selected = vm.closeCircleSize?.name,
            onSelect = { label ->
                vm.closeCircleSize = when {
                    "Solo" in label -> CircleSize.SOLO
                    "Small" in label -> CircleSize.SMALL
                    "Medium" in label -> CircleSize.MEDIUM
                    else -> CircleSize.LARGE
                }
            },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "river_wellness" -> ChipQuestion(
            options = listOf("Yes, regularly", "Occasionally", "Not yet"),
            selected = vm.mindfulnessPractice?.let { if (it) "Yes, regularly" else "Not yet" },
            onSelect = { label -> vm.mindfulnessPractice = "Yes" in label || "Occas" in label },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "jamie_family" -> TextInputStep(
            label = "Main family challenge",
            value = vm.familyChallenge,
            onChange = { vm.familyChallenge = it },
            onContinue = onAdvance,
            optional = true
        )
        else -> onAdvance()
    }
}

@Composable
private fun SpecialistQ3(vm: CoachOnboardingViewModel, onAdvance: () -> Unit) {
    when (vm.specialistCoachId) {
        "alex_career" -> {
            var expText by remember { mutableStateOf(vm.yearsExperience?.toString() ?: "") }
            OutlinedTextField(
                value = expText,
                onValueChange = { v -> expText = v; vm.yearsExperience = v.toIntOrNull() },
                label = { Text("Years of experience (approx.)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Continue", onClick = onAdvance)
            TextButton(onClick = onAdvance) { Text("Skip") }
        }
        "morgan_finance" -> ChipQuestion(
            options = listOf("Yes, some debt", "No debt"),
            selected = vm.hasDebt?.let { if (it) "Yes, some debt" else "No debt" },
            onSelect = { label -> vm.hasDebt = "Yes" in label },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "kai_fitness" -> {
            LabeledSlider(
                label = "Energy level: ${vm.energyRating} / 10",
                value = vm.energyRating,
                onValueChange = { vm.energyRating = it }
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Continue", onClick = onAdvance)
        }
        "sam_social" -> ChipQuestion(
            options = listOf("Single", "In a relationship", "Married", "Other"),
            selected = vm.relationshipStatus?.name,
            onSelect = { label ->
                vm.relationshipStatus = when {
                    "Single" == label -> RelationshipStatus.SINGLE
                    "relationship" in label -> RelationshipStatus.IN_RELATIONSHIP
                    "Married" == label -> RelationshipStatus.MARRIED
                    else -> null
                }
            },
            onContinue = onAdvance,
            onSkip = onAdvance
        )
        "river_wellness" -> TextInputStep(
            label = "Your long-term vision (optional)",
            value = vm.longTermVision,
            onChange = { vm.longTermVision = it },
            onContinue = onAdvance,
            optional = true
        )
        "jamie_family" -> TextInputStep(
            label = "What success looks like (optional)",
            value = vm.familyVision,
            onChange = { vm.familyVision = it },
            onContinue = onAdvance,
            optional = true
        )
        else -> onAdvance()
    }
}

@Composable
private fun SpecialistQ4(vm: CoachOnboardingViewModel, onAdvance: () -> Unit) {
    when (vm.specialistCoachId) {
        "alex_career" -> TextInputStep(
            label = "What's your main career ambition?",
            value = vm.careerGoal,
            onChange = { vm.careerGoal = it },
            onContinue = onAdvance,
            optional = true
        )
        "morgan_finance" -> TextInputStep(
            label = "What's your main financial goal?",
            value = vm.financialGoal,
            onChange = { vm.financialGoal = it },
            onContinue = onAdvance,
            optional = true
        )
        else -> onAdvance()
    }
}

