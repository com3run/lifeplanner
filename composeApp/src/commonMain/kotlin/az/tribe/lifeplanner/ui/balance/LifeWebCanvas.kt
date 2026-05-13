package az.tribe.lifeplanner.ui.balance

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import az.tribe.lifeplanner.domain.model.LifeAreaScore

@Composable
expect fun LifeWebCanvas(
    areaScores: List<LifeAreaScore>,
    modifier: Modifier = Modifier
)
