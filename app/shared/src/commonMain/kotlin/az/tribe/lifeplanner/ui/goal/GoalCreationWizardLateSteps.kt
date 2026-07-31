package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.service.MilestoneCoach
import az.tribe.lifeplanner.ui.components.backgroundColor
import az.tribe.lifeplanner.ui.utils.formatHuman
import kotlinx.datetime.LocalDate

@Composable
internal fun SelectionStep(
    options: List<GoalOption>,
    councilNotes: List<Pair<String, String>> = emptyList(),
    onSelect: (GoalOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose your path",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Pick the version that fits where you are right now",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (options.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            options.forEach { option ->
                GoalOptionCard(option = option, onSelect = { onSelect(option) })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (councilNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coach council",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            councilNotes.forEach { (coachLabel, note) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = coachLabel, style = MaterialTheme.typography.labelMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
internal fun GoalOptionCard(option: GoalOption, onSelect: () -> Unit) {
    val timelineColor = when (option.timeline) {
        GoalTimeline.SHORT_TERM -> Color(0xFF4CAF50)
        GoalTimeline.MID_TERM -> Color(0xFF2196F3)
        GoalTimeline.LONG_TERM -> Color(0xFF9C27B0)
    }
    val timelineLabel = when (option.timeline) {
        GoalTimeline.SHORT_TERM -> "1-3 months"
        GoalTimeline.MID_TERM -> "3-9 months"
        GoalTimeline.LONG_TERM -> "9+ months"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(50), color = timelineColor.copy(alpha = 0.15f)) {
                    Text(
                        text = option.focus,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = timelineColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)) {
                    Text(
                        text = timelineLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = option.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    text = option.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (option.milestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                option.milestones.take(3).forEach { milestone ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(timelineColor.copy(alpha = 0.7f), CircleShape))
                        Text(
                            text = milestone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (option.milestones.size > 3) {
                    Text(
                        text = "+${option.milestones.size - 3} more milestones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 14.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = timelineColor)
            ) {
                Text("Choose this path →", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun DetailsStep(
    isAiPath: Boolean,
    goalTitle: String,
    onTitleChange: (String) -> Unit,
    goalDescription: String,
    onDescriptionChange: (String) -> Unit,
    goalCategory: GoalCategory,
    onCategoryChange: (GoalCategory) -> Unit,
    selectedValueTitle: String?,
    onValueClick: () -> Unit,
    canProceed: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        if (isAiPath) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF7C4DFF).copy(alpha = 0.1f),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(PhosphorIcons.Regular.Sparkle, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(14.dp))
                    Text("AI generated · feel free to edit", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7C4DFF), fontWeight = FontWeight.Medium)
                }
            }
        }

        Text(
            text = if (isAiPath) "Here's your goal" else "Define your goal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review and adjust the details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Goal Title", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = goalTitle,
            onValueChange = onTitleChange,
            placeholder = { Text("e.g. Run a marathon by December") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = goalDescription,
            onValueChange = onDescriptionChange,
            placeholder = { Text("What does success look like?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalCategory.entries.forEach { cat ->
                val isSelected = cat == goalCategory
                val color = cat.backgroundColor()
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.15f),
                        selectedLabelColor = color
                    ),
                    border = if (isSelected) FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = true,
                        selectedBorderColor = color.copy(alpha = 0.4f),
                        selectedBorderWidth = 1.dp
                    ) else FilterChipDefaults.filterChipBorder(enabled = true, selected = false),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            if (selectedValueTitle != null) "Why this goal? · auto-linked, tap to change" else "Why this goal? (optional)",
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onValueClick),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValueTitle ?: "Link to a life value",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedValueTitle != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            enabled = canProceed,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Next \u2192", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
internal fun TimelineStep(
    selectedTimeline: GoalTimeline,
    onTimelineSelect: (GoalTimeline) -> Unit,
    dueDate: LocalDate,
    onDatePickerClick: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "When do you want to reach this?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Choose a horizon that fits your goal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TimelineCard("\uD83C\uDFAF", "Quick Win", "1, 3 months", "Build early momentum and see results fast", GoalTimeline.SHORT_TERM, selectedTimeline == GoalTimeline.SHORT_TERM) { onTimelineSelect(GoalTimeline.SHORT_TERM) }
            TimelineCard("\uD83D\uDCC8", "Growth Journey", "3, 9 months", "Steady, meaningful progress over time", GoalTimeline.MID_TERM, selectedTimeline == GoalTimeline.MID_TERM) { onTimelineSelect(GoalTimeline.MID_TERM) }
            TimelineCard("\uD83C\uDFD4", "Big Vision", "9 months, 2 years", "A life-changing, transformational goal", GoalTimeline.LONG_TERM, selectedTimeline == GoalTimeline.LONG_TERM) { onTimelineSelect(GoalTimeline.LONG_TERM) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Target Date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable { onDatePickerClick() },
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dueDate.formatHuman(), style = MaterialTheme.typography.bodyLarge)
                Icon(PhosphorIcons.Regular.Sparkle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
        Text("Auto-suggested, tap to change.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Next \u2192", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TimelineCard(
    emoji: String, label: String, period: String, description: String,
    timeline: GoalTimeline, selected: Boolean, onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(period, style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
            if (selected) Icon(PhosphorIcons.Regular.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
internal fun MilestonesStep(
    isAiPath: Boolean,
    aiMilestones: List<Pair<String, Boolean>>,
    onToggleAiMilestone: (Int) -> Unit,
    goalTitle: String,
    goalCategory: GoalCategory,
    goalDescription: String,
    customMilestones: List<String>,
    onRemoveCustom: (Int) -> Unit,
    onAddSuggested: (String) -> Unit,
    customInput: String,
    onCustomInputChange: (String) -> Unit,
    onAddCustom: () -> Unit,
    onNext: () -> Unit
) {
    // On the hand-written path there is no AI draft, so the category coach opens with steps read off
    // the title the user just typed (local and instant, see MilestoneCoach). Tap to take one.
    val coach = remember(goalCategory) { CoachPersona.getByCategory(goalCategory) }
    val coachSuggestions = remember(goalTitle, goalCategory, goalDescription) {
        if (isAiPath) emptyList()
        else MilestoneCoach.suggest(title = goalTitle, category = goalCategory, description = goalDescription)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text("Break it into steps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Milestones make big goals achievable. Optional, you can add more later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isAiPath && aiMilestones.isNotEmpty()) {
            Surface(shape = RoundedCornerShape(50), color = Color(0xFF7C4DFF).copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(PhosphorIcons.Regular.Sparkle, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(14.dp))
                    Text("AI suggested, tap to toggle", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7C4DFF), fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                aiMilestones.forEachIndexed { idx, (title, selected) ->
                    MilestoneToggleItem(title = title, selected = selected, onClick = { onToggleAiMilestone(idx) })
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (coachSuggestions.isNotEmpty()) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(PhosphorIcons.Regular.Sparkle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text("${coach.name} suggests, tap to add", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                MilestoneCoach.opener(coach.name, goalCategory),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                coachSuggestions.forEach { suggestion ->
                    val takenAt = customMilestones.indexOfFirst { it.equals(suggestion, ignoreCase = true) }
                    MilestoneToggleItem(
                        title = suggestion,
                        selected = takenAt >= 0,
                        onClick = { if (takenAt >= 0) onRemoveCustom(takenAt) else onAddSuggested(suggestion) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = if ((isAiPath && aiMilestones.isNotEmpty()) || coachSuggestions.isNotEmpty()) "Add your own" else "Add milestones",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customInput,
                onValueChange = onCustomInputChange,
                placeholder = { Text("Add a milestone...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            IconButton(
                onClick = onAddCustom,
                enabled = customInput.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (customInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(PhosphorIcons.Regular.Plus, null, tint = if (customInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (customMilestones.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                customMilestones.forEachIndexed { idx, title ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        IconButton(onClick = { onRemoveCustom(idx) }, modifier = Modifier.size(28.dp)) {
                            Icon(PhosphorIcons.Regular.X, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Next \u2192", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }

        TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("Skip milestones \u2192", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun MilestoneToggleItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

