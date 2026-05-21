package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.DependencyType
import az.tribe.lifeplanner.domain.model.DependencyGraph
import az.tribe.lifeplanner.domain.model.GoalNode
import az.tribe.lifeplanner.ui.components.backgroundColor
import az.tribe.lifeplanner.ui.components.color
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
internal fun GraphCanvas(
    graph: DependencyGraph,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    selectedNodeId: String?,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onNodeSelected: (String?) -> Unit,
    onNodeDoubleClick: (String) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Calculate node positions using force-directed layout
    val nodePositions = remember(graph.nodes) {
        calculateNodePositions(graph)
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceVariantColor.copy(alpha = 0.3f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.5f, 2.5f)
                    onScaleChange(newScale)
                    onOffsetChange(pan.x, pan.y)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Check if any node was tapped
                        val tappedNode = findNodeAtPosition(
                            offset = offset,
                            nodePositions = nodePositions,
                            scale = scale,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            canvasSize = size.toSize()
                        )
                        onNodeSelected(tappedNode)
                    },
                    onDoubleTap = { offset ->
                        val tappedNode = findNodeAtPosition(
                            offset = offset,
                            nodePositions = nodePositions,
                            scale = scale,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            canvasSize = size.toSize()
                        )
                        tappedNode?.let { onNodeDoubleClick(it) }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2 + offsetX
            val centerY = canvasHeight / 2 + offsetY

            // Draw edges first (so they appear behind nodes)
            graph.edges.forEach { edge ->
                val sourcePos = nodePositions[edge.sourceGoalId] ?: return@forEach
                val targetPos = nodePositions[edge.targetGoalId] ?: return@forEach

                val startX = centerX + sourcePos.first * scale
                val startY = centerY + sourcePos.second * scale
                val endX = centerX + targetPos.first * scale
                val endY = centerY + targetPos.second * scale

                // Draw edge line
                drawEdge(
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    dependencyType = edge.dependencyType,
                    scale = scale,
                    isHighlighted = selectedNodeId == edge.sourceGoalId ||
                            selectedNodeId == edge.targetGoalId
                )
            }

            // Draw nodes
            graph.nodes.forEach { node ->
                val pos = nodePositions[node.goal.id] ?: return@forEach
                val x = centerX + pos.first * scale
                val y = centerY + pos.second * scale

                val isSelected = selectedNodeId == node.goal.id
                val isConnected = selectedNodeId?.let { selected ->
                    graph.edges.any {
                        (it.sourceGoalId == selected && it.targetGoalId == node.goal.id) ||
                                (it.targetGoalId == selected && it.sourceGoalId == node.goal.id)
                    }
                } ?: false

                drawNode(
                    center = Offset(x, y),
                    node = node,
                    scale = scale,
                    isSelected = isSelected,
                    isConnected = isConnected,
                    isDimmed = selectedNodeId != null && !isSelected && !isConnected,
                    textMeasurer = textMeasurer,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

internal fun DrawScope.drawNode(
    center: Offset,
    node: GoalNode,
    scale: Float,
    isSelected: Boolean,
    isConnected: Boolean,
    isDimmed: Boolean,
    textMeasurer: TextMeasurer,
    surfaceColor: Color,
    onSurfaceColor: Color,
    primaryColor: Color
) {
    val nodeRadius = 40f * scale
    val categoryColor = node.goal.category.backgroundColor()

    val alpha = when {
        isDimmed -> 0.3f
        isConnected -> 0.9f
        else -> 1f
    }

    // Node shadow
    if (!isDimmed) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.1f * alpha),
            radius = nodeRadius + 4f,
            center = center + Offset(2f, 2f)
        )
    }

    // Node circle - outer ring for category
    drawCircle(
        color = categoryColor.copy(alpha = alpha),
        radius = nodeRadius,
        center = center
    )

    // Inner circle
    drawCircle(
        color = surfaceColor.copy(alpha = alpha),
        radius = nodeRadius - 4f,
        center = center
    )

    // Progress arc
    val progress = (node.goal.progress?.toFloat() ?: 0f) / 100f
    if (progress > 0) {
        drawArc(
            color = categoryColor.copy(alpha = alpha * 0.8f),
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - nodeRadius + 4f, center.y - nodeRadius + 4f),
            size = Size((nodeRadius - 4f) * 2, (nodeRadius - 4f) * 2),
            style = Stroke(width = 6f * scale)
        )
    }

    // Selection highlight
    if (isSelected) {
        drawCircle(
            color = primaryColor,
            radius = nodeRadius + 6f,
            center = center,
            style = Stroke(width = 3f)
        )
    }

    // Draw goal title (truncated)
    val title = node.goal.title.take(12) + if (node.goal.title.length > 12) "..." else ""
    val textResult = textMeasurer.measure(
        text = title,
        style = TextStyle(
            fontSize = (10f * scale).sp,
            color = onSurfaceColor.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )
    )

    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(
            center.x - textResult.size.width / 2,
            center.y - textResult.size.height / 2
        )
    )
}

