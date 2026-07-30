@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import az.tribe.lifeplanner.domain.model.CalendarEvent
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatform

actual class CalendarReader {

    private val context: Context by lazy { KoinPlatform.getKoin().get() }

    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun readUpcomingEvents(days: Int): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        return readEvents(now, now + days.toLong() * 24L * 60L * 60L * 1000L)
    }

    actual suspend fun readEvents(startEpochMillis: Long, endEpochMillis: Long): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }

        // Instances (rather than Events) expands recurring events into concrete occurrences within
        // the window, which is what "events for this day" means to the user.
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().let { b ->
            ContentUris.appendId(b, startEpochMillis)
            ContentUris.appendId(b, endEpochMillis)
            b.build()
        }
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
        )

        val events = mutableListOf<CalendarEvent>()
        try {
            context.contentResolver.query(
                uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { c ->
                val idIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                val locIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
                val calIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
                while (c.moveToNext()) {
                    val begin = c.getLong(beginIdx)
                    events += CalendarEvent(
                        // EVENT_ID repeats across recurring instances, so key by id + start.
                        id = "${c.getLong(idIdx)}@$begin",
                        title = c.getString(titleIdx)?.takeIf { it.isNotBlank() } ?: "(no title)",
                        startEpochMillis = begin,
                        endEpochMillis = c.getLong(endIdx),
                        allDay = c.getInt(allDayIdx) == 1,
                        location = c.getString(locIdx)?.takeIf { it.isNotBlank() },
                        calendarName = c.getString(calIdx)?.takeIf { it.isNotBlank() },
                    )
                }
            }
        } catch (e: Exception) {
            Logger.w("CalendarReader") { "Failed to read calendar events: ${e.message}" }
        }
        events
    }
}
