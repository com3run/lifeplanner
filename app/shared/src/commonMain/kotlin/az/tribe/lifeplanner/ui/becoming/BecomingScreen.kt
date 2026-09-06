package az.tribe.lifeplanner.ui.becoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.IdentityStatement
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.model.ValueAlignment
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Trash
import org.koin.compose.viewmodel.koinViewModel
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_delete

/**
 * Pillar 5, the "Becoming" view. Identity statements + value-alignment, shown alongside (not
 * replacing) XP/levels: who the user is becoming, evidenced by their actual choices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BecomingScreen(
    onBackClick: () -> Unit,
    viewModel: BecomingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Becoming", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back), tint = MaterialTheme.modernColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val valueTitleById = state.values.associate { it.id to it.title }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 84.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Beyond points and levels, who you're choosing to become, evidenced by your own choices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.modernColors.textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item { SectionHeader("I'm becoming someone who…") }
            if (state.statements.isEmpty()) {
                item {
                    Text(
                        "Add an identity you're growing into, tie it to a value to track it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.modernColors.textSecondary
                    )
                }
            } else {
                items(state.statements, key = { it.id }) { s ->
                    StatementCard(s, valueTitleById[s.valueId], onDelete = { viewModel.deleteStatement(s.id) })
                }
            }
            item {
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(PhosphorIcons.Regular.Plus, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Add a statement")
                }
            }

            item { SectionHeader("How your choices align") }
            val nonEmpty = state.alignments.filter { it.completedGoalCount > 0 || it.decisionCount > 0 }
            if (nonEmpty.isEmpty()) {
                item {
                    Text(
                        "Complete goals and log decisions tied to your values, and your alignment shows up here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.modernColors.textSecondary
                    )
                }
            } else {
                items(nonEmpty, key = { it.valueId }) { AlignmentCard(it) }
            }
        }
    }

    if (showAdd) {
        AddStatementDialog(
            values = state.values,
            onAdd = { text, valueId -> viewModel.addStatement(text, valueId) },
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.modernColors.textSecondary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun StatementCard(s: IdentityStatement, valueTitle: String?, onDelete: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.statement, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.textPrimary)
                if (valueTitle != null) {
                    Text(valueTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.primary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(PhosphorIcons.Regular.Trash, contentDescription = stringResource(Res.string.cd_delete), tint = MaterialTheme.modernColors.textSecondary)
            }
        }
    }
}

@Composable
private fun AlignmentCard(a: ValueAlignment) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(a.valueTitle, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.textPrimary)
                Text(
                    "${a.completedGoalCount} goal${if (a.completedGoalCount == 1) "" else "s"} · ${a.decisionCount} decision${if (a.decisionCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.modernColors.textSecondary
                )
            }
            LinearProgressIndicator(
                progress = { a.goalShare.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.modernColors.primary,
                trackColor = MaterialTheme.modernColors.surfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddStatementDialog(
    values: List<LifeValue>,
    onAdd: (String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var selectedValueId by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onAdd(text, selectedValueId); onDismiss() }, enabled = text.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("I'm becoming someone who…") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("…ships consistently") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 3
                )
                if (values.isNotEmpty()) {
                    Text("Tie to a value (optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.modernColors.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        values.forEach { v ->
                            FilterChip(
                                selected = selectedValueId == v.id,
                                onClick = { selectedValueId = if (selectedValueId == v.id) null else v.id },
                                label = { Text(v.title) }
                            )
                        }
                    }
                }
            }
        }
    )
}
