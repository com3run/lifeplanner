package az.tribe.lifeplanner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Plus

data class ExploreShortcut(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun ExploreShortcutsRow(
    shortcuts: List<ExploreShortcut>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        shortcuts.forEach { shortcut ->
            ShortcutChip(shortcut = shortcut, modifier = Modifier.weight(1f))
        }
    }
}

fun LazyListScope.exploreItems(
    onNavigateToFocus: () -> Unit,
    onNavigateToRetrospective: () -> Unit,
    onAddGoalClick: () -> Unit,
    onNavigateToHealth: () -> Unit,
) {
    item(key = "explore_header") {
        Text(
            "Explore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
        )
    }
    item(key = "explore_shortcuts") {
        ExploreShortcutsRow(
            shortcuts = listOf(
                ExploreShortcut(PhosphorIcons.Regular.Play, "Focus", Color(0xFFFF6B35), onNavigateToFocus),
                ExploreShortcut(PhosphorIcons.Regular.ClockCounterClockwise, "Recap", Color(0xFF7C4DFF), onNavigateToRetrospective),
                ExploreShortcut(PhosphorIcons.Regular.Plus, "New Goal", MaterialTheme.colorScheme.primary, onAddGoalClick),
                ExploreShortcut(PhosphorIcons.Regular.Heart, "Health", Color(0xFFEA5455), onNavigateToHealth)
            ),
            modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
        )
    }
}

@Composable
private fun ShortcutChip(
    shortcut: ExploreShortcut,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = shortcut.onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(shortcut.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                shortcut.icon,
                contentDescription = shortcut.label,
                tint = shortcut.color,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            shortcut.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
    }
}
