package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.DependencyGraph
import az.tribe.lifeplanner.ui.dependency.GoalDependencyViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Crosshair
import com.adamglin.phosphoricons.regular.Funnel
import com.adamglin.phosphoricons.regular.MagnifyingGlassMinus
import com.adamglin.phosphoricons.regular.MagnifyingGlassPlus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependencyGraphScreen(
    viewModel: GoalDependencyViewModel = koinViewModel(),
    focusGoalId: String? = null,
    onNavigateBack: () -> Unit,
    onGoalClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedNodeId by remember { mutableStateOf<String?>(focusGoalId) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<GoalCategory?>(null) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goal Dependencies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            PhosphorIcons.Regular.Funnel,
                            contentDescription = "Filter",
                            tint = if (selectedCategory != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { scale = (scale + 0.2f).coerceAtMost(2.5f) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(PhosphorIcons.Regular.MagnifyingGlassPlus, contentDescription = "Zoom In")
                }

                SmallFloatingActionButton(
                    onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(PhosphorIcons.Regular.MagnifyingGlassMinus, contentDescription = "Zoom Out")
                }

                FloatingActionButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        selectedNodeId = null
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        PhosphorIcons.Regular.Crosshair,
                        contentDescription = "Reset View"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category filters
            if (showFilters) {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            if (uiState.dependencyGraph.isEmpty || uiState.dependencyGraph.edges.isEmpty()) {
                // Empty state
                EmptyGraphState()
            } else {
                // Graph visualization
                Box(modifier = Modifier.weight(1f)) {
                    val filteredGraph = if (selectedCategory != null) {
                        DependencyGraph(
                            nodes = uiState.dependencyGraph.nodes.filter { it.goal.category == selectedCategory },
                            edges = uiState.dependencyGraph.edges.filter { edge ->
                                val sourceNode = uiState.dependencyGraph.getNodeByGoalId(edge.sourceGoalId)
                                val targetNode = uiState.dependencyGraph.getNodeByGoalId(edge.targetGoalId)
                                sourceNode?.goal?.category == selectedCategory &&
                                        targetNode?.goal?.category == selectedCategory
                            }
                        )
                    } else {
                        uiState.dependencyGraph
                    }

                    GraphCanvas(
                        graph = filteredGraph,
                        scale = animatedScale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        selectedNodeId = selectedNodeId,
                        onScaleChange = { scale = it },
                        onOffsetChange = { dx, dy ->
                            offsetX += dx
                            offsetY += dy
                        },
                        onNodeSelected = { nodeId ->
                            selectedNodeId = nodeId
                        },
                        onNodeDoubleClick = { nodeId ->
                            onGoalClick(nodeId)
                        }
                    )
                }

                // Selected node info panel
                selectedNodeId?.let { nodeId ->
                    uiState.dependencyGraph.getNodeByGoalId(nodeId)?.let { node ->
                        NodeInfoCard(
                            node = node,
                            allNodes = uiState.dependencyGraph.nodes,
                            onGoalClick = onGoalClick,
                            onDismiss = { selectedNodeId = null }
                        )
                    }
                }
            }

            // Legend
            GraphLegend()
        }
    }
}
