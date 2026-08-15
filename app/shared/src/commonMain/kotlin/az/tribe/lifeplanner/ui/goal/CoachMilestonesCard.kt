package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.service.MilestoneCoach
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check

/**
 * Step ideas for a goal, written as quiet advice rather than an offer.
 *
 * This used to be a coach production: an avatar chip, "Kai drafted your first steps", tappable
 * suggestion buttons with edit affordances and a "More ideas" expander. The owner's read was
 * right: framed that way, generic steps read as the coach recommending things unrelated to the
 * goal. The same words presented as a few lines of secondary text read as what they are, a way to
 * think about the plan, and the user adds steps through the card's own add affordance.
 */
@Composable
fun CoachMilestonesContent(
    goalTitle: String,
    category: GoalCategory,
    description: String = "",
    existingTitles: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.modernColors
    val suggestions = remember(goalTitle, category, description, existingTitles) {
        MilestoneCoach.suggest(
            title = goalTitle,
            category = category,
            description = description,
            existingTitles = existingTitles,
        )
    }
    if (suggestions.isEmpty()) return

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xxs),
    ) {
        Text(
            MilestoneCoach.advice(category),
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary,
        )
        suggestions.take(3).forEach { suggestion ->
            Text(
                "·  $suggestion",
                style = MaterialTheme.typography.bodySmall,
                color = c.textTertiary,
            )
        }
    }
}

/**
 * The standalone form of the advice, for a goal with no milestones yet: the same text plus a field
 * to write the first step, because on that goal there is no milestones card to add through.
 */
@Composable
fun CoachMilestonesCard(
    goalTitle: String,
    category: GoalCategory,
    description: String = "",
    existingTitles: List<String> = emptyList(),
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.modernColors
    val haptic = rememberHapticManager()
    var ownStep by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            CoachMilestonesContent(
                goalTitle = goalTitle,
                category = category,
                description = description,
                existingTitles = existingTitles,
            )
            StepField(
                value = ownStep,
                onValueChange = { ownStep = it },
                placeholder = "Write your first step…",
                onSubmit = {
                    val text = ownStep.trim()
                    if (text.isNotEmpty()) {
                        haptic.success()
                        onAdd(text)
                        ownStep = ""
                    }
                },
            )
        }
    }
}

/** A single-line step editor with a confirm affordance; submitting on the keyboard also works. */
@Composable
private fun StepField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSubmit: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = c.textTertiary, maxLines = 1) },
        singleLine = true,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.primary,
            unfocusedBorderColor = c.textTertiary.copy(alpha = 0.35f),
        ),
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Box(
                    Modifier
                        .padding(end = 6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(c.primary)
                        .bouncyClickable(onClick = onSubmit),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(PhosphorIcons.Regular.Check, contentDescription = "Add step", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        },
    )
}
