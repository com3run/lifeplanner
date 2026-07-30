package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock

private const val BREATH_GOAL = 3
private const val KEY_SESSIONS_TOTAL = "breath_sessions_total"

/** One phase of a breath: a spoken label, a short orb caption, its length, and the orb's end scale. */
data class BreathPhase(val label: String, val short: String, val durationMs: Int, val targetScale: Float)

/**
 * A breathing technique. Techniques are chosen automatically by the user's mindfulness level (see
 * [BREATH_LEVELS]) and are never named in the UI, the app just guides the breath. Each is [rounds]
 * repeats of [phases]; [boxStyle] techniques render the square-frame tracer instead of the orb.
 * [label] is for code/analytics only and is never shown.
 */
enum class BreathTechnique(
    val id: String,
    val label: String,
    val symbol: String,
    val xp: Int,
    val rounds: Int,
    val phases: List<BreathPhase>,
    val boxStyle: Boolean = false,
) {
    CALM("calm", "Calm Breath", "🫧", XpRewards.MINDFUL_BREATH, 3, listOf(
        BreathPhase("Breathe in", "In", 3800, 1f),
        BreathPhase("Breathe out", "Out", 3800, 0.55f),
    )),
    EXTENDED_EXHALE("ext_exhale", "Extended Exhale", "🌊", XpRewards.MINDFUL_BREATH, 4, listOf(
        BreathPhase("Breathe in", "In", 4000, 1f),
        BreathPhase("Long exhale", "Out", 6000, 0.5f),
    )),
    BOX("box", "Box Breathing", "⬜", XpRewards.MINDFUL_BREATH_BOX, 3, listOf(
        BreathPhase("Breathe in", "In", 4000, 1f),
        BreathPhase("Hold", "Hold", 4000, 1f),
        BreathPhase("Breathe out", "Out", 4000, 0.55f),
        BreathPhase("Hold", "Hold", 4000, 0.55f),
    ), boxStyle = true),
    RELAXING_478("relax478", "4-7-8 Breath", "🌙", XpRewards.MINDFUL_BREATH_BOX, 3, listOf(
        BreathPhase("Breathe in", "In", 4000, 1f),
        BreathPhase("Hold", "Hold", 7000, 1f),
        BreathPhase("Breathe out", "Out", 8000, 0.5f),
    )),
    RESONANT("resonant", "Resonant Breath", "💫", XpRewards.MINDFUL_BREATH_BOX, 5, listOf(
        BreathPhase("Breathe in", "In", 5500, 1f),
        BreathPhase("Breathe out", "Out", 5500, 0.5f),
    )),
}

/** Lifetime completed-session count required for each technique. Order = unlock order. */
private val BREATH_LEVELS: List<Pair<Int, BreathTechnique>> = listOf(
    0 to BreathTechnique.CALM,
    3 to BreathTechnique.EXTENDED_EXHALE,
    8 to BreathTechnique.BOX,
    15 to BreathTechnique.RELAXING_478,
    25 to BreathTechnique.RESONANT,
)

/** The technique the user has grown into at [sessions] lifetime sessions (the highest unlocked). */
private fun techniqueForSessions(sessions: Int): BreathTechnique =
    BREATH_LEVELS.last { sessions >= it.first }.second

/** Lifetime sessions at which the next technique unlocks, or null once every rhythm is unlocked. */
private fun nextUnlockAt(sessions: Int): Int? =
    BREATH_LEVELS.firstOrNull { sessions < it.first }?.first

private fun todayBreathKey(): String =
    "breaths_" + Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

/**
 * A tiny in-app "grow" moment: take a breath or two a day. Finishing a session earns mindfulness XP
 * and counts toward a small daily goal. The breathing pattern deepens on its own as the user builds
 * a practice (see [BREATH_LEVELS]), no technique picker, no names, just a fresh rhythm now and then.
 */
