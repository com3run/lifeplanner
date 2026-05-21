package az.tribe.lifeplanner.ui.habit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Barbell
import com.adamglin.phosphoricons.regular.Book
import com.adamglin.phosphoricons.regular.Drop
import com.adamglin.phosphoricons.regular.Flower
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.PiggyBank
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.Mood

// Habit template data class
data class HabitTemplate(
    val title: String,
    val description: String,
    val category: GoalCategory,
    val frequency: HabitFrequency,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

// Predefined habit templates
val habitTemplates = listOf(
    HabitTemplate(
        title = "Morning Meditation",
        description = "10 minutes of mindfulness",
        category = GoalCategory.PURPOSE,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.Flower,
        gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
    ),
    HabitTemplate(
        title = "Exercise",
        description = "30 minutes workout",
        category = GoalCategory.BODY,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.Barbell,
        gradientColors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
    ),
    HabitTemplate(
        title = "Read",
        description = "Read for 20 minutes",
        category = GoalCategory.CAREER,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.Book,
        gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
    ),
    HabitTemplate(
        title = "Drink Water",
        description = "8 glasses of water",
        category = GoalCategory.BODY,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.Drop,
        gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
    ),
    HabitTemplate(
        title = "Journal",
        description = "Write daily reflections",
        category = GoalCategory.WELLBEING,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.PencilSimple,
        gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C))
    ),
    HabitTemplate(
        title = "Sleep Early",
        description = "Be in bed by 10 PM",
        category = GoalCategory.BODY,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.Moon,
        gradientColors = listOf(Color(0xFF5B247A), Color(0xFF1BCEDF))
    ),
    HabitTemplate(
        title = "Connect with Family",
        description = "Quality time with loved ones",
        category = GoalCategory.PEOPLE,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.Heart,
        gradientColors = listOf(Color(0xFFFC466B), Color(0xFF3F5EFB))
    ),
    HabitTemplate(
        title = "Save Money",
        description = "Track daily expenses",
        category = GoalCategory.MONEY,
        frequency = HabitFrequency.DAILY,
        icon = PhosphorIcons.Regular.PiggyBank,
        gradientColors = listOf(Color(0xFF434343), Color(0xFF000000))
    )
)

@Composable
internal fun MoodOption(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = mood.emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
