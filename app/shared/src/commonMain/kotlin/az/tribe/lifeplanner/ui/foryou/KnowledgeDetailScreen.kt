package az.tribe.lifeplanner.ui.foryou

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import az.tribe.lifeplanner.domain.service.KnowledgeLibrary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.illus_learn_habits
import leanlifeplanner.app.shared.generated.resources.illus_learn_motivation
import leanlifeplanner.app.shared.generated.resources.illus_learn_focus
import leanlifeplanner.app.shared.generated.resources.illus_learn_hero
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GuidedBreathSession
import az.tribe.lifeplanner.ui.components.KeepScreenOn
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.X
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject
import kotlin.time.Clock

/**
 * A Learn lesson in **reader mode**: the app gets out of the way (no tabs, no cards competing for
 * attention, big set text) and the lesson becomes a short *session* rather than a page you can
 * bounce off. A session runs [SESSION_MS]; the lesson counts as read once the reader has both
 * reached the end and stayed for it.
 *
 * Fast readers are not punished for being fast: reaching the end early offers a guided breath to
 * spend the remaining seconds on, which counts as a breath in its own right. Shared Android + iOS.
 *
 * The end of a lesson is a decision, not a chore: instead of "turn this into a habit / goal" the
 * reader says what they make of it, gets a line back, and is handed straight to the next lesson.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeDetailScreen(
    bit: KnowledgeBit,
    onBackClick: () -> Unit,
    /** Open another lesson in place (the "next up" hand-off at the end of this one). */
    onOpenLesson: (String) -> Unit = {},
    nextLesson: KnowledgeBit? = null,
    /** Fired once the reader has reached the end of the lesson *and* the session has run its course. */
    onCompleted: () -> Unit = {},
    earnedXp: Int = 0,
    earnedBadgeName: String? = null,
) {
    val c = MaterialTheme.modernColors
    val settings: Settings = koinInject()
    val listState = rememberLazyListState()

    // The session clock. Stored as a start stamp (not a tick count) so backgrounding, rotation, and
    // recomposition can't rewind it, and reading with the screen off isn't rewarded either way.
    val startedAt by rememberSaveable(bit.id) { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var elapsedMs by remember(bit.id) { mutableStateOf(0L) }
    LaunchedEffect(bit.id) {
        while (elapsedMs < SESSION_MS) {
            elapsedMs = Clock.System.now().toEpochMilliseconds() - startedAt
            delay(250)
        }
        elapsedMs = SESSION_MS
    }
    val sessionDone = elapsedMs >= SESSION_MS

    // "Read" has to mean read. canScrollForward goes false both when the reader scrolls to the
    // bottom and when a short lesson fits without scrolling.
    val atEnd by remember { derivedStateOf { !listState.canScrollForward } }
    val finished = atEnd && sessionDone
    LaunchedEffect(finished) { if (finished) onCompleted() }

    var breathing by remember { mutableStateOf(false) }
    var reflection by remember(bit.id) { mutableStateOf(settings.getStringOrNull(reflectionKey(bit.id))) }

    // The screen stays awake for the session: a two-minute read shouldn't be interrupted by a
    // display timeout the reader has to keep tapping away.
    KeepScreenOn(enabled = !sessionDone)

    Scaffold(
        containerColor = c.background,
        topBar = {
            Column {
                TopAppBar(
                    // Reader mode: no title bar shouting "Learn", just the way out.
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(PhosphorIcons.Regular.X, contentDescription = "Close", tint = c.textTertiary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background),
                )
                SessionProgressLine(fraction = (elapsedMs.toFloat() / SESSION_MS).coerceIn(0f, 1f))
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + LifePlannerDesign.Spacing.sm,
                bottom = padding.calculateBottomPadding() + 96.dp,
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                    Box(Modifier.fillMaxWidth().height(150.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = c.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
                        ) {}
                        Image(
                            painter = painterResource(heroIllustration(bit.id)),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.align(Alignment.Center).height(120.dp),
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.BottomStart)
                                .padding(LifePlannerDesign.Spacing.sm).size(40.dp),
                            shape = CircleShape,
                            color = c.cardBackground,
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(bit.emoji, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    Text(
                        KnowledgeLibrary.collectionOf(bit.id)?.title?.uppercase() ?: "LEARN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = c.primary,
                    )
                    Text(
                        bit.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 38.sp),
                        color = c.textPrimary,
                    )
                }
            }

            // Set for reading, not for scanning: larger type, loose line height, one idea per block.
            val paragraphs = bit.detail.ifEmpty { listOf(bit.body) }
            items(paragraphs, key = { it }) { paragraph ->
                Text(
                    paragraph,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 32.sp),
                    color = c.textPrimary.copy(alpha = 0.88f),
                )
            }

            if (bit.takeaway.isNotBlank()) {
                item(key = "takeaway") {
                    Surface(
                        color = c.primary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
                            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
                        ) {
                            Text(
                                "TRY THIS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = c.primary,
                            )
                            Text(
                                bit.takeaway,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 27.sp),
                                color = c.textPrimary,
                            )
                        }
                    }
                }
            }

            bit.source?.let { src ->
                item(key = "source") {
                    Text("Source: $src", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                }
            }

            // Read faster than the session? Sit with it, or spend the rest of the minute breathing.
            if (atEnd && !sessionDone) {
                item(key = "settle") {
                    SettleCard(
                        secondsLeft = ((SESSION_MS - elapsedMs) / 1000L).toInt().coerceAtLeast(0),
                        onBreathe = { breathing = true },
                    )
                }
            }

            if (earnedXp > 0 || earnedBadgeName != null) {
                item(key = "reward") {
                    Surface(
                        color = c.success.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (earnedXp > 0) {
                                Text(
                                    "+$earnedXp XP",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = c.success,
                                )
                                Text("Lesson complete.", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            if (earnedBadgeName != null) {
                                Text(
                                    "🏅 Zone cleared, $earnedBadgeName earned",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = c.textPrimary,
                                )
                            }
                        }
                    }
                }
            }

            // The close of the session: a decision about the idea, then straight into the next read.
            item(key = "reflection") {
                AnimatedVisibility(visible = sessionDone, enter = fadeIn() + expandVertically()) {
                    ReflectionCard(
                        bit = bit,
                        choice = reflection,
                        onChoose = {
                            reflection = it
                            settings.putString(reflectionKey(bit.id), it)
                        },
                    )
                }
            }

            if (nextLesson != null) {
                item(key = "next") {
                    AnimatedVisibility(visible = sessionDone, enter = fadeIn() + expandVertically()) {
                        NextLessonCard(next = nextLesson, onOpen = { onOpenLesson(nextLesson.id) })
                    }
                }
            }
        }
    }

    if (breathing) {
        GuidedBreathSession(onClose = { breathing = false })
    }
}

