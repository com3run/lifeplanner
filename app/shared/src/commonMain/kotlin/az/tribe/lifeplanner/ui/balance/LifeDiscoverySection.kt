package az.tribe.lifeplanner.ui.balance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.LifeArea
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle

// ─── Main entry point ─────────────────────────────────────────────────────────

@Composable
fun DiscoverySection(
    state: DiscoveryState,
    onStart: () -> Unit,
    onSubmitAnswer: (String) -> Unit,
    onAddGoal: (DiscoveryGoal) -> Unit,
    onCreateHabit: (LifeArea) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state.phase) {
            DiscoveryPhase.IDLE -> DiscoveryInviteCard(onStart = onStart)

            DiscoveryPhase.GENERATING_QUESTION -> {
                PastExchanges(exchanges = state.exchanges)
                DiscoveryLoadingCard(text = "Crafting your next question…")
            }

            DiscoveryPhase.ACTIVE -> {
                PastExchanges(exchanges = state.exchanges)
                ActiveQuestionCard(
                    question = state.currentQuestion,
                    area = state.currentArea,
                    questionNumber = state.exchanges.size + 1,
                    totalQuestions = 2,
                    onSubmit = onSubmitAnswer
                )
            }

            DiscoveryPhase.REFLECTING -> {
                PastExchanges(exchanges = state.exchanges.dropLast(1))
                ReflectionCard(
                    exchange = state.exchanges.lastOrNull(),
                    streamedText = state.streamedText
                )
            }

            DiscoveryPhase.GENERATING_OUTCOME -> {
                PastExchanges(exchanges = state.exchanges)
                DiscoveryLoadingCard(text = "Building your personalized plan…")
            }

            DiscoveryPhase.DONE -> {
                PastExchanges(exchanges = state.exchanges)
                DiscoveryResultsCard(
                    goals = state.generatedGoals,
                    addedIds = state.addedIds,
                    onAddGoal = onAddGoal,
                    onCreateHabit = onCreateHabit,
                    onReset = onReset
                )
            }
        }
    }
}

// ─── Invite card ──────────────────────────────────────────────────────────────

