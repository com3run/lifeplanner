package az.tribe.lifeplanner.ui.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.OutcomeQuality
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pillar 5 (P5.4), metacognitive review. Surfaces logged Decisions and asks the user to grade
 * their *reasoning* (process) separately from the *result*, the "good decision vs. good luck"
 * distinction. Grading sets [Decision.outcomeQuality].
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MetacognitiveReviewScreen(
    onBackClick: () -> Unit,
    viewModel: MetacognitiveReviewViewModel = koinViewModel(),
) {
    val decisions by viewModel.decisions.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Review Decisions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = MaterialTheme.modernColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary
                )
            )
        }
    ) { padding ->
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
                    "Grade your reasoning, not the outcome. A sound decision can turn out badly " +
                        "(and a flawed one can get lucky), what matters long-term is the process.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.modernColors.textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            if (decisions.isEmpty()) {
                item {
                    Text(
                        "No decisions logged yet. Resolve a Choice Point and it'll appear here to review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.modernColors.textSecondary
                    )
                }
            } else {
                items(decisions, key = { it.id }) { d ->
                    DecisionGradeCard(d, onGrade = { q -> viewModel.grade(d, q) })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DecisionGradeCard(d: Decision, onGrade: (OutcomeQuality) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(d.question, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.textPrimary, maxLines = 2)
            Text("Chose: ${d.chosenOption}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.textSecondary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutcomeQuality.entries.forEach { q ->
                    FilterChip(
                        selected = d.outcomeQuality == q,
                        onClick = { onGrade(q) },
                        label = { Text(gradeLabel(q)) }
                    )
                }
            }
        }
    }
}

private fun gradeLabel(q: OutcomeQuality): String = when (q) {
    OutcomeQuality.GOOD_PROCESS_GOOD_RESULT -> "Sound · Worked"
    OutcomeQuality.GOOD_PROCESS_BAD_RESULT -> "Sound · Unlucky"
    OutcomeQuality.BAD_PROCESS_GOOD_RESULT -> "Flawed · Lucky"
    OutcomeQuality.BAD_PROCESS_BAD_RESULT -> "Flawed · Bad"
}
