package az.tribe.lifeplanner.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.health.HealthPermissionState
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.DeviceMobile
import com.adamglin.phosphoricons.regular.Footprints
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.Lock
import kotlin.time.Clock
import kotlin.time.Instant

// ── Section Header ──────────────────────────────────────────────────

@Composable
internal fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// ── Menu Item ───────────────────────────────────────────────────────

@Composable
internal fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.standard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.small))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = LifePlannerDesign.Alpha.overlay)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailingContent?.invoke() ?: Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Secure Account CTA Banner ───────────────────────────────────────

@Composable
internal fun SecureAccountCTABanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.large))
            .background(Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7))))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(PhosphorIcons.Regular.ShieldCheck, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Secure Your Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(2.dp))
                Text("Sign in or create an account to sync across devices", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(PhosphorIcons.Regular.CaretRight, contentDescription = "Get started", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 56.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CTAFeaturePill(icon = PhosphorIcons.Regular.ArrowsClockwise, label = "Auto-sync")
            CTAFeaturePill(icon = PhosphorIcons.Regular.DeviceMobile, label = "Multi-device")
            CTAFeaturePill(icon = PhosphorIcons.Fill.Lock, label = "Encrypted")
        }
    }
}

@Composable
internal fun CTAFeaturePill(icon: ImageVector, label: String) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.15f)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

// ── Health Connection Card ──────────────────────────────────────────

@Composable
internal fun HealthConnectionCard(
    permissionState: HealthPermissionState,
    onConnect: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.standard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (iconTint, bgColor) = when (permissionState) {
                HealthPermissionState.GRANTED -> Color(0xFF28C76F) to Color(0xFF28C76F).copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(bgColor)) {
                Icon(imageVector = PhosphorIcons.Regular.Footprints, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Health", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    when (permissionState) {
                        HealthPermissionState.GRANTED -> "Connected — data syncing to dashboard"
                        HealthPermissionState.DENIED -> "Connect to see steps, sleep & more on your dashboard"
                        HealthPermissionState.NOT_AVAILABLE -> "Health not available on this device"
                        HealthPermissionState.UNKNOWN -> "Checking health access..."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            when (permissionState) {
                HealthPermissionState.GRANTED -> IconButton(onClick = onSync) {
                    Icon(PhosphorIcons.Regular.ArrowsClockwise, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                HealthPermissionState.DENIED -> TextButton(onClick = onConnect) {
                    Text("Connect", style = MaterialTheme.typography.labelMedium)
                }
                else -> {}
            }
        }
    }
}

// ── Format Helpers ──────────────────────────────────────────────────

internal fun formatCompact(value: Int): String = when {
    value >= 1_000_000 -> {
        val m = value / 1_000_000.0
        val rounded = (m * 10).toLong() / 10.0
        if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}M" else "${rounded}M"
    }
    value >= 1_000 -> {
        val k = value / 1_000.0
        if (k == k.toLong().toDouble()) "${k.toLong()}k" else {
            val rounded = (k * 10).toLong() / 10.0
            "${rounded}k"
        }
    }
    else -> "$value"
}

internal fun formatLastSynced(instant: Instant?): String {
    if (instant == null) return "Synced"
    val now = Clock.System.now()
    val diff = now - instant
    val seconds = diff.inWholeSeconds
    return when {
        seconds < 60 -> "Synced just now"
        seconds < 3600 -> "Synced ${seconds / 60}m ago"
        seconds < 86400 -> "Synced ${seconds / 3600}h ago"
        else -> "Synced ${seconds / 86400}d ago"
    }
}

internal fun friendlyErrorMessage(raw: String?): String {
    if (raw == null) return "Sync failed"
    val lower = raw.lowercase()
    return when {
        "partial sync" in lower -> "Some data didn't sync"
        "timeout" in lower -> "Server took too long"
        "unauthorized" in lower || "401" in lower -> "Session expired, sign in again"
        "forbidden" in lower || "403" in lower -> "Permission denied"
        "not found" in lower || "404" in lower -> "Server not reachable"
        "500" in lower || "internal server" in lower -> "Server error"
        "socket" in lower || "connect" in lower || "network" in lower || "resolve" in lower -> "Connection problem"
        else -> "Sync failed"
    }
}
