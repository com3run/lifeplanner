package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import az.tribe.lifeplanner.domain.model.RelationshipStatus
import az.tribe.lifeplanner.domain.model.SavingsHabit
import az.tribe.lifeplanner.domain.model.SocialEnergy
import coil3.compose.AsyncImage

// ─── Coach bubble ─────────────────────────────────────────────────────────────

@Composable
internal fun CoachBubble(coach: CoachPersona, message: String) {
    val bgColor = try {
        Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16))
    } catch (_: Exception) { Color(0xFF6366F1) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (coach.imageUrl != null) {
            // Full-width portrait card with gradient name overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
            ) {
                AsyncImage(
                    model = coach.imageUrl,
                    contentDescription = coach.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = coach.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(bgColor)
            ) {
                Text(
                    text = coach.emoji,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = coach.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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

// ─── Phase routing ────────────────────────────────────────────────────────────

@Composable
internal fun PhaseContent(
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
            onSelect = { viewModel.topPriority = it },
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
            onContinue = onAdvance
        )

        OnboardingPhase.COMPLETE -> CompleteStep(
            completeness = viewModel.overallCompleteness(),
            onDone = onAdvance,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PriorityStep(
    selected: GoalCategory?,
    onSelect: (GoalCategory) -> Unit,
    onContinue: () -> Unit
) {
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
                modifier = Modifier.weight(1f).clickable { onSelect(category) },
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
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(text = "Continue", onClick = onContinue, enabled = selected != null)
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
    onContinue: () -> Unit
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
    PrimaryButton(text = "Let's build it!", onClick = onContinue, enabled = value.isNotBlank())
    TextButton(onClick = onContinue) { Text("Skip for now") }
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
