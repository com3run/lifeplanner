@file:OptIn(ExperimentalFoundationApi::class)

package az.tribe.lifeplanner.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MinorCrash
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalCategory
import az.tribe.lifeplanner.domain.GoalTimeline
import az.tribe.lifeplanner.ui.components.GoalCard
import az.tribe.lifeplanner.ui.components.TimelineTabs
import az.tribe.lifeplanner.ui.components.backgroundColor
import az.tribe.lifeplanner.ui.theme.brutalistColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GoalViewModel,
    onGoalClick: (Goal) -> Unit,
    goToAnalytics: () -> Unit,
    onAddGoalClick: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    var recentlyDeletedGoal by remember { mutableStateOf<Goal?>(null) }

    val goals by viewModel.goals.collectAsState()
    var selectedTimeline by remember { mutableStateOf(GoalTimeline.SHORT_TERM) }

    val scrollState = rememberLazyListState()

    val firstVisibleCategoryColor by remember {
        derivedStateOf {
            val firstVisibleVisibleId = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.key
            val firstVisibleItem = goals.firstOrNull { it.id == firstVisibleVisibleId }
            firstVisibleItem?.category?.backgroundColor() ?: Color(0xFFFF3B3B)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadGoalsByTimeline(selectedTimeline)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome!") },
                actions = {

                    IconButton(
                        onClick = {
                            goToAnalytics()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = firstVisibleCategoryColor
                        )
                    }

                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = onAddGoalClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.brutalistColors.accent,
                    contentColor = MaterialTheme.brutalistColors.buttonText
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                shape = RectangleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        },
        containerColor = MaterialTheme.brutalistColors.background,
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            TimelineTabs(
                selected = selectedTimeline,
                onSelect = {
                    selectedTimeline = it
                    viewModel.loadGoalsByTimeline(it)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            GoalListSection(
                groupedGoals = goals.groupBy { it.category },
                onGoalClick = onGoalClick,
                onGoalDelete = { goal ->
                    recentlyDeletedGoal = goal
                    viewModel.deleteGoal(goal.id)
                },
                scrollState = scrollState
            )
        }
    }

    LaunchedEffect(recentlyDeletedGoal) {
        recentlyDeletedGoal?.let { goal ->
            val result = snackBarHostState.showSnackbar(
                message = "Goal deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.createGoal(goal)
            }
            recentlyDeletedGoal = null
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalListSection(
    groupedGoals: Map<GoalCategory, List<Goal>>,
    onGoalClick: (Goal) -> Unit,
    onGoalDelete: (Goal) -> Unit,
    scrollState: androidx.compose.foundation.lazy.LazyListState
) {

    val allGoalsEmpty = groupedGoals.values.flatten().isEmpty()

    if (allGoalsEmpty) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No goals here yet. Tap + to add one!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.brutalistColors.primary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            groupedGoals.keys.sortedBy { it.order }.forEach { category ->
                val goals = groupedGoals[category].orEmpty()
                if (goals.isNotEmpty()) {
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.brutalistColors.background)
                        ) {
                            Text(
                                text = category.name.lowercase().replaceFirstChar(Char::uppercase),
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.brutalistColors.textPrimary
                            )
                        }
                    }

                    items(goals, key = { it.id }) { goal ->
                        val currentCategory = category

                        var offsetX by remember { mutableStateOf(0f) }
                        val maxOffset = 100f
                        val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "")

                        val visibleItems = scrollState.layoutInfo.visibleItemsInfo
                        val indexInVisible = visibleItems.indexOfFirst { it.key == goal.id }

                        val rawScale =
                            if (indexInVisible in 0..2 || indexInVisible >= visibleItems.size - 3) {
                                0.95f
                            } else {
                                1f
                            }
                        val scale by animateFloatAsState(targetValue = rawScale, label = "scale")

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (offsetX > -maxOffset / 2) {
                                                offsetX = 0f
                                            } else {
                                                offsetX = -maxOffset
                                            }
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            offsetX = (offsetX + dragAmount).coerceIn(-maxOffset, 0f)
                                        }
                                    )
                                }
                        ) {
                            // Neo-Brutalist: Swipe delete background with flat color, square edges, and thick black border
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        currentCategory.backgroundColor(),
                                    ),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(
                                    onClick = { onGoalDelete(goal) },
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                            val bgColor = remember(goal.id) { currentCategory.backgroundColor() }
                            Modifier
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                )
                                .background(bgColor)
                                .border(2.dp, MaterialTheme.colorScheme.outline)
                            GoalCard(
                                goal = goal,
                                onClick = { onGoalClick(goal) },
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        transformOrigin = TransformOrigin(
                                            0f,
                                            0.5f
                                        ) // Anchor scaling from start edge
                                    )
                                    .offset(x = animatedOffset.dp)
                                    .background(currentCategory.backgroundColor())
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.outline,
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}