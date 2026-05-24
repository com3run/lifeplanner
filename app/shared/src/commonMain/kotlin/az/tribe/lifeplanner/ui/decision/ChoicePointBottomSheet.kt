package az.tribe.lifeplanner.ui.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ChoicePoint
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Pillar 3, lightweight "re-choose" sheet for a [ChoicePoint]: keep / reschedule /
 * shrink / drop, with one optional line of reasoning. The chosen action + reasoning are
 * recorded as a [az.tribe.lifeplanner.domain.model.Decision].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoicePointBottomSheet(
    choicePoint: ChoicePoint,
    onResolve: (ChoicePointAction, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var reasoning by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.modernColors.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                choicePoint.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.modernColors.textPrimary
            )
            Text(
                choicePoint.prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.modernColors.textSecondary
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

            fun pick(action: ChoicePointAction) { onResolve(action, reasoning.trim()); onDismiss() }

            // Keep is the affirmative re-commit; the rest are alternatives.
            Button(
                onClick = { pick(ChoicePointAction.KEEP) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Keep going") }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pick(ChoicePointAction.RESCHEDULE) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Reschedule") }
                OutlinedButton(onClick = { pick(ChoicePointAction.SHRINK) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Shrink") }
                OutlinedButton(onClick = { pick(ChoicePointAction.DROP) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Drop") }
            }
        }
    }
}
