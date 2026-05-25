package az.tribe.lifeplanner.ui.wiring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.domain.model.TuningDial
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * Pillar 7 (Innate), "Your Wiring". Shows the user their inferred [DecisionProfile] in plain,
 * non-judgmental language: every dial is a neutral trait with trade-offs, never a deficiency.
 * Inference confidence is shown honestly, and the user can nudge a dial if it feels wrong.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourWiringScreen(
    onBackClick: () -> Unit,
    viewModel: WiringViewModel = koinViewModel(),
) {
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Your Wiring", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = MaterialTheme.modernColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 84.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "How you're wired, learned from what you do, not a quiz. None of these is good or " +
                        "bad; each is just you, with its own strengths. If one feels off, nudge it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.modernColors.textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(TuningDial.entries.toList(), key = { it.name }) { dial ->
                val setting = profile?.dial(dial) ?: DialSetting()
                DialCard(dial, setting, onNudge = { v -> viewModel.nudge(dial, v) })
            }
        }
    }
}

@Composable
private fun DialCard(dial: TuningDial, setting: DialSetting, onNudge: (Float) -> Unit) {
    val labels = dial.labels()
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(labels.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.textPrimary)
                Text(confidenceLabel(setting), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
            }
            Text(labels.describe(setting.value), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.modernColors.textSecondary)

            // Local slider state, re-seeded whenever the stored value changes.
            key(setting.value) {
                var slider by remember { mutableStateOf(setting.value) }
                Slider(
                    value = slider,
                    onValueChange = { slider = it },
                    onValueChangeFinished = { onNudge(slider) },
                    valueRange = 0f..1f,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(labels.lowLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
                Text(labels.highLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
            }
        }
    }
}

private fun confidenceLabel(s: DialSetting): String =
    if (!s.isReliable) {
        if (s.sampleSize > 0) "Still learning · ${s.sampleSize} signals" else "Still learning"
    } else {
        "Confidence ${(s.confidence * 100).roundToInt()}%"
    }

/** Plain-language framing for a dial, descriptive poles, never evaluative. */
private data class DialLabels(val title: String, val lowLabel: String, val highLabel: String, val low: String, val high: String) {
    fun describe(value: Float): String = when {
        value < 0.4f -> low
        value > 0.6f -> high
        else -> "You sit somewhere in the middle here, it depends on the day."
    }
}

private fun TuningDial.labels(): DialLabels = when (this) {
    TuningDial.CONFIDENCE_THRESHOLD -> DialLabels(
        "How you commit", "Decide on the fly", "Like to be sure",
        "You tend to act fast and decide on the fly.",
        "You like to be sure before you commit, measure twice, cut once.")
    TuningDial.NOVELTY_SALIENCE -> DialLabels(
        "Novelty vs. routine", "Routine", "Variety",
        "You thrive on routine and continuity.",
        "You're drawn to variety and trying new things.")
    TuningDial.DELAY_DISCOUNTING -> DialLabels(
        "Near vs. long term", "Patient", "Quick wins",
        "You're patient, happy to play the long game.",
        "You're pulled toward quick wins and near-term payoff.")
    TuningDial.PUNISHMENT_SENSITIVITY -> DialLabels(
        "How setbacks land", "Roll off", "Feel them",
        "Missed days and setbacks roll off you easily.",
        "Setbacks land hard, a missed day really registers.")
    TuningDial.REWARD_SENSITIVITY -> DialLabels(
        "Pull of wins", "Indifferent", "Energized",
        "Wins and streaks don't move you much.",
        "Wins, streaks and progress really energize you.")
    TuningDial.RISK_AVERSION -> DialLabels(
        "Risk appetite", "Take chances", "Play it safe",
        "You're comfortable taking chances on an uncertain payoff.",
        "You prefer the safe, sure option over a gamble.")
}
