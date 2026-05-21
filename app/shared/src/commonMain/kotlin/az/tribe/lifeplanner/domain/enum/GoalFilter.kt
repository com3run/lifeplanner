package az.tribe.lifeplanner.domain.enum

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.List
import com.adamglin.phosphoricons.regular.Play

enum class GoalFilter(val displayName: String, val icon: ImageVector) {
    ALL("All", PhosphorIcons.Regular.List),
    ACTIVE("Active", PhosphorIcons.Regular.Play),
    COMPLETED("Completed", PhosphorIcons.Regular.CheckCircle)
}
