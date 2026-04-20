package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.gradientColors

@Composable
fun GoalItem(
    goal: Goal,
    onClick: () -> Unit,
    scrollState: LazyListState
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Calculate animation values for cards based on scroll
    val visibleItems = scrollState.layoutInfo.visibleItemsInfo
    val indexInVisible = visibleItems.indexOfFirst { it.key == goal.id }

    // Create a subtle parallax effect based on scroll position
    val yOffset by animateFloatAsState(
        targetValue = if (indexInVisible in 0..3) (3 - indexInVisible) * -2f else 0f,
        label = "yOffset"
    )

    val scale by animateFloatAsState(
        targetValue = if (showDeleteConfirm) 0.98f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    // Get category gradient colors
    val categoryGradientColors = goal.category.gradientColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .graphicsLayer {
                translationY = yOffset
                scaleX = scale
                scaleY = scale
            }
            .clickable {
                if (showDeleteConfirm) {
                    showDeleteConfirm = false
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = LifePlannerDesign.Elevation.low)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Thin category accent bar on the left
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(brush = Brush.verticalGradient(categoryGradientColors))
            )

            // Goal content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(LifePlannerDesign.Padding.standard),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                val categoryColor = goal.category.backgroundColor()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = goal.category.getIcon(),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    GoalCard(goal = goal)
                }
            }
        }
    }
}