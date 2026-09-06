package az.tribe.lifeplanner.ui.profile

import az.tribe.lifeplanner.core.FeatureFlags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.ui.components.AchievementsCard
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.ui.home.HomeCoachAICard
import az.tribe.lifeplanner.ui.home.HomeViewModel
import az.tribe.lifeplanner.ui.intro.FeatureIntroCatalog
import az.tribe.lifeplanner.ui.intro.FeatureIntroHost
import az.tribe.lifeplanner.ui.intro.rememberFeatureIntroGate
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.auth.AuthBottomSheet
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.updateDisplayName
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.CloudSlash
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.Scales
import com.adamglin.phosphoricons.regular.Sliders
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    gamificationViewModel: GamificationViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    youViewModel: YouViewModel = koinViewModel(),
    onNavigateToAchievements: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    /** Deep link for the adaptive attention cards, which carry a route rather than a callback. */
    onNavigateToRoute: (String) -> Unit = {},
    onNavigateToRetrospective: () -> Unit = {},
    onNavigateToScreenTimeInsight: () -> Unit = {},
    onNavigateToToday: () -> Unit = {},
    onNavigateToGoalsRedesign: () -> Unit = {},
    onNavigateToYouRedesign: () -> Unit = {},
    onNavigateToOnboardingRedesign: () -> Unit = {},
    onNavigateToDecisions: () -> Unit = {},
    onNavigateToCausalInsights: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToBecoming: () -> Unit = {},
    onNavigateToYourWiring: () -> Unit = {},
    onNavigateToAICoach: () -> Unit,
    onContinueCoachChat: (String) -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
) {
    // The deeper screens here are ones the user may never have seen. The gate lets each one
    // introduce itself on first tap instead of dropping them into it cold.
    val introGate = rememberFeatureIntroGate()
    val introAccent = MaterialTheme.colorScheme.primary
    val authState by authViewModel.authState.collectAsState()
    val syncStatus by authViewModel.syncStatus.collectAsState()
    val isLocalOnlyGuest by authViewModel.isLocalOnlyGuest.collectAsState()
    val userProgress by gamificationViewModel.userProgress.collectAsState()
    val badges by gamificationViewModel.badges.collectAsState()
    val recentSession by homeViewModel.recentSession.collectAsState()
    val recentCoach by homeViewModel.recentCoach.collectAsState()
    val youState by youViewModel.state.collectAsState()

    var showAccountSheet by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser = when (authState) {
        is AuthState.Authenticated -> (authState as AuthState.Authenticated).user
        is AuthState.Guest -> (authState as AuthState.Guest).user
        else -> null
    }

    // Signals go stale while the tab sits in the back stack; recompute whenever it comes forward.
    LaunchedEffect(Unit) {
        gamificationViewModel.refresh()
        youViewModel.load()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
                bottom = padding.calculateBottomPadding() + 84.dp,
                top = padding.calculateTopPadding()+16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md)
        ) {
            // Guests level up too, so the hero (which now carries XP/level/streak) is not
            // signed-in-only the way the old identity-only header was.
            if (currentUser != null) {
                item {
                    ProfilePaperHeader(
                        user = currentUser,
                        userProgress = userProgress,
                        syncStatus = syncStatus,
                        onRetrySync = { authViewModel.retrySync() },
                        onEditName = { showEditNameDialog = true }
                    )
                }
            }

            if (currentUser?.email == null && !isLocalOnlyGuest) {
                item { SecureAccountCTABanner(onClick = { showAccountSheet = true }) }
            }

            if (isLocalOnlyGuest) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToSignIn() }.padding(LifePlannerDesign.Padding.standard),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(PhosphorIcons.Regular.CloudSlash, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Offline Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Data is stored locally. Tap to sign in and sync.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(8.dp))
                            Switch(checked = false, onCheckedChange = { onNavigateToSignIn() })
                        }
                    }
                }
            }

            // ── Needs you ─────────────────────────────────────────────────
            // Absent unless something actually qualifies, so its presence is itself the signal.
            if (youState.attention.isNotEmpty()) {
                item { ProfileSectionHeader("Needs you") }
                items(youState.attention, key = { it.id }) { a ->
                    AttentionCard(a, onClick = { onNavigateToRoute(a.route) })
                }
            }

            // ── Where you stand ───────────────────────────────────────────
            // Each card is one feature's answer, not a link to go find it. They degrade to an
            // honest invitation when there isn't data yet, so nothing becomes unreachable.
            // The 0-100 "Life balance" glance used to lead this section, one tap away from the
            // wheel answering the same question 0-10. The owner does not rely on it; the wheel
            // is the one instrument, and it already leads the For You feed.
            item { ProfileSectionHeader("Where you stand") }

            if (FeatureFlags.PILLAR_CAUSAL) {
                item {
                    val insight = youState.topInsight
                    FindingCard(
                        icon = PhosphorIcons.Regular.Lightning,
                        accent = MaterialTheme.colorScheme.tertiary,
                        headline = insight?.statement ?: "What drives your progress",
                        detail = if (insight != null)
                            "Across ${insight.sampleSize} days of your own data."
                        else
                            "Keep logging and we'll start showing what actually moves your numbers.",
                        footnote = insight?.let { "${it.confidence.name.lowercase()} confidence" },
                        onClick = onNavigateToCausalInsights,
                    )
                }
            }

            item {
                val peak = youState.peakHourLabel
                FindingCard(
                    icon = PhosphorIcons.Regular.ChartBar,
                    accent = MaterialTheme.colorScheme.secondary,
                    headline = if (peak != null) "You show up most in the $peak" else "Your patterns",
                    detail = if (peak != null)
                        "Your follow-through is strongest then. Worth anchoring what matters to that window."
                    else
                        "Not enough signal yet. Keep using the app and your rhythms will show up here.",
                    onClick = { introGate.open(FeatureIntroCatalog.MY_PATTERNS, introAccent, onNavigateToScreenTimeInsight) },
                )
            }

            item {
                ProfileMenuItem(
                    icon = PhosphorIcons.Regular.ClockCounterClockwise,
                    title = "Look back",
                    subtitle = "Browse any past day, what you did and how it went",
                    onClick = { introGate.open(FeatureIntroCatalog.WEEKLY_REVIEW, introAccent, onNavigateToRetrospective) },
                )
            }

            // ── Your thinking ─────────────────────────────────────────────
            // Reviewing decisions now lives inside the Decision Journal, so this is one entry, not two.
            item { ProfileSectionHeader("Your thinking") }
            item {
                ProfileMenuItem(
                    icon = PhosphorIcons.Regular.Scales,
                    title = "Decision Journal",
                    subtitle = if (youState.attention.any { it.id == "decisions_to_review" })
                        "Your choices, reasoning, and outcomes · some awaiting review"
                    else
                        "Your choices, reasoning, and outcomes",
                    onClick = { introGate.open(FeatureIntroCatalog.DECISION_JOURNAL, introAccent, onNavigateToDecisions) },
                )
            }
            if (FeatureFlags.PILLAR_BECOMING) {
                item { ProfileMenuItem(icon = PhosphorIcons.Regular.Brain, title = "Becoming", subtitle = "Who you're becoming, values & identity", onClick = onNavigateToBecoming) }
            }
            if (FeatureFlags.PILLAR_WIRING) {
                item { ProfileMenuItem(icon = PhosphorIcons.Regular.Sliders, title = "Your Wiring", subtitle = "How you're wired, your decision-making profile", onClick = onNavigateToYourWiring) }
            }

            // ── Growth ────────────────────────────────────────────────────
            item { ProfileSectionHeader("Growth") }
            item {
                HomeCoachAICard(
                    session = recentSession,
                    coach = recentCoach,
                    coachUnlocked = true,
                    // "Continue with X" resumes that chat; the coach menu is only for starting fresh.
                    onClick = {
                        val coachId = if (recentSession != null) recentCoach?.id else null
                        if (coachId != null) onContinueCoachChat(coachId) else onNavigateToAICoach()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                AchievementsCard(
                    earnedBadges = badges.size,
                    totalBadges = BadgeType.entries.size,
                    recentBadges = badges.take(3).map { it.type },
                    onSeeAllClick = onNavigateToAchievements
                )
            }

            // Everything that configures the app rather than describes you now lives behind one
            // door, so this page stays about the person using it.
            item { Spacer(Modifier.height(LifePlannerDesign.Spacing.xs)) }
            item {
                ProfileMenuItem(
                    icon = PhosphorIcons.Regular.Gear,
                    title = "Settings",
                    subtitle = "Appearance, integrations, reminders, data, account",
                    onClick = onNavigateToSettings,
                )
            }
        }
    }

    FeatureIntroHost(introGate)

    if (showAccountSheet) {
        val isGuest = currentUser?.isGuest == true
        AuthBottomSheet(
            isSignUp = isGuest,
            authViewModel = authViewModel,
            authState = authState,
            onDismiss = { showAccountSheet = false },
            onSuccess = { showAccountSheet = false }
        )
    }

    if (showEditNameDialog) {
        var editedName by remember { mutableStateOf(currentUser?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    supportingText = if (editedName.trim().length < 2) {
                        { Text("At least 2 characters", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    isError = editedName.trim().length < 2 && editedName.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = { authViewModel.updateDisplayName(editedName); showEditNameDialog = false }, enabled = editedName.trim().length >= 2) {
                    Text("Save")
                }
            },
            dismissButton = { TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") } }
        )
    }
}
