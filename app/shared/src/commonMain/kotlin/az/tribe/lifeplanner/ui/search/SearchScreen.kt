package az.tribe.lifeplanner.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onGoalClick: (goalId: String) -> Unit,
    onHabitClick: () -> Unit,
    onJournalEntryClick: (entryId: String) -> Unit,
    onCoachClick: (coachId: String) -> Unit,
    onSettingClick: (SearchDestination) -> Unit,
    initialFilter: SearchFilter? = null,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val selectedFilters by viewModel.selectedFilters.collectAsState()
    val focusRequester = remember { FocusRequester() }

    // Apply the pre-selected filter passed from the calling screen (e.g. "All Goals" on Home)
    LaunchedEffect(initialFilter) {
        viewModel.setFilter(initialFilter)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val showBrowseState = query.isBlank() && selectedFilters.isNotEmpty() && results.isEmpty
    val showEmptyState = query.isBlank() && selectedFilters.isEmpty()
    val showNoResults = query.isNotBlank() && results.isEmpty

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // ── Results / states ───────────────────────────────────────────────
        when {
            showEmptyState -> SearchEmptyState(modifier = Modifier.fillMaxSize())
            showBrowseState -> SearchBrowseState(
                filters = selectedFilters,
                modifier = Modifier.fillMaxSize(),
            )
            showNoResults -> SearchNoResults(query = query, modifier = Modifier.fillMaxSize())
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    start = LifePlannerDesign.Padding.screenHorizontal,
                    end = LifePlannerDesign.Padding.screenHorizontal,
                    bottom = 136.dp, // chips (48) + search bar (64) + padding
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (results.goals.isNotEmpty()) {
                    item { SectionHeader("Goals", results.goals.size) }
                    items(results.goals, key = { "goal_${it.id}" }) { goal ->
                        GoalResultRow(goal = goal, onClick = { onGoalClick(goal.id) })
                    }
                }

                if (results.milestones.isNotEmpty()) {
                    item { SectionHeader("Milestones", results.milestones.size) }
                    items(results.milestones, key = { "ms_${it.milestone.id}" }) { item ->
                        MilestoneResultRow(item = item, onClick = { onGoalClick(item.goal.id) })
                    }
                }

                if (results.habits.isNotEmpty()) {
                    item { SectionHeader("Habits", results.habits.size) }
                    items(results.habits, key = { "habit_${it.habit.id}" }) { habit ->
                        HabitResultRow(habit = habit, onClick = onHabitClick)
                    }
                }

                if (results.journals.isNotEmpty()) {
                    item { SectionHeader("Journal", results.journals.size) }
                    items(results.journals, key = { "journal_${it.id}" }) { entry ->
                        JournalResultRow(entry = entry, onClick = { onJournalEntryClick(entry.id) })
                    }
                }

                if (results.coaches.isNotEmpty()) {
                    item { SectionHeader("Coaches", results.coaches.size) }
                    items(results.coaches, key = { "coach_${it.id}" }) { coach ->
                        CoachResultRow(coach = coach, onClick = { onCoachClick(coach.id) })
                    }
                }

                if (results.settings.isNotEmpty()) {
                    item { SectionHeader("Navigate", results.settings.size) }
                    items(results.settings, key = { "setting_${it.destination}" }) { item ->
                        SettingsResultRow(item = item, onClick = { onSettingClick(item.destination) })
                    }
                }
            }
        }

        // ── Bottom: chips + search bar ─────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        ) {
            FilterChipsRow(
                selectedFilters = selectedFilters,
                onFilterToggle = viewModel::toggleFilter,
                onClearAll = { viewModel.setFilter(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            SearchBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onBack = onBack,
                focusRequester = focusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
