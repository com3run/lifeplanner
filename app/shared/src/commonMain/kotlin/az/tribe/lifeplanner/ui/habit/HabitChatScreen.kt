package az.tribe.lifeplanner.ui.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.PaperPlaneRight
import org.koin.compose.viewmodel.koinViewModel
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_send

/**
 * Conversational habit setup: chat with the coach, tap to add proposed habits (dedup-guarded), and
 * they appear on the Habits screen. Replaces the multi-step generator as the friendly default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitChatScreen(
    onBackClick: () -> Unit,
    viewModel: HabitChatViewModel = koinViewModel(),
) {
    val c = MaterialTheme.modernColors
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val added by viewModel.addedTitles.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, sending) {
        val target = messages.size // + typing row
        if (target > 0) listState.animateScrollToItem(target)
    }

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("New habit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back), tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
        bottomBar = {
            Surface(color = c.background) {
                Row(
                    Modifier.fillMaxWidth().padding(LifePlannerDesign.Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Say what you'd like to build…") },
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    )
                    IconButton(
                        onClick = { viewModel.send(input); input = "" },
                        enabled = input.isNotBlank() && !sending,
                    ) {
                        Icon(
                            PhosphorIcons.Regular.PaperPlaneRight,
                            contentDescription = stringResource(Res.string.cd_send),
                            tint = if (input.isNotBlank() && !sending) c.primary else c.textTertiary,
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + LifePlannerDesign.Spacing.sm,
                bottom = padding.calculateBottomPadding() + LifePlannerDesign.Spacing.sm,
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            items(messages, key = { it.key }) { m ->
                MessageBubble(m, added = added, onAdd = viewModel::add)
            }
            if (sending) {
                item(key = "typing") {
                    Text("…", style = MaterialTheme.typography.titleLarge, color = c.textTertiary)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    m: HabitChatViewModel.Message,
    added: Set<String>,
    onAdd: (GeneratedHabit) -> Unit,
) {
    val c = MaterialTheme.modernColors
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (m.fromUser) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
    ) {
        Surface(
            color = if (m.fromUser) c.primary else c.cardBackground,
            shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Text(
                m.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (m.fromUser) androidx.compose.ui.graphics.Color.White else c.textPrimary,
                modifier = Modifier.padding(LifePlannerDesign.Padding.cardContent),
            )
        }
        m.suggestions.forEach { s ->
            SuggestionCard(s, isAdded = s.title in added, onAdd = { onAdd(s) })
        }
    }
}

@Composable
private fun SuggestionCard(habit: GeneratedHabit, isAdded: Boolean, onAdd: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        Modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(habit.emoji, style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(habit.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary, maxLines = 2)
                Text(
                    "${habit.category.displayName} · ${habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · ${habit.suggestedTime}",
                    style = MaterialTheme.typography.labelSmall, color = c.textSecondary,
                )
            }
            if (isAdded) {
                Text("Added ✓", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = c.success)
            } else {
                AppButton(text = "Add", onClick = onAdd, variant = AppButtonVariant.SECONDARY)
            }
        }
    }
}
