package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.service.MilestoneCoach
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Pencil
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle

/**
 * The coach's suggested steps for a goal, always present rather than only on an empty goal.
 *
 * A milestone list the user has to invent from a blank page is where most goals stall, so the
 * category coach opens with a draft (see [MilestoneCoach], local and instant) and the user fills it
 * in: tap a step to take it as-is, tap the pencil to reword it first, or write your own at the
 * bottom. Steps already on the goal never reappear, so the card empties itself as the plan fills up.
 *
 * [onAdd] receives the final title (edited or not). [existingTitles] are the goal's current
 * milestones.
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
    val coach = remember(category) { CoachPersona.getByCategory(category) }
    val suggestions = remember(goalTitle, category, description, existingTitles) {
        MilestoneCoach.suggest(
            title = goalTitle,
            category = category,
            description = description,
            existingTitles = existingTitles,
        )
    }

    // Editing state: which suggestion is being reworded, and the draft text for it.
    var editingIndex by remember(suggestions) { mutableStateOf<Int?>(null) }
    var draft by remember { mutableStateOf("") }
    var ownStep by remember { mutableStateOf("") }
    var showAll by remember(goalTitle) { mutableStateOf(false) }

    val visible = if (showAll) suggestions else suggestions.take(3)

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
                    Modifier.size(36.dp).clip(CircleShape).background(c.secondary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, tint = c.secondary, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (existingTitles.isEmpty()) "${coach.name} drafted your first steps"
                        else "${coach.name} would add",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = c.textPrimary,
                    )
                    Text(
                        MilestoneCoach.opener(coach.name, category),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
            }

            if (suggestions.isEmpty()) {
                Text(
                    "You've taken every step ${coach.name} had. Write your own below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textTertiary,
                )
            }

            visible.forEachIndexed { index, suggestion ->
                val isEditing = editingIndex == index
                if (isEditing) {
                    StepField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = suggestion,
                        onSubmit = {
                            val text = draft.trim().ifBlank { suggestion }
                            haptic.success()
                            onAdd(text)
                            editingIndex = null
                            draft = ""
                        },
                    )
                } else {
                    SuggestionRow(
                        text = suggestion,
                        index = index,
                        onTake = { haptic.success(); onAdd(suggestion) },
                        onEdit = { editingIndex = index; draft = suggestion },
                    )
                }
            }

            if (suggestions.size > 3) {
                Row(
                    modifier = Modifier.bouncyClickable { showAll = !showAll },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(if (showAll) PhosphorIcons.Regular.CaretUp else PhosphorIcons.Regular.CaretDown, contentDescription = null, tint = c.primary, modifier = Modifier.size(14.dp))
                    Text(
                        if (showAll) "Fewer ideas" else "More ideas from ${coach.name}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = c.primary,
                    )
                }
            }

            StepField(
                value = ownStep,
                onValueChange = { ownStep = it },
                placeholder = "Write your own step…",
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

/** One suggested step: tap the row (or the +) to take it, the pencil to reword it first. */
@Composable
private fun SuggestionRow(text: String, index: Int, onTake: () -> Unit, onEdit: () -> Unit) {
    val c = MaterialTheme.modernColors
    // Each row settles in a beat after the one above it, so the draft reads as being written out.
    val enter = remember { Animatable(0f) }
    LaunchedEffect(text) {
        enter.snapTo(0f)
        enter.animateTo(1f, tween(durationMillis = 260, delayMillis = index * 70))
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = enter.value }
            .bouncyClickable(onClick = onTake),
        color = c.secondary.copy(alpha = 0.07f),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(PhosphorIcons.Regular.Plus, contentDescription = "Add this step", tint = c.secondary, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
            Icon(
                PhosphorIcons.Regular.Pencil,
                contentDescription = "Reword this step",
                tint = c.textTertiary,
                modifier = Modifier.bouncyClickable(onClick = onEdit).padding(2.dp).size(18.dp),
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
