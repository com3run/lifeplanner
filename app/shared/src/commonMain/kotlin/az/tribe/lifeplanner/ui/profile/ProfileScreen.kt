package az.tribe.lifeplanner.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.BuildKonfig
import kotlinx.coroutines.launch
import az.tribe.lifeplanner.data.sync.SyncState
import az.tribe.lifeplanner.domain.enum.AiProvider
import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.ui.components.AchievementsCard
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.ui.home.HomeCoachAICard
import az.tribe.lifeplanner.ui.home.HomeViewModel
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.auth.AuthBottomSheet
import az.tribe.lifeplanner.ui.calendar.rememberCalendarPermission
import az.tribe.lifeplanner.ui.health.HealthViewModel
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.*
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bell
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.User
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.ChatCircleText
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.CloudSlash
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Scales
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.SignOut
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = koinInject(),
    gamificationViewModel: GamificationViewModel = koinViewModel(),
    weeklyEngagementViewModel: WeeklyEngagementViewModel = koinViewModel(),
    healthViewModel: HealthViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    onNavigateToAchievements: () -> Unit,
    onNavigateToHealth: () -> Unit = {},
    onNavigateToReminders: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToRetrospective: () -> Unit = {},
    onNavigateToScreenTimeInsight: () -> Unit = {},
    onNavigateToToday: () -> Unit = {},
    onNavigateToGoalsRedesign: () -> Unit = {},
    onNavigateToYouRedesign: () -> Unit = {},
    onNavigateToOnboardingRedesign: () -> Unit = {},
    onNavigateToDecisions: () -> Unit = {},
    onNavigateToCausalInsights: () -> Unit = {},
    onNavigateToBecoming: () -> Unit = {},
    onNavigateToDecisionReview: () -> Unit = {},
    onNavigateToYourWiring: () -> Unit = {},
    onNavigateToAICoach: () -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onResetOnboarding: () -> Unit = {}
) {
    val settings: Settings = koinInject()
    val authState by authViewModel.authState.collectAsState()
    val syncStatus by authViewModel.syncStatus.collectAsState()
    val isLocalOnlyGuest by authViewModel.isLocalOnlyGuest.collectAsState()
    val userProgress by gamificationViewModel.userProgress.collectAsState()
    val badges by gamificationViewModel.badges.collectAsState()
    val weeklyEngagement by weeklyEngagementViewModel.engagement.collectAsState()
    val healthPermissionState by healthViewModel.permissionState.collectAsState()
    val recentSession by homeViewModel.recentSession.collectAsState()
    val recentCoach by homeViewModel.recentCoach.collectAsState()
    val calendarPermission = rememberCalendarPermission()

    var showAccountSheet by remember { mutableStateOf(false) }
    var showAiProviderDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedAiProvider by remember {
        val saved = settings.getStringOrNull("ai_provider")
        mutableStateOf(saved?.let {
            try { AiProvider.valueOf(it) } catch (_: Exception) { AiProvider.GEMINI }
        } ?: AiProvider.GEMINI)
    }

    val currentUser = when (authState) {
        is AuthState.Authenticated -> (authState as AuthState.Authenticated).user
        is AuthState.Guest -> (authState as AuthState.Guest).user
        else -> null
    }

    LaunchedEffect(Unit) {
        gamificationViewModel.refresh()
        weeklyEngagementViewModel.load()
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
            if (authState is AuthState.Authenticated) {
                item {
                    UserProfileHeaderCard(
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

            item { userProgress?.let { progress -> ProfileStatsCard(progress, weeklyEngagement) } }

            item { ProfileSectionHeader("AI Coach & Achievements") }
            item {
                HomeCoachAICard(
                    session = recentSession,
                    coach = recentCoach,
                    coachUnlocked = true,
                    onClick = onNavigateToAICoach,
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

            item { ProfileSectionHeader("Integrations") }
            item {
                ProfileMenuItem(
                    icon = PhosphorIcons.Regular.Brain,
                    title = "AI Provider",
                    subtitle = selectedAiProvider.displayName,
                    onClick = { showAiProviderDialog = true }
                )
            }
            item {
                HealthConnectionCard(
                    permissionState = healthPermissionState,
                    onConnect = onNavigateToHealth,
                    onSync = { healthViewModel.syncHealth() }
                )
            }
            item {
                CalendarIntegrationCard(
                    permissionState = calendarPermission.state,
                    onConnect = calendarPermission.request
                )
            }

            item { ProfileSectionHeader("Preview") }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.User, title = "You (new design)", subtitle = "Preview the redesigned profile + dark-mode toggle", onClick = onNavigateToYouRedesign) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.Sparkle, title = "Onboarding (new design)", subtitle = "Preview the redesigned first-run flow", onClick = onNavigateToOnboardingRedesign) }

            item { ProfileSectionHeader("Settings") }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.Bell, title = "Reminders", subtitle = "Notification preferences", onClick = onNavigateToReminders) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.CloudArrowUp, title = "Backup & Sync", subtitle = "Export and restore your data", onClick = onNavigateToBackup) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.ClockCounterClockwise, title = "Day Retrospective", subtitle = "Browse past days and activity", onClick = onNavigateToRetrospective) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.ClockCounterClockwise, title = "Decision Journal", subtitle = "Your choices, reasoning, and outcomes", onClick = onNavigateToDecisions) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.ChartBar, title = "Causal Insights", subtitle = "What actually drives your progress", onClick = onNavigateToCausalInsights) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.Brain, title = "Becoming", subtitle = "Who you're becoming, values & identity", onClick = onNavigateToBecoming) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.Scales, title = "Review Decisions", subtitle = "Grade your reasoning, not just outcomes", onClick = onNavigateToDecisionReview) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.Sliders, title = "Your Wiring", subtitle = "How you're wired, your decision-making profile", onClick = onNavigateToYourWiring) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.ChartBar, title = "My Patterns", subtitle = "See how you use the app + personalized tips", onClick = onNavigateToScreenTimeInsight) }
            item { ProfileMenuItem(icon = PhosphorIcons.Regular.ChatCircleText, title = "Send Feedback", subtitle = "Report bugs, request features", onClick = onNavigateToFeedback) }

            if (authState is AuthState.Authenticated && currentUser?.email != null) {
                item { ProfileSectionHeader("Account") }
                item { ProfileMenuItem(icon = PhosphorIcons.Regular.SignOut, title = "Sign Out", subtitle = currentUser?.email ?: "", onClick = { showSignOutConfirm = true }) }
            }

            item {
                @OptIn(ExperimentalFoundationApi::class)
                Text(
                    text = "v${BuildKonfig.APP_VERSION}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                onResetOnboarding()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Onboarding reset")
                                }
                            }
                        )
                )
            }
        }
    }

    if (showAiProviderDialog) {
        AiProviderDialog(
            currentProvider = selectedAiProvider,
            isGuest = currentUser?.isGuest != false,
            userLevel = userProgress?.currentLevel ?: 1,
            onProviderSelected = { provider ->
                selectedAiProvider = provider
                settings.putString("ai_provider", provider.name)
                showAiProviderDialog = false
            },
            onDismiss = { showAiProviderDialog = false }
        )
    }

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

    if (showSignOutConfirm) {
        val isOfflineOrError = syncStatus.state == SyncState.OFFLINE || syncStatus.state == SyncState.ERROR
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(if (isOfflineOrError) "Unsynced Changes" else "Ready to leave?", fontWeight = FontWeight.Bold) },
            text = {
                if (isOfflineOrError) {
                    Column {
                        Text("Some of your recent changes haven't been saved to the cloud yet. Signing out now could result in losing them.", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Try connecting to the internet and waiting for sync to finish first.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Text("Everything is synced. You can sign back in anytime to pick up where you left off.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showSignOutConfirm = false; authViewModel.signOut() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(if (isOfflineOrError) "Leave Anyway" else "Yes, Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(if (isOfflineOrError) "Wait for Sync" else "Stay")
                }
            }
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
