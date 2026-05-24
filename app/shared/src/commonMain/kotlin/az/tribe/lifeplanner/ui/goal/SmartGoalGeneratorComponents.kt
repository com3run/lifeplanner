package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.theme.modernColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SmartGeneratorTopBar(
    currentStep: GeneratorStep,
    onBackClick: () -> Unit
) {
    val progress = when (currentStep) {
        GeneratorStep.SCENARIO_SELECT -> 0f
        GeneratorStep.CUSTOM_INPUT -> 0.2f
        GeneratorStep.QUESTIONS -> 0.5f
        GeneratorStep.GENERATING -> 0.75f
        GeneratorStep.RESULTS -> 1f
    }

    Column {
        TopAppBar(
            title = {
                Text(
                    when (currentStep) {
                        GeneratorStep.SCENARIO_SELECT -> "AI Goal Generator"
                        GeneratorStep.CUSTOM_INPUT -> "Describe Your Goals"
                        GeneratorStep.QUESTIONS -> "Answer Questions"
                        GeneratorStep.GENERATING -> "Creating Goals"
                        GeneratorStep.RESULTS -> "Your Goals"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                if (currentStep != GeneratorStep.GENERATING) {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Progress indicator
        val animatedProgress by animateFloatAsState(progress, label = "progress")
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
internal fun ScenarioCard(
    scenario: LifeScenario,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        if (enabled) scenario.gradientColors
                        else scenario.gradientColors.map { it.copy(alpha = 0.4f) }
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = scenario.icon,
                    fontSize = 32.sp
                )

                Column {
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = scenario.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun GeneratingStep() {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                PhosphorIcons.Regular.Sparkle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Creating your goals${".".repeat(dotCount)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AI is crafting personalized goals with milestones tailored just for you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.modernColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
internal fun GeneratedGoalCard(
    goal: Goal,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    val categoryColor = goal.category.toColor()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isAdded) 0.dp else 2.dp,
        border = if (isAdded) BorderStroke(2.dp, Color(0xFF4CAF50)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = categoryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = goal.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (isAdded) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Check,
                            contentDescription = "Added",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.modernColors.textSecondary
            )

            if (goal.milestones.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${goal.milestones.size} milestones:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    goal.milestones.take(3).forEach { milestone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.modernColors.textSecondary
                            )
                        }
                    }
                    if (goal.milestones.size > 3) {
                        Text(
                            text = "+${goal.milestones.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                    }
                }
            }

            if (!isAdded) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
                ) {
                    Icon(PhosphorIcons.Regular.Plus, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add This Goal")
                }
            }
        }
    }
}

internal fun GoalCategory.toColor(): Color = when (this) {
    GoalCategory.CAREER -> Color(0xFF667EEA)
    GoalCategory.MONEY -> Color(0xFFF7971E)
    GoalCategory.BODY -> Color(0xFFED213A)
    GoalCategory.PEOPLE -> Color(0xFF4776E6)
    GoalCategory.WELLBEING -> Color(0xFF11998E)
    GoalCategory.PURPOSE -> Color(0xFF8E54E9)
    GoalCategory.FAMILY -> Color(0xFFF57C00)
}
