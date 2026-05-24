package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.Motion
import az.tribe.lifeplanner.ui.theme.backgroundColor
import az.tribe.lifeplanner.ui.theme.containerColor
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * D11 — redesigned first-run. Establishes the agency-first promise in minute one, collects *just
 * enough* (a few values — no interrogation), and reaches a meaningful first action fast. Warm and
 * non-pressuring (D9/D12), token-pure with the premium blocks, Crossfade step transitions (D10).
 *
 * Values selected here are persisted as **Pillar 1** `LifeValue` rows on finish (the seam is wired
 * now that Pillar 1 is on `main`).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
fun OnboardingFlowScreen(
    onFinish: () -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    val selected = remember { mutableStateOf(setOf<GoalCategory>()) }
    val c = MaterialTheme.modernColors
    val lifeValueRepository: LifeValueRepository = koinInject()
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = c.background) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(LifePlannerDesign.Padding.screenHorizontal),
        ) {
            // Progress dots + Skip
            Row(Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    repeat(3) { i ->
                        Box(
                            Modifier.size(width = if (i == step) 24.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.full))
                                .background(if (i == step) c.primary else c.surfaceVariant),
                        )
                    }
                }
                if (step < 2) Text("Skip", style = MaterialTheme.typography.labelLarge, color = c.textSecondary, modifier = Modifier.clickable(onClick = onFinish))
            }

            Crossfade(targetState = step, animationSpec = tween(Motion.Duration.medium), label = "onboardingStep", modifier = Modifier.weight(1f)) { s ->
                when (s) {
                    0 -> PromiseStep()
                    1 -> ValuesStep(selected.value, onToggle = { cat ->
                        selected.value = if (cat in selected.value) selected.value - cat else selected.value + cat
                    })
                    else -> ReadyStep(selected.value)
                }
            }

            // Bottom CTA
            val (label, enabled) = when (step) {
                0 -> "Get started" to true
                1 -> "Continue" to selected.value.isNotEmpty()
                else -> "Go to Today" to true
            }
            AppButton(
                text = label,
                onClick = {
                    if (step < 2) {
                        step++
                    } else {
                        val values = selected.value.mapIndexed { i, cat ->
                            LifeValue(id = Uuid.random().toString(), title = cat.displayName, order = i)
                        }
                        scope.launch { runCatching { lifeValueRepository.insertLifeValues(values) } }
                        onFinish()
                    }
                },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.md),
            )
        }
    }
}

@Composable
private fun PromiseStep() {
    val c = MaterialTheme.modernColors
    Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md)) {
        GradientHero(
            eyebrow = "Welcome",
            title = "You're the one steering",
            subtitle = "LifePlanner helps you see your real options and choose deliberately. It never decides for you — and never makes you feel like the failure case.",
        )
        PromiseBullet("See what you could do right now — not a pile of obligations.")
        PromiseBullet("Every goal connects to a reason that's yours.")
        PromiseBullet("It gets more useful the more you live — no heavy setup.")
    }
}

@Composable
private fun PromiseBullet(text: String) {
    val c = MaterialTheme.modernColors
    Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.full)).background(c.primary))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ValuesStep(selected: Set<GoalCategory>, onToggle: (GoalCategory) -> Unit) {
    val c = MaterialTheme.modernColors
    Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
        Text("What matters most to you?", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
        Text("Pick a few — we'll connect your goals to them. You can change these anytime.", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
            modifier = Modifier.padding(top = LifePlannerDesign.Spacing.sm),
        ) {
            GoalCategory.entries.forEach { cat ->
                val on = cat in selected
                Surface(
                    color = if (on) cat.containerColor() else c.surfaceVariant,
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full),
                    modifier = Modifier.clickable { onToggle(cat) },
                ) {
                    Text(
                        cat.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = if (on) cat.backgroundColor() else c.textSecondary,
                        modifier = Modifier.padding(horizontal = LifePlannerDesign.Spacing.md, vertical = LifePlannerDesign.Spacing.sm),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadyStep(selected: Set<GoalCategory>) {
    val c = MaterialTheme.modernColors
    Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md)) {
        GradientHero(
            eyebrow = "You're set",
            title = "That's all we need",
            subtitle = "Everything else, the app learns from what you do. Let's see what you could do today.",
        )
        if (selected.isNotEmpty()) {
            Text("What matters to you", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs), verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                selected.forEach { cat ->
                    Surface(color = cat.containerColor(), shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full)) {
                        Text(cat.displayName, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = cat.backgroundColor(), modifier = Modifier.padding(horizontal = LifePlannerDesign.Spacing.md, vertical = LifePlannerDesign.Spacing.sm))
                    }
                }
            }
        }
    }
}
