package az.tribe.lifeplanner.ui.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionDetailScreen(
    decisionId: String,
    onBackClick: () -> Unit,
    viewModel: DecisionViewModel = koinViewModel(),
) {
    val decisions by viewModel.decisions.collectAsState()
    val decision = decisions.find { it.id == decisionId }

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Decision", fontWeight = FontWeight.Bold) },
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
        if (decision == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                decision.question,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.modernColors.textPrimary
            )
            FieldBlock("Chose", decision.chosenOption)
            if (decision.optionsConsidered.isNotEmpty()) {
                FieldBlock("Options considered", decision.optionsConsidered.joinToString(" · "))
            }
            if (decision.reasoning.isNotBlank()) FieldBlock("Reasoning", decision.reasoning)
            if (decision.expectedOutcome.isNotBlank()) FieldBlock("Expected outcome", decision.expectedOutcome)
            FieldBlock("Confidence", "${decision.confidence}%")
            FieldBlock("Decided", decision.decidedAt.date.toString())

            val quality = decision.outcomeQuality
            if (quality != null || decision.actualOutcome != null) {
                FieldBlock(
                    "Outcome",
                    buildString {
                        decision.actualOutcome?.let { append(it) }
                        if (quality != null) {
                            if (isNotEmpty()) append("\n")
                            append(quality.name.lowercase().replace('_', ' '))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FieldBlock(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.modernColors.cardBackground,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.modernColors.textSecondary
            )
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.modernColors.textPrimary)
        }
    }
}
