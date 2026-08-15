package az.tribe.lifeplanner.ui.trajectory

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.TrajectoryPoint
import az.tribe.lifeplanner.domain.model.TrajectorySeries
import az.tribe.lifeplanner.domain.service.TrajectoryProjector
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * The explorable **life-balance trajectory**. Four lines, how it's gone, where the current pace
 * leads, where it could go with more effort, and the ideal, plus an "effort" slider the user drags
 * to watch the "could be" line and the projected score move. Data is real (current balance +
 * reconstructed past); the projections are computed by [TrajectoryProjector].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrajectoryScreen(
    onBackClick: () -> Unit,
    viewModel: TrajectoryViewModel = koinViewModel(),
) {
    val s by viewModel.state.collectAsState()
    val c = MaterialTheme.modernColors
    var effort by remember { mutableStateOf(0.5f) }

    val minWeek = -TrajectoryViewModel.PAST_WEEKS
    val maxWeek = TrajectoryViewModel.HORIZON_WEEKS
    val series = remember(s.past, effort) {
        TrajectoryProjector.project(s.past, maxWeek, effort, TrajectoryViewModel.IDEAL_SCORE)
    }

    val pastColor = c.primary
    val paceColor = c.textTertiary
    val couldColor = c.success
    val idealColor = Color(0xFFF5A623)

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Your trajectory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
    ) { padding ->
        if (s.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Charting your trajectory…", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = padding.calculateTopPadding() + LifePlannerDesign.Spacing.sm,
                    bottom = padding.calculateBottomPadding() + 84.dp,
                    start = LifePlannerDesign.Padding.screenHorizontal,
                    end = LifePlannerDesign.Padding.screenHorizontal,
                ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            Text(
                "See how your life balance has gone, where today's pace leads, and how far you could take it. Drag the effort slider to explore.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )

            ProjectedReadout(
                currentScore = s.currentScore,
                paceEnd = series.currentPace.lastOrNull()?.score?.roundToInt() ?: s.currentScore,
                couldEnd = series.projectedEndScore,
                weeks = maxWeek,
                accent = couldColor,
            )

            TrajectoryChart(
                series = series,
                minWeek = minWeek,
                maxWeek = maxWeek,
                ideal = TrajectoryViewModel.IDEAL_SCORE,
                pastColor = pastColor,
                paceColor = paceColor,
                couldColor = couldColor,
                idealColor = idealColor,
                gridColor = c.textTertiary.copy(alpha = 0.18f),
                nowColor = c.textSecondary,
                labelColor = c.textSecondary,
            )

            Legend(pastColor, paceColor, couldColor, idealColor)

            EffortSlider(effort = effort, onEffort = { effort = it }, accent = couldColor)
        }
    }
}

@Composable
private fun ProjectedReadout(currentScore: Int, paceEnd: Int, couldEnd: Int, weeks: Int, accent: Color) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$couldEnd", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = accent)
                Text("could-be balance in $weeks weeks", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(
                "Today you're at $currentScore. At this effort you'd reach $couldEnd; on current pace, about $paceEnd.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textTertiary,
            )
        }
    }
}

