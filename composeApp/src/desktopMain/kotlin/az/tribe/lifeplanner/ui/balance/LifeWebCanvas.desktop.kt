package az.tribe.lifeplanner.ui.balance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import az.tribe.lifeplanner.domain.model.LifeAreaScore

@Composable
actual fun LifeWebCanvas(
    areaScores: List<LifeAreaScore>,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Life Web (mobile only)")
    }
}
