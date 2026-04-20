package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.ActivityLevel
import az.tribe.lifeplanner.domain.model.CircleSize
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.EmploymentStatus
import az.tribe.lifeplanner.domain.model.IncomeBand
import az.tribe.lifeplanner.domain.model.RelationshipStatus
import az.tribe.lifeplanner.domain.model.SavingsHabit
import az.tribe.lifeplanner.domain.model.SocialEnergy
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoachOnboardingScreen(
    onComplete: () -> Unit,
    viewModel: CoachOnboardingViewModel = koinViewModel()
) {
    val phase by viewModel.phase.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val luna = CoachPersona.getById("luna_general")
    val specialist = runCatching { CoachPersona.getById(viewModel.specialistCoachId) }.getOrElse { luna }
    val activeCoach = if (phase >= OnboardingPhase.SPECIALIST_INTRO) specialist else luna

    val completeness = viewModel.overallCompleteness()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingProgressHeader(
                completeness = completeness,
                coachName = activeCoach.name,
                phase = phase
            )

            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                },
                modifier = Modifier.weight(1f)
            ) { currentPhase ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoachBubble(coach = activeCoach, message = phaseMessage(currentPhase, viewModel))
                    Spacer(modifier = Modifier.height(24.dp))
                    PhaseContent(
                        phase = currentPhase,
                        viewModel = viewModel,
                        onAdvance = {
                            if (currentPhase == OnboardingPhase.COMPLETE) {
                                viewModel.completeOnboarding(onComplete)
                            } else {
                                viewModel.advance()
                            }
                        },
                        isSaving = isSaving
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgressHeader(
    completeness: Float,
    coachName: String,
    phase: OnboardingPhase
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(completeness * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { completeness },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CoachBubble(coach: CoachPersona, message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(try { Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16)) } catch (_: Exception) { Color(0xFF6366F1) })
        ) {
            if (coach.imageUrl != null) {
                AsyncImage(
                    model = coach.imageUrl,
                    contentDescription = coach.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = coach.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = coach.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun PhaseContent(
    phase: OnboardingPhase,
    viewModel: CoachOnboardingViewModel,
    onAdvance: () -> Unit,
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
            selected = viewModel.topPriority,
            onSelect = { viewModel.topPriority = it; onAdvance() }
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

        OnboardingPhase.COMPLETE -> CompleteStep(
            completeness = viewModel.overallCompleteness(),
            onDone = onAdvance,
            isSaving = isSaving
        )
    }
}

// ─── Step composables ─────────────────────────────────────────────────────────

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
    var ageText by remember { mutableStateOf(age?.toString() ?: "") }

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PriorityStep(selected: GoalCategory?, onSelect: (GoalCategory) -> Unit) {
    val options = listOf(
        GoalCategory.CAREER to ("💼" to "Career"),
        GoalCategory.MONEY to ("💰" to "Money"),
        GoalCategory.BODY to ("💪" to "Body"),
        GoalCategory.PEOPLE to ("🤝" to "People"),
        GoalCategory.WELLBEING to ("✨" to "Wellbeing"),
        GoalCategory.PURPOSE to ("🧘" to "Purpose")
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2
    ) {
        options.forEach { (category, pair) ->
            val (emoji, label) = pair
            val isSelected = selected == category
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WellbeingStep(
    stress: Int,
    sleep: Int,
    onStressChange: (Int) -> Unit,
    onSleepChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    LabeledSlider(
        label = "Stress level: $stress / 10",
        value = stress,
        onValueChange = onStressChange
    )
    Spacer(modifier = Modifier.height(20.dp))
    LabeledSlider(
        label = "Sleep quality: $sleep / 10",
        value = sleep,
        onValueChange = onSleepChange
    )
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(text = "Continue", onClick = onContinue)
}

@Composable
private fun LabeledSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = 1f..10f,
        steps = 8,
        modifier = Modifier.fillMaxWidth()
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Low", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("High", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
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
            onSelect = { label ->
                vm.employmentStatus = employmentStatusFromLabel(label)
                onAdvance()
            },
            onSkip = onAdvance
        )
        "morgan_finance" -> ChipQuestion(
            options = incomeBandOptions(),
            selected = vm.incomeBand?.name,
            onSelect = { label -> vm.incomeBand = incomeBandFromLabel(label); onAdvance() },
            onSkip = onAdvance
        )
        "kai_fitness" -> ChipQuestion(
            options = activityLevelOptions(),
            selected = vm.activityLevel?.name,
            onSelect = { label -> vm.activityLevel = activityLevelFromLabel(label); onAdvance() },
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
                onAdvance()
            },
            onSkip = onAdvance
        )
        "river_wellness" -> ValuesPickerStep(
            selected = vm.topValues,
            onDone = { vm.topValues = it; onAdvance() },
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
                    "None" -> az.tribe.lifeplanner.domain.model.SavingsHabit.NONE
                    "Sporadic" -> az.tribe.lifeplanner.domain.model.SavingsHabit.SPORADIC
                    "Consistent" -> az.tribe.lifeplanner.domain.model.SavingsHabit.CONSISTENT
                    else -> az.tribe.lifeplanner.domain.model.SavingsHabit.AGGRESSIVE
                }
                onAdvance()
            },
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
                onAdvance()
            },
            onSkip = onAdvance
        )
        "river_wellness" -> ChipQuestion(
            options = listOf("Yes, regularly", "Occasionally", "Not yet"),
            selected = vm.mindfulnessPractice?.let { if (it) "Yes, regularly" else "Not yet" },
            onSelect = { label ->
                vm.mindfulnessPractice = "Yes" in label || "Occas" in label
                onAdvance()
            },
            onSkip = onAdvance
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
            onSelect = { label -> vm.hasDebt = "Yes" in label; onAdvance() },
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
                onAdvance()
            },
            onSkip = onAdvance
        )
        "river_wellness" -> TextInputStep(
            label = "Your long-term vision (optional)",
            value = vm.longTermVision,
            onChange = { vm.longTermVision = it },
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

// ─── Reusable step widgets ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipQuestion(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onSkip: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected?.contains(option, ignoreCase = true) == true
            Surface(
                modifier = Modifier.clickable { onSelect(option) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Skip this question") }
}

@Composable
private fun TextInputStep(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    onContinue: () -> Unit,
    optional: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
    )
    Spacer(Modifier.height(16.dp))
    PrimaryButton("Continue", onClick = onContinue, enabled = optional || value.isNotBlank())
    if (optional) TextButton(onClick = onContinue) { Text("Skip") }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ValuesPickerStep(
    selected: List<String>,
    onDone: (List<String>) -> Unit,
    onSkip: () -> Unit
) {
    val allValues = listOf("Growth", "Family", "Freedom", "Health", "Creativity",
        "Achievement", "Security", "Impact", "Balance", "Adventure")
    var picks by remember { mutableStateOf(selected) }

    Text(
        text = "Pick up to 3 values that matter most",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allValues.forEach { v ->
            val isSelected = v in picks
            Surface(
                modifier = Modifier.clickable {
                    picks = if (isSelected) picks - v
                    else if (picks.size < 3) picks + v else picks
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = v,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    PrimaryButton("Continue", onClick = { onDone(picks) })
    TextButton(onClick = onSkip) { Text("Skip") }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Phase message strings ─────────────────────────────────────────────────────

private fun phaseMessage(phase: OnboardingPhase, vm: CoachOnboardingViewModel): String {
    val name = vm.userName.takeIf { it.isNotBlank() }?.let { "Hey $it!" } ?: "Hey!"
    return when (phase) {
        OnboardingPhase.LUNA_INTRO ->
            "Hi, I'm Luna — your life coach! Before we dive in, I'd love to get to know you a little. It'll only take 2 minutes, and the more I know, the better I can help."
        OnboardingPhase.LUNA_NAME ->
            "Let's start with the basics. What's your name, and how old are you? (You can skip either if you prefer.)"
        OnboardingPhase.LUNA_PRIORITY ->
            "$name Which area of life feels most important to you right now?"
        OnboardingPhase.LUNA_WELLBEING ->
            "Got it. How are you feeling lately? Be honest — this helps me calibrate your goals."
        OnboardingPhase.SPECIALIST_INTRO -> {
            val specialistName = runCatching { CoachPersona.getById(vm.specialistCoachId).name }.getOrElse { "Your coach" }
            val area = vm.topPriority?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "your goals"
            "Great choices! I'm bringing in $specialistName — our $area specialist. They'll do a quick check-in so your $area goals are actually built for you."
        }
        OnboardingPhase.SPECIALIST_Q1 -> specialistQ1Message(vm)
        OnboardingPhase.SPECIALIST_Q2 -> specialistQ2Message(vm)
        OnboardingPhase.SPECIALIST_Q3 -> specialistQ3Message(vm)
        OnboardingPhase.SPECIALIST_Q4 -> specialistQ4Message(vm)
        OnboardingPhase.COMPLETE ->
            "You're all set! I've built your initial profile. Let's make some goals."
    }
}

private fun specialistQ1Message(vm: CoachOnboardingViewModel) = when (vm.specialistCoachId) {
    "alex_career" -> ageAdaptedCareerQ1(vm.userAge ?: 25)
    "morgan_finance" -> "What's your rough income range? I keep everything private — this helps me tailor financial goals to your reality."
    "kai_fitness" -> "How active are you day-to-day?"
    "sam_social" -> "Are you more of an introvert, ambivert, or extrovert? No right answer — just helps me understand how you recharge."
    "river_wellness" -> "What are your top values? Pick up to 3. These are the compass for all your goals."
    else -> "What's on your mind?"
}

private fun specialistQ2Message(vm: CoachOnboardingViewModel) = when (vm.specialistCoachId) {
    "alex_career" -> ageBandedRoleQuestion(vm.userAge ?: 25)
    "morgan_finance" -> "How consistent are your savings habits?"
    "kai_fitness" -> "How many hours do you sleep on a typical night?"
    "sam_social" -> "How big is your close social circle — people you'd actually call in a tough moment?"
    "river_wellness" -> "Do you have any mindfulness or meditation practice?"
    else -> ""
}

private fun specialistQ3Message(vm: CoachOnboardingViewModel) = when (vm.specialistCoachId) {
    "alex_career" -> "Roughly how many years of work experience do you have?"
    "morgan_finance" -> "Do you currently have any debt you're managing?"
    "kai_fitness" -> "On a scale of 1–10, how's your energy level on a typical day?"
    "sam_social" -> "What's your relationship status? Helps me understand your support structure."
    "river_wellness" -> "In one sentence, what's your long-term vision for your life?"
    else -> ""
}

private fun specialistQ4Message(vm: CoachOnboardingViewModel) = when (vm.specialistCoachId) {
    "alex_career" -> "What's your main career ambition right now? (One sentence is fine.)"
    "morgan_finance" -> "What's your main financial goal? Build savings? Pay off debt? Invest?"
    else -> ""
}

private fun ageAdaptedCareerQ1(age: Int) = when {
    age in 13..17 -> "Which grade are you in? Any part-time work or side projects?"
    age in 18..22 -> "Are you studying, working, or doing both right now?"
    age in 23..35 -> "Are you employed full-time, freelancing, job-hunting, or running your own thing?"
    age in 36..55 -> "Tell me about your current role — employed, self-employed, or something else?"
    else -> "Are you still working, semi-retired, or fully retired?"
}

private fun ageBandedRoleLabel(age: Int) = when {
    age in 13..22 -> "What are you studying or working on?"
    age in 23..55 -> "What's your current role or job title?"
    else -> "Any current work, projects, or passions?"
}

private fun ageBandedRoleQuestion(age: Int) = when {
    age in 13..22 -> "What are you studying or working on?"
    age in 23..55 -> "What's your current role or job title? (Optional)"
    else -> "Any current work, projects, or passions?"
}

// ─── Enum helpers ─────────────────────────────────────────────────────────────

private fun employmentStatusOptions(age: Int) = when {
    age in 13..17 -> listOf("Student", "Part-time work", "Side hustle")
    age in 18..22 -> listOf("Student", "Employed", "Both", "Job hunting")
    age in 23..55 -> listOf("Employed", "Freelance", "Entrepreneur", "Job hunting", "Unemployed")
    else -> listOf("Working", "Semi-retired", "Retired", "Freelance")
}

private fun employmentStatusFromLabel(label: String) = when {
    "Student" in label -> az.tribe.lifeplanner.domain.model.EmploymentStatus.STUDENT
    "Employed" in label || "Working" in label -> az.tribe.lifeplanner.domain.model.EmploymentStatus.EMPLOYED
    "Freelance" in label -> az.tribe.lifeplanner.domain.model.EmploymentStatus.FREELANCE
    "Entrepreneur" in label -> az.tribe.lifeplanner.domain.model.EmploymentStatus.ENTREPRENEUR
    "Retired" in label -> az.tribe.lifeplanner.domain.model.EmploymentStatus.RETIRED
    else -> az.tribe.lifeplanner.domain.model.EmploymentStatus.UNEMPLOYED
}

private fun incomeBandOptions() = listOf(
    "Under \$15K", "\$15K–30K", "\$30K–60K", "\$60K–100K", "\$100K–200K", "Over \$200K"
)

private fun incomeBandFromLabel(label: String) = when {
    "Under" in label -> az.tribe.lifeplanner.domain.model.IncomeBand.UNDER_15K
    "15K–30" in label -> az.tribe.lifeplanner.domain.model.IncomeBand.BAND_15_30K
    "30K–60" in label -> az.tribe.lifeplanner.domain.model.IncomeBand.BAND_30_60K
    "60K–100" in label -> az.tribe.lifeplanner.domain.model.IncomeBand.BAND_60_100K
    "100K–200" in label -> az.tribe.lifeplanner.domain.model.IncomeBand.BAND_100_200K
    else -> az.tribe.lifeplanner.domain.model.IncomeBand.OVER_200K
}

private fun activityLevelOptions() = listOf("Mostly sedentary", "Light (walks etc.)", "Moderate (3×/wk)", "Active (5×/wk)", "Very active (daily)")

private fun activityLevelFromLabel(label: String) = when {
    "sedentary" in label -> ActivityLevel.SEDENTARY
    "Light" in label -> ActivityLevel.LIGHT
    "Moderate" in label -> ActivityLevel.MODERATE
    "Active (5" in label -> ActivityLevel.ACTIVE
    else -> ActivityLevel.VERY_ACTIVE
}
