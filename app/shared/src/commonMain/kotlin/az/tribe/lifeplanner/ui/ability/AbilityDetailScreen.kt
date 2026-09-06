package az.tribe.lifeplanner.ui.ability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.X
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_rename
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityDetailScreen(
    abilityId: String,
    onBackClick: () -> Unit,
    onGoalClick: (String) -> Unit = {},
    viewModel: AbilityDetailViewModel = koinViewModel(parameters = { parametersOf(abilityId) })
) {
    val ability by viewModel.ability.collectAsStateWithLifecycle()
    val linkedHabits by viewModel.linkedHabits.collectAsStateWithLifecycle()
    val allHabitsForLinking by viewModel.allHabitsForLinking.collectAsStateWithLifecycle()
    val linkedGoals by viewModel.linkedGoals.collectAsStateWithLifecycle()
    val allGoalsForLinking by viewModel.allGoalsForLinking.collectAsStateWithLifecycle()
    val supervisionInsight by viewModel.supervisionInsight.collectAsStateWithLifecycle()
    val isGeneratingInsight by viewModel.isGeneratingInsight.collectAsStateWithLifecycle()
    var showLinkHabitSheet by remember { mutableStateOf(false) }
    var showLinkGoalSheet by remember { mutableStateOf(false) }

    // Inline title editing
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    val titleFocusRequester = remember { FocusRequester() }

    // Seed titleInput when ability first loads (e.g. "New Ability" from quick-create)
    LaunchedEffect(ability?.title) {
        ability?.title?.let { t ->
            titleInput = t
            // Auto-open editing when the title is the default placeholder
            if (t == "New Ability") isEditingTitle = true
        }
    }
    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) titleFocusRequester.requestFocus()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        ability?.title ?: "Ability",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LifePlannerDesign.Padding.screenHorizontal, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // The ability written like a page, matching the other detail screens. The old hero
            // was a half-poster: a 56sp centered emoji, a centered bold title, a Level pill, and
            // an 8dp XP bar. Paper rules: the level is an overline said once, identity is a
            // byline row (still tap-to-rename), and progression is a thin line with the numbers
            // written next to it.
            ability?.let { ab ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "LEVEL ${ab.currentLevel}",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ab.iconEmoji, fontSize = 24.sp)
                        }
                        if (isEditingTitle) {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                singleLine = true,
                                placeholder = { Text("Name this ability…") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        viewModel.updateTitle(titleInput)
                                        isEditingTitle = false
                                    }) {
                                        Icon(PhosphorIcons.Regular.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    viewModel.updateTitle(titleInput)
                                    isEditingTitle = false
                                }),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(titleFocusRequester),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f).clickable {
                                    titleInput = ab.title
                                    isEditingTitle = true
                                }
                            ) {
                                Text(
                                    ab.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    PhosphorIcons.Regular.PencilSimple,
                                    contentDescription = stringResource(Res.string.cd_rename),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (ab.description.isNotBlank()) {
                        Text(
                            ab.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { ab.levelProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        "${ab.xpIntoCurrentLevel} / ${ab.xpForNextLevel} XP to Level ${ab.currentLevel + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Linked Habits ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Linked Habits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (allHabitsForLinking.isNotEmpty()) {
                    TextButton(onClick = { showLinkHabitSheet = true }) {
                        Icon(PhosphorIcons.Regular.Plus, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Habit")
                    }
                }
            }

            if (linkedHabits.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        "Link habits to start building this ability. Each check-in awards XP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    linkedHabits.forEach { (habit, link) ->
                        LinkedHabitRow(
                            habit = habit,
                            xpPerCheckIn = (10 * link.xpWeight).toInt(),
                            onUnlink = { viewModel.unlinkHabit(habit.id) }
                        )
                    }
                }
            }

            // ── Contributing To Goals ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Contributing To Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (allGoalsForLinking.isNotEmpty()) {
                    TextButton(onClick = { showLinkGoalSheet = true }) {
                        Icon(PhosphorIcons.Regular.Plus, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Link Goal")
                    }
                }
            }

            if (linkedGoals.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        "Link a goal to see how this ability drives your bigger outcomes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    linkedGoals.forEach { (goal, _) ->
                        LinkedGoalRow(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                            onUnlink = { viewModel.unlinkGoal(goal.id) }
                        )
                    }
                }
            }

            // The coaching read, without the production it used to arrive in (Sparkle-in-a-
            // circle avatar, "AI Coaching Insight" header, "Get personalized coaching to build
            // this ability faster"). The words are the value; staging an AI presence around them
            // is the same offer-card pattern the goal detail dropped. Generation stays behind a
            // deliberate tap because it costs a network call.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (supervisionInsight.isNotBlank()) {
                        Text(
                            supervisionInsight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isGeneratingInsight) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            when {
                                isGeneratingInsight -> "Reading your habits…"
                                supervisionInsight.isBlank() && linkedHabits.isEmpty() ->
                                    "Link a habit first, then there is something to read."
                                supervisionInsight.isBlank() ->
                                    "A coaching read on how to build this faster."
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isGeneratingInsight && linkedHabits.isNotEmpty()) {
                            TextButton(onClick = { viewModel.generateSupervisionInsight() }) {
                                Text(if (supervisionInsight.isBlank()) "Get insight" else "Refresh")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // Link habit bottom sheet
    if (showLinkHabitSheet) {
        ModalBottomSheet(onDismissRequest = { showLinkHabitSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Add Habit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                allHabitsForLinking.forEach { habit ->
                    ListItem(
                        headlineContent = { Text(habit.title) },
                        supportingContent = { Text("${habit.type.displayName} · ${habit.frequency.displayName}") },
                        trailingContent = {
                            TextButton(onClick = {
                                viewModel.linkHabit(habit.id)
                                showLinkHabitSheet = false
                            }) { Text("Link") }
                        }
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Link goal bottom sheet
    if (showLinkGoalSheet) {
        ModalBottomSheet(onDismissRequest = { showLinkGoalSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Link to Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                allGoalsForLinking
                    .filter { it.status != GoalStatus.COMPLETED }
                    .forEach { goal ->
                        ListItem(
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        PhosphorIcons.Regular.Flag,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp).size(16.dp)
                                    )
                                }
                            },
                            headlineContent = { Text(goal.title, maxLines = 1) },
                            supportingContent = { Text(goal.category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            trailingContent = {
                                TextButton(onClick = {
                                    viewModel.linkGoal(goal.id)
                                    showLinkGoalSheet = false
                                }) { Text("Link") }
                            }
                        )
                    }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LinkedHabitRow(
    habit: Habit,
    xpPerCheckIn: Int,
    onUnlink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(
                    "${habit.type.displayName} · +${xpPerCheckIn} XP per check-in",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onUnlink) {
                Icon(PhosphorIcons.Regular.X, "Unlink", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LinkedGoalRow(
    goal: Goal,
    onClick: () -> Unit,
    onUnlink: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                PhosphorIcons.Regular.Flag,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(
                    goal.status.name.lowercase().replaceFirstChar { it.uppercase() } + " · " + goal.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onUnlink) {
                Icon(PhosphorIcons.Regular.X, "Unlink", modifier = Modifier.size(18.dp))
            }
        }
    }
}