@Composable
fun BreathingCard() {
    val settings: Settings = koinInject()
    val gamification: GamificationRepository = koinInject()
    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val c = MaterialTheme.modernColors

    val dateKey = remember { todayBreathKey() }
    var count by remember { mutableStateOf(settings.getInt(dateKey, 0)) }
    var sessionsTotal by remember { mutableStateOf(settings.getInt(KEY_SESSIONS_TOTAL, 0)) }
    var active by remember { mutableStateOf(false) }
    var justUnlocked by remember { mutableStateOf(false) }

    val technique = techniqueForSessions(sessionsTotal)

    AnimatedVisibility(visible = count < BREATH_GOAL || active) {
        Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
            if (active) {
                BreathingSession(
                    technique = technique,
                    onDone = {
                        val next = (count + 1).coerceAtMost(BREATH_GOAL)
                        count = next
                        settings.putInt(dateKey, next)
                        val newTotal = sessionsTotal + 1
                        // Crossing into a higher level reveals a fresh rhythm (still unnamed).
                        if (techniqueForSessions(newTotal) != techniqueForSessions(sessionsTotal)) justUnlocked = true
                        settings.putInt(KEY_SESSIONS_TOTAL, newTotal)
                        sessionsTotal = newTotal
                        scope.launch { runCatching { gamification.awardXp(technique.xp.toLong()) } }
                        haptic.success()
                        active = false
                    },
                    onCancel = { active = false },
                )
            } else {
                Column(
                    Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
                    verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(technique.symbol, style = MaterialTheme.typography.headlineSmall)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Take a breath", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                            Text(
                                if (count == 0) "A moment to reset before you dive in." else "$count of $BREATH_GOAL today. One more?",
                                style = MaterialTheme.typography.bodySmall, color = c.textSecondary,
                            )
                        }
                        AppButton(text = "Breathe", onClick = { active = true }, variant = AppButtonVariant.SECONDARY)
                    }

                    // A quiet sense of progression: a fresh rhythm on level-up, or how far to the next.
                    val remaining = nextUnlockAt(sessionsTotal)?.minus(sessionsTotal)
                    when {
                        justUnlocked -> Text(
                            "🎉 A new breath unlocked — a fresh rhythm to try.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = c.primary,
                        )
                        remaining != null -> Text(
                            if (remaining == 1) "1 more breath to your next rhythm" else "$remaining more breaths to your next rhythm",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textTertiary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * A self-contained guided breath for use outside the feed card (e.g. the "take a breath" nudge).
 * Auto-selects the level-appropriate technique, awards XP and advances the practice on completion,
 * then calls [onClose]. Cancelling just closes without counting.
 */
@Composable
fun GuidedBreathSession(onClose: () -> Unit) {
    val settings: Settings = koinInject()
    val gamification: GamificationRepository = koinInject()
    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val c = MaterialTheme.modernColors

    val sessionsTotal = remember { settings.getInt(KEY_SESSIONS_TOTAL, 0) }
    val technique = techniqueForSessions(sessionsTotal)

    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        BreathingSession(
            technique = technique,
            onDone = {
                settings.putInt(KEY_SESSIONS_TOTAL, sessionsTotal + 1)
                val dateKey = todayBreathKey()
                settings.putInt(dateKey, settings.getInt(dateKey, 0) + 1)
                scope.launch { runCatching { gamification.awardXp(technique.xp.toLong()) } }
                haptic.success()
                onClose()
            },
            onCancel = onClose,
        )
    }
}

@Composable
private fun BreathingSession(technique: BreathTechnique, onDone: () -> Unit, onCancel: () -> Unit) {
    if (technique.boxStyle) BoxBreathVisual(technique, onDone, onCancel)
    else PacedBreathVisual(technique, onDone, onCancel)
}

/**
 * The "grow" orb: it swells and settles through the technique's phases. Timing is driven by explicit
 * per-phase delays (so hold phases actually hold), while the orb scale eases toward each phase's
 * target over that phase's duration.
 */
@Composable
private fun PacedBreathVisual(technique: BreathTechnique, onDone: () -> Unit, onCancel: () -> Unit) {
    val c = MaterialTheme.modernColors
    val phases = technique.phases
    val rounds = technique.rounds
    var round by remember { mutableStateOf(0) }
    var phaseIdx by remember { mutableStateOf(0) }
    val current = phases[phaseIdx]
    val scale by animateFloatAsState(
        targetValue = current.targetScale,
        animationSpec = tween(durationMillis = current.durationMs, easing = FastOutSlowInEasing),
        label = "pacedScale",
    )
    LaunchedEffect(Unit) {
        for (r in 0 until rounds) {
            round = r
            for (i in phases.indices) {
                phaseIdx = i
                delay(phases[i].durationMs.toLong())
            }
        }
        onDone()
    }
    Column(
        Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
    ) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(150.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(c.primary.copy(alpha = 0.55f), c.primary.copy(alpha = 0.12f)))),
            )
            Text(current.short, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
        }
        Text("${current.label}…", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Text("Breath ${(round + 1).coerceAtMost(rounds)} of $rounds", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        TextButton(onClick = onCancel) { Text("Done for now") }
    }
}

/**
 * Box breathing: inhale, hold, exhale, hold, four equal phases. A square frame traces one side per
 * phase so the passing time reads as motion around the box, not a number ticking down. Phase count,
 * durations, and labels come from [technique].
 */
@Composable
private fun BoxBreathVisual(technique: BreathTechnique, onDone: () -> Unit, onCancel: () -> Unit) {
    val c = MaterialTheme.modernColors
    val rounds = technique.rounds
    val phases = technique.phases
    var currentSide by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(0) }
    val sideProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        for (r in 0 until rounds) {
            round = r
            for (side in 0..3) {
                currentSide = side
                sideProgress.snapTo(0f)
                sideProgress.animateTo(1f, animationSpec = tween(phases[side].durationMs, easing = LinearEasing))
            }
        }
        onDone()
    }

    val p = sideProgress.value
    val phaseLabel = phases[currentSide].label
    // Inner square breathes with the phase: grows on inhale, holds, shrinks on exhale, holds.
    val innerScale = when (currentSide) {
        0 -> 0.55f + 0.45f * p
        1 -> 1f
        2 -> 1f - 0.4f * p
        else -> 0.6f
    }

    Column(
        Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
    ) {
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val strokePx = 10.dp.toPx()
                val s = size.minDimension - strokePx
                val o = strokePx / 2f
                // Corners clockwise from top-left; side i runs corners[i] -> corners[i+1].
                val corners = listOf(
                    Offset(o, o),
                    Offset(o + s, o),
                    Offset(o + s, o + s),
                    Offset(o, o + s),
                    Offset(o, o),
                )
                val faint = c.primary.copy(alpha = 0.15f)
                val bright = c.primary

                // Faint full frame.
                for (i in 0..3) drawLine(faint, corners[i], corners[i + 1], strokePx, cap = StrokeCap.Round)

                // Inner square that breathes.
                val inner = s * innerScale
                val center = Offset(size.width / 2f, size.height / 2f)
                drawRoundRect(
                    color = c.primary.copy(alpha = 0.10f),
                    topLeft = Offset(center.x - inner / 2f, center.y - inner / 2f),
                    size = Size(inner, inner),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                )

                // Bright trace: completed sides this round, plus the partial current side + a head dot.
                for (i in 0..3) {
                    when {
                        i < currentSide -> drawLine(bright, corners[i], corners[i + 1], strokePx, cap = StrokeCap.Round)
                        i == currentSide -> {
                            val head = lerp(corners[i], corners[i + 1], p)
                            drawLine(bright, corners[i], head, strokePx, cap = StrokeCap.Round)
                            drawCircle(bright, radius = strokePx * 0.85f, center = head)
                        }
                    }
                }
            }
            Text(
                phaseLabel,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
        }
        Text("Round ${round + 1} of $rounds", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        TextButton(onClick = onCancel) { Text("Done for now") }
    }
}