/** How long a Learn session runs. Long enough to actually land, short enough to say yes to. */
private const val SESSION_MS = 120_000L

private val REFLECTION_CHOICES = listOf("I'll try it", "Already do this", "Not for me")

private fun reflectionKey(lessonId: String) = "lesson_decision_$lessonId"

/** The session clock as a hairline under the toolbar: present, but nothing to watch. */
@Composable
private fun SessionProgressLine(fraction: Float) {
    val c = MaterialTheme.modernColors
    val animated by animateFloatAsState(targetValue = fraction, label = "sessionProgress")
    Box(Modifier.fillMaxWidth().height(2.dp).background(c.primary.copy(alpha = 0.10f))) {
        Box(Modifier.fillMaxWidth(animated).height(2.dp).background(c.primary.copy(alpha = 0.55f)))
    }
}

/** Shown to readers who finish ahead of the clock: sit with it, or breathe out the difference. */
@Composable
private fun SettleCard(secondsLeft: Int, onBreathe: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Text(
                "Quick reader.",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = c.textPrimary,
            )
            Text(
                "${secondsLeft}s left in this session. Reading it is half of it, letting it land is the other half.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
            AppButton(
                text = "Breathe while it lands",
                onClick = onBreathe,
                variant = AppButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * The end of a lesson as a decision. Three answers, one line back from the app, and the choice is
 * remembered so returning to the lesson shows what you decided last time.
 */
@Composable
private fun ReflectionCard(bit: KnowledgeBit, choice: String?, onChoose: (String) -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Text(
                "So, what do you make of it?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                REFLECTION_CHOICES.forEach { option ->
                    val selected = option == choice
                    Surface(
                        modifier = Modifier.weight(1f).bouncyClickable { onChoose(option) },
                        color = if (selected) c.primary else c.primary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full),
                    ) {
                        Text(
                            option,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) c.background else c.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        )
                    }
                }
            }
            if (choice != null) {
                Text(
                    responseTo(choice, bit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                )
            }
        }
    }
}

/** What the app says back. Short, specific to the decision, never a lecture. */
private fun responseTo(choice: String, bit: KnowledgeBit): String = when (choice) {
    REFLECTION_CHOICES[0] ->
        bit.takeaway.ifBlank { "Good. The smallest version of it, today, beats the perfect version later." }
    REFLECTION_CHOICES[1] -> "Then you already have the evidence. Watch for the week it starts slipping."
    else -> "Fair enough. Deciding what you won't do is worth as much as deciding what you will."
}

/** The hand-off: reading is a path, so the next step is right here rather than back in the hub. */
@Composable
private fun NextLessonCard(next: KnowledgeBit, onOpen: () -> Unit) {
    val c = MaterialTheme.modernColors
    val path = KnowledgeLibrary.collectionOf(next.id)?.title
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onOpen),
        color = c.primary.copy(alpha = 0.10f),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(next.emoji, style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (path != null) "NEXT IN ${path.uppercase()}" else "NEXT UP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = c.primary,
                )
                Text(
                    next.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = c.textPrimary,
                )
                Text("${next.readMin} min", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
            Icon(PhosphorIcons.Regular.ArrowRight, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
        }
    }
}

/** The path illustration for a lesson (matches the Learn hub), falling back to the generic hero. */
private fun heroIllustration(lessonId: String): DrawableResource =
    when (KnowledgeLibrary.collectionOf(lessonId)?.id) {
        "col_habits" -> Res.drawable.illus_learn_habits
        "col_motivation" -> Res.drawable.illus_learn_motivation
        "col_mind" -> Res.drawable.illus_learn_focus
        else -> Res.drawable.illus_learn_hero
    }
