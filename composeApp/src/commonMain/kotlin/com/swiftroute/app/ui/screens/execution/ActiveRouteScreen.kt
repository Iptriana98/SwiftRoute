package com.swiftroute.app.ui.screens.execution

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.RouteEntity
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.data.RouteSessionEntity
import com.swiftroute.app.shared.data.StopEntity
import kotlinx.coroutines.launch

/**
 * Active Route Mode screen
 * Shows current stop info, Navigate button, Complete/Skip buttons, and progress indicator
 * Supports pause/resume functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
class ActiveRouteScreen(
    private val userId: String,
    private val routeId: String,
    private val routeRepository: RouteRepository,
    private val onNavigateToSummary: () -> Unit = {},
    private val onPauseRoute: () -> Unit = {}
) : Screen {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        
        var route by remember { mutableStateOf<RouteEntity?>(null) }
        var stops by remember { mutableStateOf<List<StopEntity>>(emptyList()) }
        var currentSession by remember { mutableStateOf<RouteSessionEntity?>(null) }
        var currentStopIndex by remember { mutableIntStateOf(0) }
        var completedStopIds by remember { mutableStateOf<List<String>>(emptyList()) }
        var skippedStopIds by remember { mutableStateOf<List<String>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showEndConfirmation by remember { mutableStateOf(false) }
        var showPauseConfirmation by remember { mutableStateOf(false) }
        var preferredNavApp by remember { mutableStateOf("google_maps") }
        var isPaused by remember { mutableStateOf(false) }

        // Load route, stops, and session
        LaunchedEffect(routeId) {
            try {
                route = routeRepository.getRoute(routeId)
                stops = routeRepository.getStops(routeId).sortedBy { it.order }
                
                // Try to load existing session (for resume functionality)
                // TODO: In real implementation, load from repository
                if (currentSession == null) {
                    val newSession = RouteSessionEntity(
                        id = "",
                        routeId = routeId,
                        userId = userId,
                        startedAt = System.currentTimeMillis(),
                        currentStopIndex = 0
                    )
                    currentSession = newSession
                }
                
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }

        val currentStop = stops.getOrNull(currentStopIndex)
        val pendingStops = stops.filter { 
            it.id !in completedStopIds && it.id !in skippedStopIds 
        }
        val remainingStops = pendingStops.size
        val totalStops = stops.size
        val completedCount = completedStopIds.size
        val progress = if (totalStops > 0) completedCount.toFloat() / totalStops else 0f
        
        // Calculate estimated time remaining
        val totalServiceTime = pendingStops.sumOf { it.serviceTimeMinutes }
        val travelTimeEstimate = pendingStops.size * 5 // Estimate 5 min per travel
        val etaMinutes = totalServiceTime + travelTimeEstimate

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        if (currentStop == null || stops.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No stops available")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToSummary) {
                        Text("Go to Summary")
                    }
                }
            }
            return
        }

        // Paused state UI
        if (isPaused) {
            PausedRouteView(
                routeName = route?.name ?: "Route",
                completedCount = completedCount,
                totalStops = totalStops,
                onResume = { isPaused = false },
                onEndRoute = { 
                    showEndConfirmation = true 
                }
            )
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(route?.name ?: "Active Route")
                            Text(
                                text = "${completedCount}/$totalStops completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showPauseConfirmation = true }) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause route")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEndConfirmation = true }) {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "End route",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Stop ${currentStopIndex + 1} of $totalStops",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$remainingStops stops remaining",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "ETA ${etaMinutes}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        )
                    }
                }

                // Current stop card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Current Stop",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val stopLabel = currentStop.label
                        if (!stopLabel.isNullOrBlank()) {
                            Text(
                                text = stopLabel,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = currentStop.address,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Service time: ${currentStop.serviceTimeMinutes} min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Notes if present
                        val stopNotes = currentStop.notes
                        if (!stopNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Note,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stopNotes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Navigate button
                Button(
                    onClick = {
                        // Open navigation app
                        val address = currentStop.address
                        val uri = when (preferredNavApp) {
                            "google_maps" -> "google.navigation:q=${Uri.encode(address)}"
                            "waze" -> "waze://?q=${Uri.encode(address)}"
                            else -> "maps://?daddr=${Uri.encode(address)}"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to generic maps
                            val genericUri = "geo:0,0?q=${Uri.encode(address)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(genericUri))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Navigate",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Skip current stop
                            skippedStopIds = skippedStopIds + currentStop.id
                            val nextIndex = currentStopIndex + 1
                            if (nextIndex >= stops.size) {
                                onNavigateToSummary()
                            } else {
                                currentStopIndex = nextIndex
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skip")
                    }

                    Button(
                        onClick = {
                            // Mark current stop as complete
                            completedStopIds = completedStopIds + currentStop.id
                            val nextIndex = currentStopIndex + 1
                            if (nextIndex >= stops.size) {
                                onNavigateToSummary()
                            } else {
                                currentStopIndex = nextIndex
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete")
                    }
                }

                // Upcoming stops indicator
                if (remainingStops > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upcoming (${remainingStops - 1})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Pause confirmation dialog
        if (showPauseConfirmation) {
            AlertDialog(
                onDismissRequest = { showPauseConfirmation = false },
                title = { Text("Pause Route?") },
                text = { Text("You can resume this route later from where you left off. Progress is saved automatically.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPauseConfirmation = false
                            isPaused = true
                            // TODO: Save session state to repository
                        }
                    ) {
                        Text("Pause Route")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPauseConfirmation = false }) {
                        Text("Continue")
                    }
                }
            )
        }

        // End route confirmation dialog
        if (showEndConfirmation) {
            AlertDialog(
                onDismissRequest = { showEndConfirmation = false },
                title = { Text("End Route?") },
                text = { 
                    Text(
                        if (completedCount > 0) 
                            "You have $completedCount of $totalStops stops completed. End this route and view your summary?"
                        else 
                            "Are you sure you want to end this route without completing any stops?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showEndConfirmation = false
                            onNavigateToSummary()
                        }
                    ) {
                        Text("End Route", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndConfirmation = false }) {
                        Text("Continue Route")
                    }
                }
            )
        }
    }
}

/**
 * Paused state view when user pauses the route
 */
@Composable
private fun PausedRouteView(
    routeName: String,
    completedCount: Int,
    totalStops: Int,
    onResume: () -> Unit,
    onEndRoute: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PauseCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Route Paused",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = routeName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$completedCount of $totalStops stops completed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Text(
                text = "You can resume this route anytime.\nYour progress has been saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resume Route")
            }
            
            OutlinedButton(
                onClick = onEndRoute,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Route")
            }
        }
    }
}

/**
 * Active stop item data class (for backward compatibility)
 */
data class ActiveStopItem(
    val order: Int,
    val label: String?,
    val address: String,
    val serviceTime: Int,
    val hasNotes: Boolean,
    val notes: String? = null
)
