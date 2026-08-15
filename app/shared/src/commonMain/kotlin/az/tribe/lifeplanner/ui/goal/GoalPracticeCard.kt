package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.GoalPractice
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Fire
import com.adamglin.phosphoricons.regular.Repeat

/**
 * A goal you keep rather than finish.
 *
 * When habits are linked to a goal, the habits are the goal: there is no checklist to complete, and
 * scoring it out of a milestone count says nothing true. This shows the thing that is actually
 * happening — how long the practice has been running, and whether it is still running.
 *
 * The 66-day window is a horizon, not a deadline, and the copy has to keep it that way. The figure
 * is a median with a range of 18 to 254 days, so day 67 is not a failure and the card never implies
 * it is.
 */
@Composable
fun GoalPracticeCard(
    practice: GoalPractice,
    onHabitClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.modernColors

    val animatedProgress by animateFloatAsState(
        targetValue = practice.windowProgress,
        animationSpec = tween(700),
        label = "practiceProgress",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(c.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        PhosphorIcons.Regular.Repeat,
                        contentDescription = null,
                        tint = c.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        // Past the window there is nothing left to count towards, so it stops being
                        // "day 91 of 66" and becomes a thing the user simply does.
                        if (practice.isEstablished) "This is a practice now"
                        else "Day ${practice.dayNumber} of ${practice.windowDays}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = c.textPrimary,
                    )
                    Text(
                        if (practice.isEstablished) {
                            "${practice.dayNumber} days in. It stopped being something you have to remember."
                        } else {
                            "Habits take about ${practice.windowDays} days to feel automatic, give or take a lot."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
            }

            Box(
                Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    .background(c.primary.copy(alpha = 0.14f)),
            ) {
                Box(
                    Modifier.fillMaxWidth(animatedProgress).height(6.dp).clip(CircleShape)
                        .background(c.primary),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md)) {
                PracticeStat(
                    value = practice.currentStreak.toString(),
                    label = if (practice.currentStreak == 1) "day running" else "days running",
                    icon = true,
                )
                PracticeStat(value = practice.checkIns.toString(), label = "check-ins")
                PracticeStat(value = practice.longestStreak.toString(), label = "best run")
            }

            practice.habits.forEach { habit ->
                Surface(
                    modifier = Modifier.fillMaxWidth()
                        .bouncyClickable { onHabitClick(habit.id) },
                    color = c.primary.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            habit.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (habit.currentStreak > 0) {
                            Text(
                                "${habit.currentStreak}d",
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textSecondary,
                            )
                        }
                        Icon(
                            PhosphorIcons.Regular.ArrowRight,
                            contentDescription = null,
                            tint = c.textTertiary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeStat(value: String, label: String, icon: Boolean = false) {
    val c = MaterialTheme.modernColors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon) {
                Icon(
                    PhosphorIcons.Regular.Fire,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}
