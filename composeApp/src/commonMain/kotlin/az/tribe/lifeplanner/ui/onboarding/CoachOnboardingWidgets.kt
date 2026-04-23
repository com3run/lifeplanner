package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ActivityLevel
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.EmploymentStatus
import az.tribe.lifeplanner.domain.model.IncomeBand

// ─── Reusable widgets ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChipQuestion(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit,
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
    PrimaryButton("Continue", onClick = onContinue)
    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Skip this question") }
}

@Composable
internal fun TextInputStep(
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
internal fun ValuesPickerStep(
    selected: List<String>,
    onDone: (List<String>) -> Unit,
    onSkip: () -> Unit
) {
    val allValues = listOf("Growth", "Family", "Freedom", "Health", "Creativity",
        "Achievement", "Security", "Impact", "Balance", "Adventure")
    var picks by remember(selected) { mutableStateOf(selected) }

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
internal fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun LabeledSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
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

// ─── Phase message strings ─────────────────────────────────────────────────────

internal fun phaseMessage(phase: OnboardingPhase, vm: CoachOnboardingViewModel): String {
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
        OnboardingPhase.MIND_DUMP ->
            "Last one — and my favourite. What's actually on your mind right now? Just say it naturally. I'll turn it into your first goal."
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

// ─── Age-banded helpers ───────────────────────────────────────────────────────

private fun ageAdaptedCareerQ1(age: Int) = when {
    age in 13..17 -> "Which grade are you in? Any part-time work or side projects?"
    age in 18..22 -> "Are you studying, working, or doing both right now?"
    age in 23..35 -> "Are you employed full-time, freelancing, job-hunting, or running your own thing?"
    age in 36..55 -> "Tell me about your current role — employed, self-employed, or something else?"
    else -> "Are you still working, semi-retired, or fully retired?"
}

internal fun ageBandedRoleLabel(age: Int) = when {
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

internal fun employmentStatusOptions(age: Int) = when {
    age in 13..17 -> listOf("Student", "Part-time work", "Side hustle")
    age in 18..22 -> listOf("Student", "Employed", "Both", "Job hunting")
    age in 23..55 -> listOf("Employed", "Freelance", "Entrepreneur", "Job hunting", "Unemployed")
    else -> listOf("Working", "Semi-retired", "Retired", "Freelance")
}

internal fun employmentStatusFromLabel(label: String) = when {
    "Student" in label -> EmploymentStatus.STUDENT
    "Employed" in label || "Working" in label -> EmploymentStatus.EMPLOYED
    "Freelance" in label -> EmploymentStatus.FREELANCE
    "Entrepreneur" in label -> EmploymentStatus.ENTREPRENEUR
    "Retired" in label -> EmploymentStatus.RETIRED
    else -> EmploymentStatus.UNEMPLOYED
}

internal fun incomeBandOptions() = listOf(
    "Under \$15K", "\$15K–30K", "\$30K–60K", "\$60K–100K", "\$100K–200K", "Over \$200K"
)

internal fun incomeBandFromLabel(label: String) = when {
    "Under" in label -> IncomeBand.UNDER_15K
    "15K–30" in label -> IncomeBand.BAND_15_30K
    "30K–60" in label -> IncomeBand.BAND_30_60K
    "60K–100" in label -> IncomeBand.BAND_60_100K
    "100K–200" in label -> IncomeBand.BAND_100_200K
    else -> IncomeBand.OVER_200K
}

internal fun activityLevelOptions() = listOf(
    "Mostly sedentary", "Light (walks etc.)", "Moderate (3×/wk)", "Active (5×/wk)", "Very active (daily)"
)

internal fun activityLevelFromLabel(label: String) = when {
    "sedentary" in label -> ActivityLevel.SEDENTARY
    "Light" in label -> ActivityLevel.LIGHT
    "Moderate" in label -> ActivityLevel.MODERATE
    "Active (5" in label -> ActivityLevel.ACTIVE
    else -> ActivityLevel.VERY_ACTIVE
}
