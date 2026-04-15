package com.swiftroute.app.ui.screens.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.RouteEntity
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.data.StopEntity
import kotlinx.coroutines.launch

/**
 * Route Overview screen with map and stop list
 * Shows map with route stops, stop list, and Start Route button
 */
class RouteOverviewScreen(
    private val userId: String,
    private val routeId: String,
    private val routeRepository: RouteRepository,
    private val onNavigateBack: () -> Unit = {},
    private val onStartRoute: (String) -> Unit = {},
    private val onEditRoute: (String) -> Unit = {}
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var route by remember { mutableStateOf<RouteEntity?>(null) }
        var stops by remember { mutableStateOf<List<StopEntity>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        // Load route and stops
        LaunchedEffect(routeId) {
            try {
                route = routeRepository.getRoute(routeId)
                stops = routeRepository.getStops(routeId)
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Failed to load route"
                isLoading = false
            }
        }

        // Calculate totals
        val totalServiceTime = stops.sumOf { it.serviceTimeMinutes }
        val estimatedTotalMinutes = totalServiceTime + (stops.size * 5) // Add travel time estimate
        val totalStops = stops.size

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = error!!)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(route?.name ?: "Route Overview") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditRoute(routeId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Map placeholder (will be replaced with actual MapLibre)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // TODO: Replace with actual RouteMapView
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${stops.size} stops on this route",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (stops.isNotEmpty()) {
                            Text(
                                text = stops.firstOrNull()?.address ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Map controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { /* TODO: Zoom in */ },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom in")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingActionButton(
                            onClick = { /* TODO: Zoom out */ },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom out")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingActionButton(
                            onClick = { /* TODO: Center on route */ },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "My location")
                        }
                    }
                }

                // Route summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$totalStops",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${estimatedTotalMinutes}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Est. Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${totalServiceTime}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Service",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Stop list
                Text(
                    text = "Stop Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (stops.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No stops added yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(stops.sortedBy { it.order }) { index, stop ->
                            OverviewStopCard(
                                stop = stop,
                                order = index + 1,
                                isFirst = index == 0,
                                isLast = index == stops.lastIndex
                            )
                        }
                    }
                }

                // Start route button
                Button(
                    onClick = { onStartRoute(routeId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = stops.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Route", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
        }
    }
}

/**
 * Card for overview stop item
 */
@Composable
fun OverviewStopCard(
    stop: StopEntity,
    order: Int,
    isFirst: Boolean,
    isLast: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Order number
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFirst) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = order.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isFirst) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stop info
            Column(modifier = Modifier.weight(1f)) {
                val stopLabel = stop.label
                if (!stopLabel.isNullOrBlank()) {
                    Text(
                        text = stopLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = stop.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Icons
            Row {
                val stopNotes = stop.notes
                if (!stopNotes.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Note,
                        contentDescription = "Has notes",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stop.serviceTimeMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
