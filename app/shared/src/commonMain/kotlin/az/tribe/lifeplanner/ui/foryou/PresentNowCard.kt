package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.service.PresentMoment
import az.tribe.lifeplanner.domain.service.StepDuration
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Flag
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val LateRed = Color(0xFFE53935)

/**
 * The top of the Present tab: what is happening in this hour, said once.
 *
 * It sits above the day's plan on purpose. The plan answers "what does today hold"; this answers
 * "what now", and those are different questions that were being run together. Everything it can
 * show is either under way (an event) or one tap from done (a step, a habit), so the card is never
 * only an announcement.
 */
@Composable
fun PresentNowCard(
    moment: PresentMoment.Moment,
    tz: TimeZone,
    onAct: (() -> Unit)? = null,
    onOpen: (() -> Unit)? = null,
) {
    val c = MaterialTheme.modernColors
    val accent = when (moment.kind) {
        PresentMoment.Kind.LATE_STEP -> LateRed
        PresentMoment.Kind.HABIT -> c.success
        else -> c.primary
    }
    val icon = when (moment.kind) {
        PresentMoment.Kind.EVENT_NOW, PresentMoment.Kind.EVENT_SOON -> PhosphorIcons.Regular.CalendarBlank
        PresentMoment.Kind.HABIT -> PhosphorIcons.Regular.ArrowsClockwise
        else -> PhosphorIcons.Regular.Flag
    }
    val eyebrow = when (moment.kind) {
        PresentMoment.Kind.EVENT_NOW -> "HAPPENING NOW"
        PresentMoment.Kind.EVENT_SOON -> "IN ${countdown(moment.minutesUntil ?: 0)}"
        PresentMoment.Kind.LATE_STEP -> "STILL OPEN"
        PresentMoment.Kind.STEP -> "NEXT UP"
        PresentMoment.Kind.HABIT -> "RIGHT NOW"
    }
    // The second line carries the clock for events (how long is left, when it starts) and the
    // context for everything else (which goal this step serves).
    val detail = when (moment.kind) {
        PresentMoment.Kind.EVENT_NOW -> listOfNotNull(
            moment.endsAtEpochMillis?.let { "Until ${clockLabel(it, tz)}" },
            moment.detail,
        ).joinToString(" · ").ifBlank { null }
        else -> moment.detail
    }

    Surface(
        modifier = Modifier.fillMaxWidth().let { m -> if (onOpen != null) m.bouncyClickable(onClick = onOpen) else m },
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(color = accent.copy(alpha = 0.18f), shape = CircleShape) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(8.dp).size(LifePlannerDesign.IconSize.medium),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    eyebrow,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                    color = accent,
                )
                Text(
                    moment.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = c.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                detail?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            // Only things the user can finish get a tick. An event is attended, not completed.
            // A step that names a length of time gets the countdown it gets in the plan: promoting
            // it to the top of the screen should not take the timer away from it.
            val milestoneId = moment.milestoneId
            val seconds = remember(moment.title, milestoneId) {
                if (milestoneId == null) null else StepDuration.secondsIn(moment.title)
            }
            val act = onAct
            val timer = if (act != null && seconds != null && milestoneId != null) {
                rememberStepTimer(milestoneId, seconds, act)
            } else null
            when {
                timer != null -> StepTimerControl(timer, accent = accent)

                act != null -> Icon(
                    imageVector = PhosphorIcons.Regular.Circle,
                    contentDescription = "Mark done",
                    tint = accent,
                    modifier = Modifier
                        .clip(CircleShape)
                        .bouncyClickable(onClick = act)
                        .padding(4.dp)
                        .size(LifePlannerDesign.IconSize.large),
                )
            }
        }
    }
}

/** "45 MIN" under an hour, "1 HR 15 MIN" over it. Whole hours drop the trailing zero minutes. */
private fun countdown(minutes: Int): String {
    if (minutes < 60) return "$minutes MIN"
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "$h HR" else "$h HR $m MIN"
}

private fun clockLabel(epochMillis: Long, tz: TimeZone): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(tz)
    val h12 = when (val h = dt.hour % 12) { 0 -> 12; else -> h }
    val mm = dt.minute.toString().padStart(2, '0')
    return "$h12:$mm ${if (dt.hour < 12) "AM" else "PM"}"
}
