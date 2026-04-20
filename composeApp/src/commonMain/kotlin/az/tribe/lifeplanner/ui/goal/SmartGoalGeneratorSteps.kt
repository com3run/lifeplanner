package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.PlusCircle
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.WifiSlash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.theme.modernColors
import kotlinx.coroutines.delay

@Composable
internal fun ScenarioSelectStep(
    scenarios: List<LifeScenario>,
    isOffline: Boolean,
    onScenarioSelected: (LifeScenario) -> Unit,
    onCustomClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // AI hero section
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Pick a path, AI does the rest",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Select a life moment and we'll create personalized goals with milestones instantly",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.modernColors.textSecondary
                )
            }
        }

        if (isOffline) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            PhosphorIcons.Regular.WifiSlash,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "You're offline. Goal generation requires internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        items(scenarios.chunked(2)) { rowScenarios ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowScenarios.forEach { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        enabled = !isOffline,
                        onClick = { onScenarioSelected(scenario) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowScenarios.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Custom option
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onCustomClick
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Regular.PencilSimple,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Something else",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Describe your goals in your own words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.modernColors.textSecondary
                        )
                    }

                    Icon(
                        PhosphorIcons.Regular.ArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
internal fun CustomInputStep(
    prompt: String,
    isOffline: Boolean,
    onPromptChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tell us about your goals",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Describe what you want to achieve and AI will create goals with milestones for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.modernColors.textSecondary
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            BasicTextField(
                value = prompt,
                onValueChange = onPromptChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.modernColors.textPrimary
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (prompt.isEmpty()) {
                            Text(
                                text = "Example: I want to get healthier, save more money, spend quality time with family, and advance in my career...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.modernColors.textSecondary.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        if (isOffline) {
            Text(
                text = "Goal generation requires internet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = prompt.length >= 10 && !isOffline
        ) {
            Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate My Goals", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun QuestionsStep(
    questions: List<az.tribe.lifeplanner.data.model.GoalTypeQuestions>,
    onAnswerQuestion: (String, String) -> Unit,
    isQuestionnaireComplete: Boolean,
    onSubmitAnswers: () -> Unit
) {
    val allQuestions = questions.flatMap { goalType ->
        goalType.questions.map { q -> goalType.goalType to q }
    }
    val answers = remember { mutableStateMapOf<String, String>() }
    var currentIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (allQuestions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val (goalType, question) = allQuestions[currentIndex]

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${allQuestions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.modernColors.textSecondary
                    )
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / allQuestions.size },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Goal type chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = goalType.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Question title
                Text(
                    text = question.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Options
                question.options.forEach { option ->
                    val isSelected = answers[question.title] == option
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        onClick = {
                            answers[question.title] = option
                            onAnswerQuestion(question.title, option)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    answers[question.title] = option
                                    onAnswerQuestion(question.title, option)
                                }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { currentIndex-- },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Previous")
                    }
                }

                val isLastQuestion = currentIndex == allQuestions.size - 1
                val hasCurrentAnswer = answers.containsKey(allQuestions[currentIndex].second.title)

                Button(
                    onClick = {
                        if (isLastQuestion) {
                            onSubmitAnswers()
                        } else {
                            currentIndex++
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = hasCurrentAnswer
                ) {
                    if (isLastQuestion) {
                        Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Goals", fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Next", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(PhosphorIcons.Regular.ArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun ResultsStep(
    goals: List<Goal>,
    onAddGoal: (Goal) -> Unit,
    onAddAll: () -> Unit,
    onComplete: () -> Unit
) {
    var addedGoals by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showCelebration = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = showCelebration,
                enter = fadeIn() + scaleIn(initialScale = 0.8f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Icon(PhosphorIcons.Regular.Star, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${goals.size} Goals Created!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Here are your personalized goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.modernColors.textSecondary
                    )
                }
            }
        }

        items(goals) { goal ->
            GeneratedGoalCard(
                goal = goal,
                isAdded = addedGoals.contains(goal.id),
                onAdd = {
                    onAddGoal(goal)
                    addedGoals = addedGoals + goal.id
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (addedGoals.size < goals.size) {
                    Button(
                        onClick = {
                            onAddAll()
                            addedGoals = goals.map { it.id }.toSet()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(PhosphorIcons.Regular.PlusCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add All Goals", fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (addedGoals.isNotEmpty()) "Done - View My Goals"
                        else "Skip for Now",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
