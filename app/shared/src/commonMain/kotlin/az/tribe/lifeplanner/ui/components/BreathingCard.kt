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
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock

private const val BREATH_GOAL = 3
private const val BOX_UNLOCK_AT = 5 // completed sessions before Box Breathing opens up
private const val KEY_SESSIONS_TOTAL = "breath_sessions_total"
private const val KEY_TECHNIQUE = "breath_technique"

/** A breathing technique, each with its own symbol so they read as distinct rituals. */
enum class BreathTechnique(val id: String, val label: String, val symbol: String, val xp: Int) {
    CALM("calm", "Calm Breath", "🫧", XpRewards.MINDFUL_BREATH),
    BOX("box", "Box Breathing", "⬜", XpRewards.MINDFUL_BREATH_BOX),
}

/**
 * A tiny in-app "grow" moment: take a breath or two a day. Finishing a session earns mindfulness XP
 * and counts toward a small daily goal. After [BOX_UNLOCK_AT] lifetime sessions, Box Breathing opens
 * up as a second technique, tracked with a square frame that traces one side per phase instead of a
 * bare countdown.
 */
@Composable
fun BreathingCard() {
    val settings: Settings = koinInject()
    val gamification: GamificationRepository = koinInject()
    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val c = MaterialTheme.modernColors

    val dateKey = remember { "breaths_" + Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() }
    var count by remember { mutableStateOf(settings.getInt(dateKey, 0)) }
    var sessionsTotal by remember { mutableStateOf(settings.getInt(KEY_SESSIONS_TOTAL, 0)) }
    var active by remember { mutableStateOf(false) }
    var justUnlocked by remember { mutableStateOf(false) }

    val boxUnlocked = sessionsTotal >= BOX_UNLOCK_AT
    var selected by remember {
        mutableStateOf(
            BreathTechnique.entries.firstOrNull { it.id == settings.getString(KEY_TECHNIQUE, BreathTechnique.CALM.id) }
                ?: BreathTechnique.CALM
        )
    }
    // A locked technique can never be the active one.
    val technique = if (!boxUnlocked && selected == BreathTechnique.BOX) BreathTechnique.CALM else selected

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
                        if (sessionsTotal < BOX_UNLOCK_AT && newTotal >= BOX_UNLOCK_AT) justUnlocked = true
                        sessionsTotal = newTotal
                        settings.putInt(KEY_SESSIONS_TOTAL, newTotal)
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

                    when {
                        justUnlocked -> Text(
                            "🎉 Box Breathing unlocked! Give it a try.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = c.primary,
                        )
                        boxUnlocked -> TechniqueChips(
                            selected = technique,
                            onSelect = { t -> selected = t; settings.putString(KEY_TECHNIQUE, t.id) },
                        )
                        else -> Text(
                            "⬜ Box Breathing unlocks in ${BOX_UNLOCK_AT - sessionsTotal} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textTertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TechniqueChips(selected: BreathTechnique, onSelect: (BreathTechnique) -> Unit) {
    val c = MaterialTheme.modernColors
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BreathTechnique.entries.forEach { t ->
            val isSel = t == selected
            Surface(
                modifier = Modifier.bouncyClickable { onSelect(t) },
                shape = RoundedCornerShape(50),
                color = if (isSel) c.primary.copy(alpha = 0.14f) else Color.Transparent,
                border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, c.textTertiary.copy(alpha = 0.4f)),
            ) {
                Text(
                    "${t.symbol}  ${t.label}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (isSel) c.primary else c.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun BreathingSession(technique: BreathTechnique, onDone: () -> Unit, onCancel: () -> Unit) {
    when (technique) {
        BreathTechnique.CALM -> CalmBreathVisual(onDone, onCancel)
        BreathTechnique.BOX -> BoxBreathVisual(onDone, onCancel)
    }
}

/** The original "grow" circle: swells on the inhale, settles on the exhale. */
@Composable
private fun CalmBreathVisual(onDone: () -> Unit, onCancel: () -> Unit) {
    val c = MaterialTheme.modernColors
    val total = 3
    var inhaling by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = if (inhaling) 1f else 0.55f,
        animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
        label = "breathScale",
    )
    LaunchedEffect(Unit) {
        for (i in 0 until total) {
            inhaling = true; delay(3800)
            inhaling = false; delay(3800)
            completed = i + 1
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
            Text(if (inhaling) "In" else "Out", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
        }
        Text(if (inhaling) "Breathe in…" else "Breathe out…", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Text("Breath ${(completed + 1).coerceAtMost(total)} of $total", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        TextButton(onClick = onCancel) { Text("Done for now") }
    }
}

/**
 * Box breathing: inhale, hold, exhale, hold, four equal phases. A square frame traces one side per
 * phase so the passing time reads as motion around the box, not a number ticking down.
 */
@Composable
private fun BoxBreathVisual(onDone: () -> Unit, onCancel: () -> Unit) {
    val c = MaterialTheme.modernColors
    val rounds = 3
    val phaseMs = 4000
    var currentSide by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(0) }
    val sideProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        for (r in 0 until rounds) {
            round = r
            for (side in 0..3) {
                currentSide = side
                sideProgress.snapTo(0f)
                sideProgress.animateTo(1f, animationSpec = tween(phaseMs, easing = LinearEasing))
            }
        }
        onDone()
    }

    val p = sideProgress.value
    val phaseLabel = when (currentSide) {
        0 -> "Breathe in"
        1 -> "Hold"
        2 -> "Breathe out"
        else -> "Hold"
    }
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
