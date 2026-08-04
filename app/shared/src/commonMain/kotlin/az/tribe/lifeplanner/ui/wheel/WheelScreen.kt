package az.tribe.lifeplanner.ui.wheel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.model.WheelScore
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen(
    viewModel: WheelViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report = state.report

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Wheel of life") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            report == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    state.error ?: "Could not read your wheel.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            else -> WheelContent(
                report = report,
                selected = state.selected,
                onSelect = viewModel::select,
                onSetScore = viewModel::setScore,
                onClearScore = viewModel::clearScore,
                topPadding = padding.calculateTopPadding(),
                comparison = state.comparison,
                period = state.period,
                snapshotCount = state.snapshotCount,
                comparisonLoading = state.comparisonLoading,
                onPeriodChange = viewModel::setPeriod,
            )
        }
    }
}

@Composable
private fun WheelContent(
    report: WheelReport,
    selected: WheelArea?,
    onSelect: (WheelArea?) -> Unit,
    onSetScore: (WheelArea, Double) -> Unit,
    onClearScore: (WheelArea) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    comparison: az.tribe.lifeplanner.domain.model.WheelComparison?,
    period: az.tribe.lifeplanner.domain.model.ComparisonPeriod,
    snapshotCount: Int,
    comparisonLoading: Boolean,
    onPeriodChange: (az.tribe.lifeplanner.domain.model.ComparisonPeriod) -> Unit,
) {
    // Where the finger currently has a slice, before it is kept. The headline and the face read
    // from this so they move with the drag; nothing here reaches the database until release.
    var live by remember { mutableStateOf<Pair<WheelArea, Double>?>(null) }
    val shown = remember(report, live) {
        live?.let { (area, value) ->
            report.copy(
                scores = report.scores.map { if (it.area == area) it.copy(score = value) else it }
            )
        } ?: report
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = LifePlannerDesign.Padding.screenHorizontal,
            end = LifePlannerDesign.Padding.screenHorizontal,
            top = topPadding + LifePlannerDesign.Spacing.xs,
            bottom = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
    ) {
        item {
            WheelHeadline(shown)
        }

        item {
            WheelCanvas(
                scores = shown.scores,
                onAreaTap = onSelect,
                selected = selected,
                onScoreDrag = { area, value -> live = area to value },
                onScoreCommit = { area, value ->
                    onSetScore(area, value)
                    live = null
                },
                modifier = Modifier.padding(vertical = LifePlannerDesign.Spacing.xs),
            )
        }

        report.joy?.let { joy ->
            item { JoyCard(joy) }
        }

        item {
            WheelHistoryCard(
                comparison = comparison,
                period = period,
                snapshotCount = snapshotCount,
                isLoading = comparisonLoading,
                onPeriodChange = onPeriodChange,
            )
        }

        val unconfirmed = report.unconfirmed
        if (unconfirmed.isNotEmpty()) {
            item {
                Text(
                    text = "We could not read ${unconfirmed.size} of these from your data. " +
                        "Tap one to set it yourself.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(shown.segments, key = { it.area.name }) { score ->
            AreaRow(
                score = score,
                expanded = score.area == selected,
                onTap = { onSelect(score.area) },
                onSetScore = { onSetScore(score.area, it) },
                onClearScore = { onClearScore(score.area) },
            )
        }
    }
}

@Composable
private fun WheelHeadline(report: WheelReport) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatScore(report.overall),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = " / 10 overall",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        // The shape matters more than the mean, so name the gap rather than only the average.
        report.lowest?.let { low ->
            Text(
                text = if (report.spread >= 3.0) {
                    "${low.area.displayName} is furthest behind, ${formatScore(report.spread)} below your best."
                } else {
                    "Your wheel is fairly even. ${low.area.displayName} is the lowest."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun JoyCard(joy: WheelScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(areaColor(WheelArea.JOY)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = formatScore(joy.score),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.surface,
                )
            }
            Column(modifier = Modifier.padding(start = LifePlannerDesign.Spacing.sm)) {
                Text("Joy", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = joy.basis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AreaRow(
    score: WheelScore,
    expanded: Boolean,
    onTap: () -> Unit,
    onSetScore: (Double) -> Unit,
    onClearScore: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTap,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(10.dp).clip(CircleShape).background(areaColor(score.area))
                )
                Text(
                    text = score.area.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = LifePlannerDesign.Spacing.xs).weight(1f),
                )
                Text(
                    text = if (score.needsConfirmation) "not set" else formatScore(score.score),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (score.needsConfirmation) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            Text(
                text = score.basis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xxs),
            )

            if (expanded) {
                // The rubric only appears when the user is about to score, which is the one moment
                // it earns its space.
                Text(
                    text = "A 10 means: ${score.area.rubric}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
                )
                // onValueChange fires every frame of a drag. Writing through to the repository
                // from here meant one database write and one sync request per frame, and left a
                // trail of half-chosen values. The drag lives in local state; only the released
                // value is kept.
                var dragged by remember(score.area) { mutableStateOf<Float?>(null) }
                Slider(
                    value = dragged ?: score.score.toFloat(),
                    onValueChange = { dragged = it },
                    onValueChangeFinished = {
                        dragged?.let { onSetScore(it.toDouble()) }
                        dragged = null
                    },
                    valueRange = 0f..10f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (score.source == ScoreSource.USER) {
                    TextButton(onClick = onClearScore) { Text("Use the app's estimate instead") }
                }
            }
        }
    }
}
