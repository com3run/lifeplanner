package az.tribe.lifeplanner.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.AiProvider
import az.tribe.lifeplanner.domain.model.AiUsageStats
import az.tribe.lifeplanner.domain.repository.AiUsageRepository
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
internal fun AiProviderDialog(
    currentProvider: AiProvider,
    isGuest: Boolean,
    userLevel: Int = 1,
    onProviderSelected: (AiProvider) -> Unit,
    onDismiss: () -> Unit
) {
    val aiUsageRepository: AiUsageRepository = koinInject()
    var usageStats by remember { mutableStateOf<AiUsageStats?>(null) }
    var isLoadingStats by remember { mutableStateOf(!isGuest) }

    LaunchedEffect(Unit) {
        if (!isGuest) {
            usageStats = aiUsageRepository.getMonthlyStats()
            isLoadingStats = false
        }
    }

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val monthName = now.month.name.lowercase().replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Provider") },
        text = {
            Column {
                when {
                    isGuest -> {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                            Text("Sign in to track AI usage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp))
                        }
                    }
                    isLoadingStats -> {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                                Text("Loading usage stats...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    else -> {
                        usageStats?.let { stats ->
                            Text("$monthName ${now.year} Usage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    UsageStatItem(value = "${stats.totalRequests}", label = "Requests")
                                    UsageStatItem(value = formatTokenCount(stats.totalTokens), label = "Tokens")
                                    UsageStatItem(value = formatAiCost(stats.estimatedCostUsd), label = "Cost")
                                }
                            }
                            if (stats.byProvider.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                stats.byProvider.forEach { summary ->
                                    val providerName = AiProvider.fromProviderName(summary.provider)?.displayName ?: summary.provider
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(providerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text("${summary.requestCount} req  ${formatAiCost(summary.estimatedCostUsd)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text("Choose provider:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))

                AiProvider.entries.forEach { provider ->
                    val isUnlocked = provider.isUnlocked(userLevel)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .then(if (isUnlocked) Modifier.clickable { onProviderSelected(provider) } else Modifier)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = provider == currentProvider, onClick = { if (isUnlocked) onProviderSelected(provider) }, enabled = isUnlocked)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    provider.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                if (!isUnlocked) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text("Lv. ${provider.requiredLevel}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                if (isUnlocked) provider.modelInfo else "Reach level ${provider.requiredLevel} to unlock",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
internal fun UsageStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun formatTokenCount(tokens: Long): String = when {
    tokens >= 1_000_000 -> {
        val value = tokens / 1_000_000.0
        val rounded = (value * 10).toLong() / 10.0
        "${rounded}M"
    }
    tokens >= 1_000 -> {
        val value = tokens / 1_000.0
        val rounded = (value * 10).toLong() / 10.0
        "${rounded}K"
    }
    else -> "$tokens"
}

internal fun formatAiCost(cost: Double): String = when {
    cost < 0.005 -> if (cost == 0.0) "$0.00" else "<$0.01"
    else -> {
        val cents = (cost * 100).toLong()
        val dollars = cents / 100
        val remainder = cents % 100
        "$${dollars}.${remainder.toString().padStart(2, '0')}"
    }
}
