package az.tribe.lifeplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.brutalistColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboard(viewModel: GoalViewModel, onBackClick: () -> Unit) {
    val analytics by viewModel.analytics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }


    Scaffold(
        containerColor = MaterialTheme.brutalistColors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.brutalistColors.background,
                ),
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("📊 Goal Analytics", style = MaterialTheme.typography.headlineSmall)

            if (analytics == null) {
                CircularProgressIndicator()
            } else {
                Text("Total Goals: ${analytics!!.totalGoals}")
                Text("🟡 Not Started: ${analytics!!.notStarted}")
                Text("🟠 In Progress: ${analytics!!.inProgress}")
                Text("✅ Completed: ${analytics!!.completed}")
                Text("📅 Avg Days to Complete: ${analytics!!.averageCompletionDays}")
            }
        }
    }
}
