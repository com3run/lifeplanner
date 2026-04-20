package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Sparkle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachPersona

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WizardTopBar(
    stepLabel: String,
    progress: Float,
    showBack: Boolean,
    onBackClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stepLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                if (showBack) {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    }
}

@Composable
internal fun IntentStep(
    intentText: String,
    onIntentChange: (String) -> Unit,
    detectedCategory: GoalCategory,
    error: String?,
    isGeneratingQuestions: Boolean,
    onGenerateClick: () -> Unit,
    onManualClick: () -> Unit
) {
    val categoryCoach = remember(detectedCategory) { CoachPersona.getByCategory(detectedCategory) }
    val coach = if (intentText.length >= 4) categoryCoach else CoachPersona.getGeneral()

    val bgColor = remember(coach) {
        try { Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color.Unspecified }
    }

    val canProceed = intentText.trim().length >= 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = coach,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
        ) { c ->
            val cBg = try { Color(("FF" + c.avatar.backgroundColor.removePrefix("#")).toLong(16)) }
            catch (_: Exception) { MaterialTheme.colorScheme.primary }
            val cAc = try { Color(("FF" + c.avatar.accentColor.removePrefix("#")).toLong(16)) }
            catch (_: Exception) { MaterialTheme.colorScheme.tertiary }
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        Brush.radialGradient(listOf(cBg, cAc.copy(alpha = 0.6f))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (c.imageUrl != null) {
                    AsyncImage(
                        model = c.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(c.emoji, style = MaterialTheme.typography.displaySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = coach,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { c ->
            val cBg = try { Color(("FF" + c.avatar.backgroundColor.removePrefix("#")).toLong(16)) }
            catch (_: Exception) { MaterialTheme.colorScheme.primary }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = c.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${detectedCategory.name.lowercase().replaceFirstChar { it.uppercase() }} Coach",
                    style = MaterialTheme.typography.bodySmall,
                    color = cBg
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What goal do you have in mind?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Describe it in your own words — no need to be perfect",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = intentText.isEmpty()) {
            Column {
                Text(
                    text = "Popular ideas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GOAL_IDEAS) { idea ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { onIntentChange(idea) }
                        ) {
                            Text(
                                text = idea,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        OutlinedTextField(
            value = intentText,
            onValueChange = onIntentChange,
            placeholder = {
                Text(
                    "e.g. I want to get fit and run a marathon, or save money for a house...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )

        AnimatedVisibility(visible = intentText.length >= 3) {
            val cBg = if (bgColor != Color.Unspecified) bgColor else MaterialTheme.colorScheme.primary
            Surface(
                shape = RoundedCornerShape(50),
                color = cBg.copy(alpha = 0.12f),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "${coach.emoji}  ${detectedCategory.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelMedium,
                    color = cBg,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        AnimatedVisibility(visible = error != null) {
            error?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onGenerateClick,
            enabled = canProceed && !isGeneratingQuestions,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isGeneratingQuestions) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Preparing questions\u2026", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            } else {
                Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate with AI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onManualClick,
            enabled = !isGeneratingQuestions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Fill out manually \u2192",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
internal fun QuestionsStep(
    questions: List<WizardQuestion>,
    answers: List<List<String>>,
    onAnswerToggle: (index: Int, answer: String) -> Unit,
    error: String?,
    onContinue: () -> Unit
) {
    var currentIndex by remember(questions.size) { mutableStateOf(0) }

    val isLastQuestion = currentIndex == questions.size - 1
    val currentSelections = answers.getOrElse(currentIndex) { emptyList() }
    val hasSelection = currentSelections.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        if (questions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    questions.indices.forEach { i ->
                        val answered = answers.getOrElse(i) { emptyList() }.isNotEmpty()
                        Box(
                            modifier = Modifier
                                .size(if (i == currentIndex) 8.dp else 6.dp)
                                .background(
                                    color = when {
                                        answered -> MaterialTheme.colorScheme.primary
                                        i == currentIndex -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Text(
                text = "Select all that apply",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn(tween(250)) togetherWith
                                slideOutHorizontally { -it } + fadeOut(tween(200))
                    } else {
                        slideInHorizontally { -it } + fadeIn(tween(250)) togetherWith
                                slideOutHorizontally { it } + fadeOut(tween(200))
                    }
                },
                modifier = Modifier.weight(1f)
            ) { idx ->
                val q = questions.getOrNull(idx)
                if (q != null) {
                    val selectedForQ = answers.getOrElse(idx) { emptyList() }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            q.options.forEach { option ->
                                val isSelected = option in selectedForQ
                                val isNoneOption = option.startsWith("None of the above", ignoreCase = true)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = when {
                                                isSelected && isNoneOption -> MaterialTheme.colorScheme.outline
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.outlineVariant
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onAnswerToggle(idx, option) },
                                    color = when {
                                        isSelected && isNoneOption -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = when {
                                                isSelected && isNoneOption -> MaterialTheme.colorScheme.onSurfaceVariant
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (isSelected) PhosphorIcons.Regular.CheckCircle
                                                         else PhosphorIcons.Regular.Circle,
                                            contentDescription = null,
                                            tint = when {
                                                isSelected && isNoneOption -> MaterialTheme.colorScheme.outline
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (idx == questions.size - 1) {
                            AnimatedVisibility(visible = error != null) {
                                error?.let {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                    ) {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentIndex > 0) {
                    TextButton(onClick = { currentIndex-- }) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Back", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                if (isLastQuestion) {
                    Button(
                        onClick = onContinue,
                        enabled = hasSelection,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generate my goal →", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = { currentIndex++ },
                        enabled = hasSelection,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Next →", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

