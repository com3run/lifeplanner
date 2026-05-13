package az.tribe.lifeplanner.widget.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import az.tribe.lifeplanner.MainActivity

class AddGoalWidgetCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("lifeplanner://goal_wizard"), context, MainActivity::class.java)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        )
    }
}
