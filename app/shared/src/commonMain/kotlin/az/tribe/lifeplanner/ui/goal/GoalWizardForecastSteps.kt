package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.service.AdherenceForecast
import az.tribe.lifeplanner.domain.service.ForecastConfidence
import az.tribe.lifeplanner.domain.service.ForecastDriver
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.modernColors

// ─── Crystal Ball wizard steps (FORECAST + CRYSTAL_BALL) ─────────────────────
// Ali Abdaal's evidence-based framing (mental contrasting + implementation
// intentions), powered by AdherenceForecastEngine (DecisionProfile + Calibration).

/** One editable pre-mortem row on the CRYSTAL_BALL step. */
data class PreMortemDraft(
    val obstacle: String = "",
    val thenAction: String = "",
    val trigger: PreMortemTriggerOption = PreMortemTriggerOption.STREAK_BREAK,
) {
    val isComplete: Boolean get() = obstacle.isNotBlank() && thenAction.isNotBlank()
}

/** UI trigger choices, mapped to ChoicePointTrigger names for storage/resurfacing. */
enum class PreMortemTriggerOption(val label: String, val storageName: String) {
    STREAK_BREAK("Streak breaks", "HABIT_STREAK_BREAK"),
    STALL("I stall", "GOAL_STALLED"),
    DEADLINE("Deadline slips", "DEADLINE_PASSED"),
    OTHER("Something else", "OTHER"),
}

@Composable
internal fun ForecastStep(
    forecast: AdherenceForecast,
    onCrystalBall: () -> Unit,
    onSkipAndCreate: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Your follow-through odds",
            style = MaterialTheme.typography.headlineSmall,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Not how good the plan is — how likely you are to still be running it. Based on your own wiring and track record.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
        )
        Spacer(Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = c.primaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${forecast.adherencePercent}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = c.onPrimaryContainer,
                    )
                    Text(
                        text = "  ±${forecast.bandPercent}",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (forecast.confidence) {
                        ForecastConfidence.HIGH -> "High confidence — we know your patterns well"
                        ForecastConfidence.MEDIUM -> "Medium confidence — the picture is forming"
                        ForecastConfidence.LOW -> "Low confidence — we're still learning you"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = c.onPrimaryContainer,
                )
            }
        }

        if (forecast.isColdStart) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "This sharpens as the app learns how you actually decide and finish — every goal you run makes the next forecast more yours.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textTertiary,
            )
        }

        if (forecast.drivers.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "WHAT'S MOVING YOUR NUMBER",
                style = MaterialTheme.typography.labelSmall,
                color = c.textTertiary,
            )
            Spacer(Modifier.height(8.dp))
            forecast.drivers.forEach { driver ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = c.surfaceVariant,
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (driver.direction == ForecastDriver.Direction.RAISES) "↑" else "↓",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (driver.direction == ForecastDriver.Direction.RAISES) c.success else c.warning,
                        )
                        Spacer(Modifier.height(0.dp))
                        Text(
                            text = "  " + driver.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        AppButton(
            text = "Raise my odds — run the Crystal Ball",
            onClick = onCrystalBall,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSkipAndCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Skip & create goal", color = c.textSecondary)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun CrystalBallStep(
    drafts: List<PreMortemDraft>,
    adherenceNow: Int,
    onDraftChange: (Int, PreMortemDraft) -> Unit,
    onFinish: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val written = drafts.count { it.isComplete }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Crystal Ball",
            style = MaterialTheme.typography.headlineSmall,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Name three reasons future-you won't follow this plan — and decide now what you'll do when they hit. When one actually happens, I'll hand you your own fix.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
        )
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(20.dp), color = c.successContainer) {
            Text(
                text = "Odds now: $adherenceNow%  ·  $written of 3 fixes written",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = c.onSuccessContainer,
            )
        }
        Spacer(Modifier.height(16.dp))

        drafts.forEachIndexed { index, draft ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = c.surface,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reason ${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = draft.obstacle,
                        onValueChange = { onDraftChange(index, draft.copy(obstacle = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("What could derail you?") },
                        minLines = 1,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = draft.thenAction,
                        onValueChange = { onDraftChange(index, draft.copy(thenAction = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("If that happens, I will…") },
                        minLines = 1,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PreMortemTriggerOption.entries.forEach { option ->
                            FilterChip(
                                selected = draft.trigger == option,
                                onClick = { onDraftChange(index, draft.copy(trigger = option)) },
                                label = { Text(option.label, style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        AppButton(
            text = if (written > 0) "Create goal with $written fix${if (written == 1) "" else "es"}" else "Create goal",
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            variant = AppButtonVariant.PRIMARY,
        )
        Spacer(Modifier.height(24.dp))
    }
}
