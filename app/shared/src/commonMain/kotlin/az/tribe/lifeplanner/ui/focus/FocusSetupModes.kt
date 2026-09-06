package az.tribe.lifeplanner.ui.focus

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.FocusTheme
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.gradientColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Infinity
import com.adamglin.phosphoricons.regular.Timer

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FocusFreeFlowSetup(
    focusViewModel: FocusViewModel,
    onStartFocus: () -> Unit
) {
    val milestoneItems by focusViewModel.milestoneItems.collectAsStateWithLifecycle()
    val selectedMilestone by focusViewModel.selectedMilestone.collectAsStateWithLifecycle()
    val selectedGoal by focusViewModel.selectedGoal.collectAsStateWithLifecycle()
    val todaySeconds by focusViewModel.todaySeconds.collectAsStateWithLifecycle()
    val allTimeSeconds by focusViewModel.allTimeSeconds.collectAsStateWithLifecycle()
    val selectedMood by focusViewModel.selectedMood.collectAsStateWithLifecycle()
    val selectedFocusTheme by focusViewModel.selectedFocusTheme.collectAsStateWithLifecycle()

    val freeFlowGoals = remember(milestoneItems) {
        milestoneItems.map { it.goal }.distinctBy { it.id }
    }
    val selectedGoalMilestones = remember(selectedGoal, milestoneItems) {
        selectedGoal?.let { goal ->
            milestoneItems.filter { it.goal.id == goal.id }
        } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = LifePlannerDesign.Padding.screenHorizontal,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TimerModeToggle(
                    isFreeFlow = true,
                    onModeChange = { focusViewModel.setTimerMode(it) }
                )
            }

            if (todaySeconds > 0 || allTimeSeconds > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (todaySeconds > 0) {
                            Text(
                                "${formatDuration(todaySeconds)} today",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFF6B35),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (todaySeconds > 0 && allTimeSeconds > 0) {
                            Text(
                                "  •  ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (allTimeSeconds > 0) {
                            Text(
                                "${formatDuration(allTimeSeconds)} all time",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Visual Theme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FocusTheme.entries.forEach { theme ->
                        val isSelected = selectedFocusTheme == theme
                        Surface(
                            onClick = { focusViewModel.setFocusTheme(theme) },
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFFFF6B35).copy(alpha = 0.15f) else Color.Transparent,
                            modifier = if (isSelected) Modifier.border(2.dp, Color(0xFFFF6B35), CircleShape) else Modifier
                        ) {
                            Text(
                                theme.icon,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }

            if (selectedFocusTheme != FocusTheme.DEFAULT) {
                item {
                    ThemePreviewCard(theme = selectedFocusTheme, mood = selectedMood)
                }
            }

            if (freeFlowGoals.isNotEmpty()) {
                item {
                    Text(
                        "Working on",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        freeFlowGoals.forEach { goal ->
                            val isSelected = selectedGoal?.id == goal.id
                            val categoryColors = goal.category.gradientColors()
                            Surface(
                                onClick = { focusViewModel.selectGoalOnly(goal) },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) categoryColors.first().copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = if (isSelected) Modifier.border(
                                    1.5.dp,
                                    Brush.horizontalGradient(categoryColors),
                                    RoundedCornerShape(50)
                                ) else Modifier
                            ) {
                                Text(
                                    goal.title,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) categoryColors.first()
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (selectedGoalMilestones.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Pick a milestone",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "optional",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(selectedGoalMilestones, key = { it.milestone.id }) { item ->
                        MilestonePickerItem(
                            milestoneItem = item,
                            isSelected = selectedMilestone?.id == item.milestone.id,
                            onClick = {
                                focusViewModel.selectMilestoneWithGoal(item.milestone, item.goal)
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        Button(
            onClick = onStartFocus,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LifePlannerDesign.Padding.screenHorizontal, vertical = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
        ) {
            Icon(PhosphorIcons.Regular.Infinity, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Start Focusing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FocusTimedSetup(
    focusViewModel: FocusViewModel,
    onStartFocus: () -> Unit
) {
    val milestoneItems by focusViewModel.milestoneItems.collectAsStateWithLifecycle()
    val selectedMilestone by focusViewModel.selectedMilestone.collectAsStateWithLifecycle()
    val durationMinutes by focusViewModel.durationMinutes.collectAsStateWithLifecycle()
    val todaySessionCount by focusViewModel.todaySessionCount.collectAsStateWithLifecycle()
    val todaySeconds by focusViewModel.todaySeconds.collectAsStateWithLifecycle()
    val allTimeSessionCount by focusViewModel.allTimeSessionCount.collectAsStateWithLifecycle()
    val allTimeSeconds by focusViewModel.allTimeSeconds.collectAsStateWithLifecycle()
    val selectedMood by focusViewModel.selectedMood.collectAsStateWithLifecycle()
    val selectedFocusTheme by focusViewModel.selectedFocusTheme.collectAsStateWithLifecycle()
    val isCustomDuration by focusViewModel.isCustomDuration.collectAsStateWithLifecycle()
    val customDurationMinutes by focusViewModel.customDurationMinutes.collectAsStateWithLifecycle()

    val canStart = selectedMilestone != null

    val groupedMilestones = remember(milestoneItems) { milestoneItems.groupBy { it.goal } }
    val suggestedMilestones = remember(milestoneItems) { milestoneItems.sortedBy { it.goal.dueDate }.take(3) }
    val suggestedIds = remember(suggestedMilestones) { suggestedMilestones.map { it.milestone.id }.toSet() }
    val remainingMilestones = remember(milestoneItems, suggestedIds) { milestoneItems.filter { it.milestone.id !in suggestedIds } }

    var showAllMilestones by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = LifePlannerDesign.Padding.screenHorizontal, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TimerModeToggle(isFreeFlow = false, onModeChange = { focusViewModel.setTimerMode(it) })
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (todaySessionCount > 0 || todaySeconds > 0) {
                        Text("Today", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("$todaySessionCount", "sessions")
                            StatItem(formatDuration(todaySeconds), "focused")
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    if (allTimeSessionCount > 0 || allTimeSeconds > 0) {
                        Text("All Time", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("$allTimeSessionCount", "sessions")
                            StatItem(formatDuration(allTimeSeconds), "focused")
                        }
                    }
                }
            }
        }

        item { Text("Visual Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FocusTheme.entries.forEach { theme ->
                    val isSelected = selectedFocusTheme == theme
                    Surface(
                        onClick = { focusViewModel.setFocusTheme(theme) },
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFFFF6B35).copy(alpha = 0.15f) else Color.Transparent,
                        modifier = if (isSelected) Modifier.border(2.dp, Color(0xFFFF6B35), CircleShape) else Modifier
                    ) {
                        Text(theme.icon, modifier = Modifier.padding(12.dp), fontSize = 24.sp)
                    }
                }
            }
        }

        if (selectedFocusTheme != FocusTheme.DEFAULT) {
            item { ThemePreviewCard(theme = selectedFocusTheme, mood = selectedMood) }
        }

        item { Text("What will you focus on?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

        if (milestoneItems.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No milestones to focus on", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Create a goal with milestones to start a focus session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            if (suggestedMilestones.isNotEmpty()) {
                item { Text("Suggested", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF6B35)) }
                items(suggestedMilestones, key = { "suggested_${it.milestone.id}" }) { item ->
                    MilestonePickerItem(milestoneItem = item, isSelected = selectedMilestone?.id == item.milestone.id, onClick = { focusViewModel.selectMilestoneWithGoal(item.milestone, item.goal) })
                }
            }

            if (remainingMilestones.isNotEmpty()) {
                item {
                    Surface(
                        onClick = { showAllMilestones = !showAllMilestones },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("All milestones", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(if (showAllMilestones) "Hide" else "${remainingMilestones.size} more", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (showAllMilestones) {
                    groupedMilestones.forEach { (goal, items) ->
                        val filtered = items.filter { it.milestone.id !in suggestedIds }
                        if (filtered.isNotEmpty()) {
                            item(key = "goal_header_${goal.id}") { GoalSectionHeader(goal = goal) }
                            items(filtered, key = { it.milestone.id }) { item ->
                                MilestonePickerItem(milestoneItem = item, isSelected = selectedMilestone?.id == item.milestone.id, onClick = { focusViewModel.selectMilestoneWithGoal(item.milestone, item.goal) })
                            }
                        }
                    }
                }
            }
        }

        if (milestoneItems.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                val presets = listOf(15, 25, 30, 45, 60)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        DurationChip(minutes = preset, isSelected = durationMinutes == preset && !isCustomDuration, onClick = { focusViewModel.setDuration(preset) })
                    }
                    PickerChip(label = "Custom", isSelected = isCustomDuration, onClick = { focusViewModel.toggleCustomDuration() })
                }
            }

            if (isCustomDuration) {
                item {
                    CustomDurationStepper(
                        minutes = customDurationMinutes,
                        onIncrement = { focusViewModel.incrementCustomDuration() },
                        onDecrement = { focusViewModel.decrementCustomDuration() }
                    )
                }
            }
        }

        if (milestoneItems.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onStartFocus,
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Icon(PhosphorIcons.Regular.Timer, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