@Composable
private fun TrajectoryChart(
    series: TrajectorySeries,
    minWeek: Int,
    maxWeek: Int,
    ideal: Float,
    pastColor: Color,
    paceColor: Color,
    couldColor: Color,
    idealColor: Color,
    gridColor: Color,
    nowColor: Color,
    labelColor: Color,
) {
    val density = LocalDensity.current
    val padL = with(density) { 8.dp.toPx() }
    val padR = with(density) { 8.dp.toPx() }
    val padT = with(density) { 12.dp.toPx() }
    val padB = with(density) { 12.dp.toPx() }
    val weekSpan = (maxWeek - minWeek).toFloat().coerceAtLeast(1f)

    var selectedWeek by remember { mutableStateOf<Int?>(null) }

    fun weekAtX(x: Float, width: Int): Int {
        val plotW = width - padL - padR
        val frac = ((x - padL) / plotW).coerceIn(0f, 1f)
        return (minWeek + frac * weekSpan).roundToInt().coerceIn(minWeek, maxWeek)
    }

    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(Modifier.fillMaxWidth().padding(LifePlannerDesign.Spacing.sm)) {
            // Scrubber readout
            val sel = selectedWeek
            val label = if (sel == null) "Tap or drag the chart to inspect a week" else {
                val pace = valueAt(series.currentPace, sel)
                val could = valueAt(series.couldBe, sel)
                val past = valueAt(series.past, sel)
                val whenTxt = when {
                    sel < 0 -> "${-sel}w ago"
                    sel == 0 -> "now"
                    else -> "+${sel}w"
                }
                val shown = past ?: could
                "$whenTxt · pace ${pace?.roundToInt() ?: "-"} · could ${could?.roundToInt() ?: "-"}" +
                    (if (past != null) " · was ${shown?.roundToInt()}" else "")
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .pointerInput(minWeek, maxWeek) {
                        detectTapGestures { off -> selectedWeek = weekAtX(off.x, size.width) }
                    }
                    .pointerInput(minWeek, maxWeek) {
                        detectHorizontalDragGestures { change, _ -> selectedWeek = weekAtX(change.position.x, size.width) }
                    },
            ) {
                val plotW = size.width - padL - padR
                val plotH = size.height - padT - padB

                fun px(week: Int): Float = padL + ((week - minWeek) / weekSpan) * plotW
                fun py(score: Float): Float = padT + (1f - (score / 100f)) * plotH

                // horizontal gridlines at 0/25/50/75/100
                listOf(0f, 25f, 50f, 75f, 100f).forEach { g ->
                    val y = py(g)
                    drawLine(gridColor, Offset(padL, y), Offset(size.width - padR, y), strokeWidth = 1f)
                }

                // ideal target line (dashed)
                val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                drawLine(idealColor.copy(alpha = 0.7f), Offset(padL, py(ideal)), Offset(size.width - padR, py(ideal)), strokeWidth = 2f, pathEffect = dash)

                // "now" divider
                drawLine(nowColor.copy(alpha = 0.5f), Offset(px(0), padT), Offset(px(0), size.height - padB), strokeWidth = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)))

                fun offsets(points: List<TrajectoryPoint>) = points.map { Offset(px(it.weekOffset), py(it.score)) }

                polyline(offsets(series.currentPace), paceColor, 4f, PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))
                polyline(offsets(series.couldBe), couldColor, 6f, null)
                polyline(offsets(series.past), pastColor, 6f, null)

                // anchor dot at now
                series.past.lastOrNull()?.let { drawCircle(pastColor, radius = 7f, center = Offset(px(0), py(it.score))) }

                // scrubber
                selectedWeek?.let { w ->
                    val x = px(w)
                    drawLine(nowColor, Offset(x, padT), Offset(x, size.height - padB), strokeWidth = 1.5f)
                    valueAt(series.couldBe, w)?.let { drawCircle(couldColor, 6f, Offset(x, py(it))) }
                    valueAt(series.currentPace, w)?.let { drawCircle(paceColor, 5f, Offset(x, py(it))) }
                    valueAt(series.past, w)?.let { drawCircle(pastColor, 6f, Offset(x, py(it))) }
                }
            }
        }
    }
}

private fun valueAt(points: List<TrajectoryPoint>, week: Int): Float? =
    points.firstOrNull { it.weekOffset == week }?.score

private fun DrawScope.polyline(points: List<Offset>, color: Color, stroke: Float, effect: PathEffect?) {
    for (i in 0 until points.size - 1) {
        drawLine(color, points[i], points[i + 1], strokeWidth = stroke, cap = StrokeCap.Round, pathEffect = effect)
    }
}

@Composable
private fun Legend(pastColor: Color, paceColor: Color, couldColor: Color, idealColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md), modifier = Modifier.fillMaxWidth()) {
        LegendItem("So far", pastColor)
        LegendItem("Current pace", paceColor)
        LegendItem("Could be", couldColor)
        LegendItem("Ideal", idealColor)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
    }
}

@Composable
private fun EffortSlider(effort: Float, onEffort: (Float) -> Unit, accent: Color) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("How much more will you put in?", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
            Slider(value = effort, onValueChange = onEffort, valueRange = 0f..1f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Coast", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                Text("All in", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
            }
        }
    }
}
