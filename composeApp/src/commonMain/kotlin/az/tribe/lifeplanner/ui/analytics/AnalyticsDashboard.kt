package az.tribe.lifeplanner.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.goal.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboard(viewModel: GoalViewModel, onBackClick: () -> Unit) {
    val analytics by viewModel.analytics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        val currentAnalytics = analytics
        if (currentAnalytics == null) {
            LoadingState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Hero Stats
                item {
                    HeroStatsSection(currentAnalytics)
                }

                // Progress Overview
                item {
                    ProgressOverviewSection(currentAnalytics)
                }

                // Category Breakdown
                item {
                    CategoryBreakdownSection(currentAnalytics)
                }

                // Timeline Distribution
                item {
                    TimelineDistributionSection(currentAnalytics)
                }

                // Performance Insights
                item {
                    PerformanceInsightsSection(currentAnalytics)
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
