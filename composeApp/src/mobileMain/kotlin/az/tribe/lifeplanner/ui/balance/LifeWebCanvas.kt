package az.tribe.lifeplanner.ui.balance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.LifeArea
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import network.chaintech.cmpcharts.ui.polarareachart.PolarAreaChart
import network.chaintech.cmpcharts.ui.polarareachart.PolarAreaChartConfig
import network.chaintech.cmpcharts.ui.polarareachart.PolarChartEntry
import network.chaintech.cmpcharts.ui.polarareachart.PolarChartEntryStyle
import network.chaintech.cmpcharts.ui.polarareachart.PolarChartScaleConfig

private val WEB_AREA_ORDER = listOf(
    LifeArea.CAREER,
    LifeArea.WELLBEING,
    LifeArea.MONEY,
    LifeArea.PURPOSE,
    LifeArea.PEOPLE,
    LifeArea.BODY
)

@Composable
actual fun LifeWebCanvas(
    areaScores: List<LifeAreaScore>,
    modifier: Modifier
) {
    val isDark = true
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    PolarAreaChart(
        modifier = modifier,
        config = PolarAreaChartConfig(
            entries = WEB_AREA_ORDER.mapNotNull { area ->
                val score = areaScores.find { it.area == area } ?: return@mapNotNull null
                val color = getAreaColor(area, isDark)
                PolarChartEntry(
                    label = area.displayName,
                    value = score.score.toFloat().coerceAtLeast(2f),
                    style = PolarChartEntryStyle(
                        color = color,
                        brushColors = listOf(color, color.copy(alpha = 0.45f)),
                        alpha = 0.88f
                    )
                )
            },
            maxValue = 100f,
            polarChartScaleConfig = PolarChartScaleConfig(
                scaleSteps = 5,
                scaleColor = onSurfaceVariant.copy(alpha = 0.08f),
                scaleStrokeWidth = 0.8.dp
            ),
            showScaleLabels = false,
            showValueLabels = true,
            showCategoryLabels = true,
            scaleLabelSuffix = "",
            sliceValueTextStyle = TextStyle(
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            sliceLabelTextStyle = TextStyle(
                color = onSurfaceVariant,
                fontSize = 9.sp
            ),
            scaleTextStyle = TextStyle(
                color = onSurfaceVariant.copy(alpha = 0.2f),
                fontSize = 7.sp
            ),
            labelPadding = 6.dp
        )
    )
}
