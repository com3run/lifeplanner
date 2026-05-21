package az.tribe.lifeplanner.ui.screentime

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Spinner
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeInsightScreen(
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit,
    viewModel: ScreenTimeInsightViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Patterns", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, "Back")
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = state.phase,
            modifier = Modifier.fillMaxSize().padding(padding),
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "insight_phase"
        ) { phase ->
            when (phase) {
                InsightPhase.LOADING -> LoadingPhase()
                InsightPhase.EMPTY -> EmptyPhase(onBack = onNavigateBack)
                InsightPhase.OVERVIEW -> OverviewPhase(state, onContinue = { viewModel.advanceToPatterns() })
                InsightPhase.PATTERNS -> PatternsPhase(state, onContinue = { viewModel.generateRecommendations() })
                InsightPhase.GENERATING_RECS -> GeneratingPhase(state.streamingText)
                InsightPhase.RECOMMENDATIONS -> RecommendationsPhase(
                    state = state,
                    onNavigateTo = onNavigateTo,
                    onBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun LoadingPhase() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyPhase(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyPatternCard()
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) {
            Text("Go Back")
        }
    }
}

@Composable
private fun OverviewPhase(state: ScreenTimeInsightState, onContinue: () -> Unit) {
    val pattern = state.pattern ?: return
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Here's how you've been using the app",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OverviewCard(pattern)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("See Detailed Patterns")
            Spacer(Modifier.size(6.dp))
            Icon(PhosphorIcons.Regular.ArrowRight, null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PatternsPhase(state: ScreenTimeInsightState, onContinue: () -> Unit) {
    val pattern = state.pattern ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Your behavior patterns",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item { HourActivityBar(pattern) }
        item { DayActivityCard(pattern) }
        item { FeatureEngagementCard(pattern) }
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Get Personalized Tips")
                Spacer(Modifier.size(6.dp))
                Icon(PhosphorIcons.Regular.ArrowRight, null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GeneratingPhase(streamingText: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            "Analyzing your patterns…",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        if (streamingText.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                streamingText.take(120),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecommendationsPhase(
    state: ScreenTimeInsightState,
    onNavigateTo: (String) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Your Personalized Tips", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Based on how you actually use the app",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        }
        items(state.recommendations) { rec ->
            RecommendationCard(
                rec = rec,
                index = state.recommendations.indexOf(rec),
                onClick = onNavigateTo
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Done")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