internal fun DrawScope.drawEdge(
    start: Offset,
    end: Offset,
    dependencyType: DependencyType,
    scale: Float,
    isHighlighted: Boolean
) {
    val color = dependencyType.color()
    val alpha = if (isHighlighted) 1f else 0.5f
    val strokeWidth = if (isHighlighted) 3f * scale else 2f * scale

    // Calculate direction
    val dx = end.x - start.x
    val dy = end.y - start.y
    val distance = sqrt(dx * dx + dy * dy)
    val nodeRadius = 40f * scale

    // Shorten line to stop at node boundaries
    val ratio = nodeRadius / distance
    val actualStart = Offset(start.x + dx * ratio, start.y + dy * ratio)
    val actualEnd = Offset(end.x - dx * ratio, end.y - dy * ratio)

    // Draw line
    drawLine(
        color = color.copy(alpha = alpha),
        start = actualStart,
        end = actualEnd,
        strokeWidth = strokeWidth
    )

    // Draw arrow for directional dependencies
    if (dependencyType == DependencyType.BLOCKS ||
        dependencyType == DependencyType.BLOCKED_BY ||
        dependencyType == DependencyType.PARENT_OF ||
        dependencyType == DependencyType.CHILD_OF
    ) {
        drawArrowHead(
            tip = actualEnd,
            from = actualStart,
            color = color.copy(alpha = alpha),
            size = 12f * scale
        )
    }
}

internal fun DrawScope.drawArrowHead(
    tip: Offset,
    from: Offset,
    color: Color,
    size: Float
) {
    val angle = atan2(tip.y - from.y, tip.x - from.x)
    val arrowAngle = PI / 6 // 30 degrees

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(
            (tip.x - size * cos(angle - arrowAngle)).toFloat(),
            (tip.y - size * sin(angle - arrowAngle)).toFloat()
        )
        lineTo(
            (tip.x - size * cos(angle + arrowAngle)).toFloat(),
            (tip.y - size * sin(angle + arrowAngle)).toFloat()
        )
        close()
    }

    drawPath(path = path, color = color, style = Fill)
}

internal fun calculateNodePositions(graph: DependencyGraph): Map<String, Pair<Float, Float>> {
    if (graph.nodes.isEmpty()) return emptyMap()

    val positions = mutableMapOf<String, Pair<Float, Float>>()
    val nodeCount = graph.nodes.size

    // Simple circular layout with level adjustments
    val baseRadius = 150f + (nodeCount * 20f)

    graph.nodes.forEachIndexed { index, node ->
        val level = node.level
        val radius = baseRadius + (level * 80f)
        val angle = (2 * PI * index / nodeCount).toFloat()

        positions[node.goal.id] = Pair(
            (radius * cos(angle)).toFloat(),
            (radius * sin(angle)).toFloat()
        )
    }

    return positions
}

internal fun findNodeAtPosition(
    offset: Offset,
    nodePositions: Map<String, Pair<Float, Float>>,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    canvasSize: Size
): String? {
    val centerX = canvasSize.width / 2 + offsetX
    val centerY = canvasSize.height / 2 + offsetY
    val nodeRadius = 40f * scale

    nodePositions.forEach { (nodeId, pos) ->
        val nodeX = centerX + pos.first * scale
        val nodeY = centerY + pos.second * scale

        val distance = sqrt(
            (offset.x - nodeX) * (offset.x - nodeX) +
                    (offset.y - nodeY) * (offset.y - nodeY)
        )

        if (distance <= nodeRadius) {
            return nodeId
        }
    }

    return null
}

internal fun androidx.compose.ui.unit.IntSize.toSize(): Size {
    return Size(width.toFloat(), height.toFloat())
}
