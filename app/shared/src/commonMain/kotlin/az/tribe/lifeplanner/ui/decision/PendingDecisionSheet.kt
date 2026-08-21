package az.tribe.lifeplanner.ui.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Pillar 3, the gentle "want to log this?" confirmation for an AI-detected [Decision] (a
 * [az.tribe.lifeplanner.domain.model.DecisionSource.JOURNAL] decision that is still
 * [az.tribe.lifeplanner.domain.model.DecisionStatus.PENDING]). The user validates the AI's guess,
 * optionally corrects which option they actually chose and the reasoning, then logs it, or says it
 * wasn't really a decision. Only a confirmed decision enters the log and (from Phase 4) the wiring.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PendingDecisionSheet(
    decision: Decision,
    onConfirm: (chosenOption: String, reasoning: String) -> Unit,
    onDismissDecision: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var chosen by remember { mutableStateOf(decision.chosenOption) }
    var reasoning by remember { mutableStateOf(decision.reasoning) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = c.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Two text fields and no keyboard handling: typing into either one hid the
                // confirm button behind the IME with nothing to scroll.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Looks like a decision", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = c.textSecondary)
            Text(decision.question, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)

            if (decision.optionsConsidered.isNotEmpty()) {
                Text("What did you choose?", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    decision.optionsConsidered.forEach { option ->
                        FilterChip(
                            selected = chosen == option,
                            onClick = { chosen = option },
                            label = { Text(option) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = chosen,
                onValueChange = { chosen = it },
                label = { Text("Chosen option") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = reasoning,
                onValueChange = { reasoning = it },
                label = { Text("Why? (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { onConfirm(chosen.trim(), reasoning.trim()); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Log this decision") }

            TextButton(
                onClick = { onDismissDecision(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Not a decision", color = c.textSecondary) }
        }
    }
}
