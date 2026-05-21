package az.tribe.lifeplanner.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import az.tribe.lifeplanner.MainActivity
import az.tribe.lifeplanner.shared.R

object ShortcutHelper {

    fun register(context: Context) {
        val shortcuts = listOf(
            build(context, "focus",       "Start Focus",     R.drawable.ic_shortcut_focus,  "lifeplanner://focus"),
            build(context, "habits",      "Check In Habits", R.drawable.ic_shortcut_habits, "lifeplanner://habits"),
            build(context, "goal_wizard", "New Goal",        R.drawable.ic_shortcut_goals,  "lifeplanner://goal_wizard"),
            build(context, "ai_chat",     "Talk to Coach",   R.drawable.ic_shortcut_coach,  "lifeplanner://ai_chat"),
        )
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    private fun build(context: Context, id: String, label: String, iconRes: Int, uri: String): ShortcutInfoCompat {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri), context, MainActivity::class.java)
        return ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, iconRes))
            .setIntent(intent)
            .build()
    }
}
