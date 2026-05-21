package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import az.tribe.lifeplanner.ui.navigation.BottomNavItem
import az.tribe.lifeplanner.ui.theme.LifePlannerGradients

/**
 * Contextual action for the circular button at the end of the nav bar.
 * Changes based on current screen and sub-state (e.g. Hub tab).
 */
data class NavContextAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

private val NavBarHeight = 64.dp
private val CircleButtonSize = 64.dp
private val GroupPillPadding = 4.dp
private val ItemCornerRadius = 14.dp

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem> = BottomNavItem.items,
    isVisible: Boolean = true,
    contextAction: NavContextAction? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pillShape = RoundedCornerShape(50)
    val isDark = true
    val pillBackground = if (isDark) LifePlannerGradients.glassNavDark else LifePlannerGradients.glassOverlayHigh
    val pillBorder = if (isDark) LifePlannerGradients.glassBorderDark else LifePlannerGradients.glassBorder

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Grouped pill with all nav items ──────────────────────────────
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(NavBarHeight)
                    .clip(pillShape)
                    .border(
                        width = 1.dp,
                        brush = pillBorder,
                        shape = pillShape
                    )
                    .background(pillBackground)
                    .padding(GroupPillPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val interactionSource = remember { MutableInteractionSource() }

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                        animationSpec = tween(220),
                        label = "navItemBg"
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(bgColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationRoute!!) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )

                        // Label animates in/out when selected state changes
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(200)) + expandHorizontally(tween(220)),
                            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(200))
                        ) {
                            Row {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ── Standalone circle action button ──────────────────────────────
            contextAction?.let { action ->
                Box(
                    modifier = Modifier
                        .size(CircleButtonSize)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            brush = pillBorder,
                            shape = CircleShape
                        )
                        .background(pillBackground)
                        .clickable(onClick = action.onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
