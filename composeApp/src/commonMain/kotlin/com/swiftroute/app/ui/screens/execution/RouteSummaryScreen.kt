package com.swiftroute.app.ui.screens.execution

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.RouteEntity
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.data.RouteSessionEntity
import kotlinx.coroutines.launch

/**
 * Route Summary screen
 * Shows route statistics and Start again / Back to home buttons
 */
class RouteSummaryScreen(
    private val userId: String,
    private val routeId: String,
    private val routeRepository: RouteRepository,
    private val onStartAgain: (String) -> Unit = {},
    private val onGoHome: () -> Unit = {}
) : Screen {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        
        var route by remember { mutableStateOf<RouteEntity?>(null) }
        var completedStops by remember { mutableIntStateOf(0) }
        var skippedStops by remember { mutableIntStateOf(0) }
        var totalStops by remember { mutableIntStateOf(0) }
        var totalMinutes by remember { mutableIntStateOf(0) }
        var isLoading by remember { mutableStateOf(true) }
        
        // Load route and session
        LaunchedEffect(routeId) {
            try {
                route = routeRepository.getRoute(routeId)
                val stops = routeRepository.getStops(routeId)
                totalStops = stops.size
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        val routeName = route?.name ?: "Route"
        val completionPercentage = if (totalStops > 0) {
            (completedStops.toFloat() / totalStops * 100).toInt()
        } else {
            100 // If we don't know total, assume 100%
        }

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Success icon
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Route Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = routeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Stats cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        value = "$totalMinutes",
                        label = "Minutes"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircle,
                        value = "$completedStops",
                        label = "Completed"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SkipNext,
                        value = "$skippedStops",
                        label = "Skipped"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Route Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (totalStops > 0) {
                            LinearProgressIndicator(
                                progress = { completedStops.toFloat() / totalStops },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$completedStops of $totalStops stops completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(completedStops.toFloat() / totalStops * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                text = "Route completed successfully!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Actions
                Button(
                    onClick = { onStartAgain(routeId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Again", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Home", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * Statistics card component
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