@Composable
private fun DiscoveryInviteCard(onStart: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
    ) {
        Box(
            modifier = Modifier.size(180.dp).align(Alignment.TopEnd)
                .background(Color(0xFF7B61FF).copy(alpha = 0.08f), CircleShape)
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B61FF).copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null,
                        modifier = Modifier.size(13.dp), tint = Color(0xFF9B85FF))
                    Text("AI-Powered", style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9B85FF), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Discover What\nDrives You",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 34.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Answer 2 thoughtful questions. Your coach AI will find patterns in your answers and build a plan made just for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
            ) {
                Text("Start Exploring", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Icon(PhosphorIcons.Regular.ArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── Active question card ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveQuestionCard(
    question: String,
    area: LifeArea?,
    questionNumber: Int,
    totalQuestions: Int,
    onSubmit: (String) -> Unit
) {
    var answer by remember(question) { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val areaColor = area?.let { getAreaColor(it, isDark = true) } ?: Color(0xFF7B61FF)
    val quickAnswers = area?.let { quickChipsFor(it) } ?: emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // Area + question number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                area?.let { a ->
                    Surface(shape = RoundedCornerShape(8.dp), color = areaColor.copy(alpha = 0.18f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(a.icon, fontSize = 13.sp)
                            Text(a.displayName, style = MaterialTheme.typography.labelSmall,
                                color = areaColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("$questionNumber / $totalQuestions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // The question
            Text(
                question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // Quick-fill chips
            if (quickAnswers.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickAnswers.forEach { chip ->
                        val selected = answer == chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) areaColor.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .border(1.dp,
                                    if (selected) areaColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    RoundedCornerShape(20.dp))
                                .clickable { answer = if (selected) "" else chip }
                        ) {
                            Text(chip, style = MaterialTheme.typography.labelSmall,
                                color = if (selected) areaColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Text field
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Share your thoughts…", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = areaColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    if (answer.isNotBlank()) {
                        keyboard?.hide()
                        onSubmit(answer.trim())
                    }
                },
                enabled = answer.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = areaColor)
            ) {
                Text("Continue", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Icon(PhosphorIcons.Regular.ArrowRight, contentDescription = null,
                    modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── Reflection card (streaming) ──────────────────────────────────────────────

@Composable
private fun ReflectionCard(exchange: DiscoveryExchange?, streamedText: String) {
    val inf = rememberInfiniteTransition(label = "dots")
    val pulse by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "p")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            exchange?.let { ex ->
                val areaColor = getAreaColor(ex.area, isDark = true)
                Surface(shape = RoundedCornerShape(8.dp), color = areaColor.copy(alpha = 0.18f)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(ex.area.icon, fontSize = 13.sp)
                        Text(ex.area.displayName, style = MaterialTheme.typography.labelSmall,
                            color = areaColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)) {
                    Text(
                        "\"${ex.answer}\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            if (streamedText.isNotEmpty()) {
                AnimatedVisibility(visible = true,
                    enter = fadeIn() + slideInVertically()) {
                    Text(streamedText, style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    Box(modifier = Modifier.size(5.dp)
                        .alpha(((pulse + i * 0.33f) % 1f))
                        .background(Color(0xFF9B85FF), CircleShape))
                }
                Spacer(Modifier.width(6.dp))
                Text("Reflecting…", style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9B85FF).copy(alpha = 0.7f))
            }
        }
    }
}

// ─── Results card ─────────────────────────────────────────────────────────────

@Composable
private fun DiscoveryResultsCard(
    goals: List<DiscoveryGoal>,
    addedIds: Set<String>,
    onAddGoal: (DiscoveryGoal) -> Unit,
    onCreateHabit: (LifeArea) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null,
                    modifier = Modifier.size(18.dp), tint = Color(0xFF9B85FF))
                Text("Your Personalized Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("Based on your answers, here's what I found for you.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (goals.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Try refreshing or explore again for new insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            } else {
                Spacer(Modifier.height(16.dp))
                goals.forEach { dGoal ->
                    DiscoveryGoalItem(
                        goal = dGoal,
                        isAdded = addedIds.contains(dGoal.id),
                        onAdd = { onAddGoal(dGoal) },
                        onCreateHabit = { onCreateHabit(dGoal.area) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onReset, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Explore Again", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DiscoveryGoalItem(
    goal: DiscoveryGoal,
    isAdded: Boolean,
    onAdd: () -> Unit,
    onCreateHabit: () -> Unit
) {
    val areaColor = getAreaColor(goal.area, isDark = true)
    val isGoal = goal.type == "goal"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = areaColor.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(36.dp)
                    .background(areaColor.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Text(goal.area.icon, fontSize = 18.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = areaColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 3.dp)) {
                        Text(if (isGoal) "Goal" else "Habit",
                            style = MaterialTheme.typography.labelSmall,
                            color = areaColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                    }
                }
            }

            if (goal.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(goal.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3,
                    lineHeight = 19.sp)
            }

            Spacer(Modifier.height(10.dp))

            if (isGoal) {
                Button(
                    onClick = onAdd,
                    enabled = !isAdded,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isAdded) ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        disabledContentColor = Color(0xFF4CAF50)
                    ) else ButtonDefaults.buttonColors(containerColor = areaColor)
                ) {
                    if (isAdded) {
                        Icon(PhosphorIcons.Regular.Check, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Added to Goals", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(PhosphorIcons.Regular.Plus, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Add to My Goals", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = onCreateHabit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = areaColor)
                ) {
                    Icon(PhosphorIcons.Regular.Plus, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Create Habit", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Past exchanges ───────────────────────────────────────────────────────────

@Composable
private fun PastExchanges(exchanges: List<DiscoveryExchange>) {
    exchanges.forEach { ex ->
        CompletedExchangeChip(exchange = ex)
    }
}

@Composable
private fun CompletedExchangeChip(exchange: DiscoveryExchange) {
    val areaColor = getAreaColor(exchange.area, isDark = true)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(28.dp)
                .background(areaColor.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center) {
                Text(exchange.area.icon, fontSize = 13.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(exchange.question, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Text("\"${exchange.answer}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2)
            }
            Icon(PhosphorIcons.Regular.Check, contentDescription = null,
                modifier = Modifier.size(14.dp).padding(top = 2.dp),
                tint = Color(0xFF4CAF50))
        }
    }
}

// ─── Loading card ─────────────────────────────────────────────────────────────

@Composable
private fun DiscoveryLoadingCard(text: String) {
    val inf = rememberInfiniteTransition(label = "load")
    val pulse by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "p")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Box(modifier = Modifier.size(6.dp)
                        .alpha(((pulse + i * 0.33f) % 1f))
                        .background(Color(0xFF9B85FF), CircleShape))
                }
            }
            Text(text, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Quick chips helper ───────────────────────────────────────────────────────

private fun quickChipsFor(area: LifeArea): List<String> = when (area) {
    LifeArea.CAREER -> listOf("Not enough time", "Unclear direction", "Low motivation", "Too many tasks")
    LifeArea.MONEY -> listOf("High spending", "No savings plan", "Low income", "Inconsistent")
    LifeArea.BODY -> listOf("No routine", "Low energy", "Busy schedule", "Lack of sleep")
    LifeArea.PEOPLE -> listOf("Always busy", "Geographic distance", "Drifted apart", "Hard to connect")
    LifeArea.WELLBEING -> listOf("Work stress", "Poor sleep", "No downtime", "Overthinking")
    LifeArea.PURPOSE -> listOf("Feeling lost", "Daily grind", "No creative outlet", "Uninspired")
}
