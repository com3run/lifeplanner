package az.tribe.lifeplanner.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.action.clickable
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import az.tribe.lifeplanner.shared.R
import az.tribe.lifeplanner.widget.action.AddGoalWidgetCallback
import az.tribe.lifeplanner.widget.action.AskCoachWidgetCallback
import az.tribe.lifeplanner.widget.theme.WidgetColorProviders

class QuickActionsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(colors = WidgetColorProviders) {
                val isSmall = LocalSize.current.width < 200.dp
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(16.dp)
                        .background(GlanceTheme.colors.surface)
                        .padding(12.dp)
                ) {
                    if (isSmall) SmallLayout() else MediumLayout()
                }
            }
        }
    }

    companion object {
        val SMALL_SIZE = DpSize(110.dp, 110.dp)
        val MEDIUM_SIZE = DpSize(250.dp, 110.dp)
    }
}

@androidx.compose.runtime.Composable
private fun SmallLayout() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionButton(
            icon = R.drawable.ic_shortcut_goals,
            label = "New Goal",
            callback = actionRunCallback<AddGoalWidgetCallback>(),
            modifier = GlanceModifier.fillMaxWidth()
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        ActionButton(
            icon = R.drawable.ic_shortcut_coach,
            label = "Ask Coach",
            callback = actionRunCallback<AskCoachWidgetCallback>(),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

@androidx.compose.runtime.Composable
private fun MediumLayout() {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "Quick Actions",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = R.drawable.ic_shortcut_goals,
                label = "New Goal",
                callback = actionRunCallback<AddGoalWidgetCallback>(),
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            ActionButton(
                icon = R.drawable.ic_shortcut_coach,
                label = "Ask Coach",
                callback = actionRunCallback<AskCoachWidgetCallback>(),
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun ActionButton(
    icon: Int,
    label: String,
    callback: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .cornerRadius(12.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .clickable(callback)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = label,
                modifier = GlanceModifier.width(16.dp).height(16.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}
