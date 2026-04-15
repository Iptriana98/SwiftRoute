package com.swiftroute.app.ui.screens.route

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.ContentCopy
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
import com.swiftroute.app.shared.data.StopEntity
import kotlinx.coroutines.launch

/**
 * Create/Edit Route screen
 */
class CreateRouteScreen(
    private val userId: String,
    private val routeId: String? = null,
    private val routeRepository: RouteRepository,
    private val onNavigateBack: () -> Unit = {},
    private val onNavigateToAddStop: (routeId: String, stopId: String?) -> Unit = { _, _ -> },
    private val onNavigateToOptimization: (routeId: String) -> Unit = {}
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var routeName by remember { mutableStateOf("") }
        var stops by remember { mutableStateOf<List<StopItem>>(emptyList()) }
        var showMenu by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(routeId != null) }
        var isSaving by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var currentRouteId by remember { mutableStateOf(routeId ?: "") }

        // Load existing route if editing
        LaunchedEffect(routeId) {
            if (routeId != null) {
                try {
                    val route = routeRepository.getRoute(routeId)
                    if (route != null) {
                        routeName = route.name
                        currentRouteId = route.id
                    }
                    // Load stops
                    val loadedStops = routeRepository.getStops(routeId)
                    stops = loadedStops.map { stop ->
                        StopItem(
                            id = stop.id,
                            address = stop.address,
                            label = stop.label,
                            serviceTime = stop.serviceTimeMinutes,
                            hasNotes = !stop.notes.isNullOrBlank()
                        )
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    isLoading = false
                }
            }
        }

        // Save route on name change
        LaunchedEffect(routeName) {
            if (routeId != null && routeName.isNotBlank()) {
                try {
                    val route = RouteEntity(
                        id = routeId,
                        userId = userId,
                        name = routeName
                    )
                    routeRepository.updateRoute(route)
                } catch (e: Exception) {
                    // Handle error silently
                }
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (routeId == null) "Create Route" else "Edit Route")
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (routeId != null) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplicate") },
                                    onClick = { showMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmation = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
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
                // Route name input
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name") },
                    placeholder = { Text("e.g., Monday Clients") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider()

                // Stops section header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stops (${stops.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (stops.size >= 2) {
                        TextButton(
                            onClick = { onNavigateToOptimization(currentRouteId) },
                            enabled = currentRouteId.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Optimize")
                        }
                    }
                }

                if (stops.isEmpty()) {
                    // Empty state for stops
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No stops added yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the button below to add your first stop",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Stops list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stops, key = { it.id }) { stop ->
                            StopCard(
                                stop = stop,
                                onEdit = { 
                                    onNavigateToAddStop(currentRouteId, stop.id) 
                                },
                                onDelete = {
                                    scope.launch {
                                        try {
                                            routeRepository.deleteStop(stop.id)
                                            stops = stops.filter { s -> s.id != stop.id }
                                        } catch (e: Exception) {
                                            // Handle error
                                        }
                                    }
                                },
                                onMoveUp = { /* TODO: Reorder */ },
                                onMoveDown = { /* TODO: Reorder */ }
                            )
                        }
                    }
                }

                // Bottom action bar
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Stop button (always visible when editing)
                    if (currentRouteId.isNotEmpty()) {
                        Button(
                            onClick = { onNavigateToAddStop(currentRouteId, null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Stop")
                        }
                    }
                    
                    // Route actions (when stops exist)
                    if (stops.isNotEmpty() && currentRouteId.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToOptimization(currentRouteId) },
                                modifier = Modifier.weight(1f),
                                enabled = stops.size >= 2
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Optimize")
                            }
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Route") },
                text = { Text("Are you sure you want to delete this route? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isDeleting = true
                            scope.launch {
                                try {
                                    routeRepository.deleteRoute(currentRouteId)
                                    showDeleteConfirmation = false
                                    onNavigateBack()
                                } catch (e: Exception) {
                                    // Handle error
                                } finally {
                                    isDeleting = false
                                }
                            }
                        },
                        enabled = !isDeleting
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Stop item data class
 */
data class StopItem(
    val id: String,
    val address: String,
    val label: String?,
    val serviceTime: Int,
    val hasNotes: Boolean = false
)

/**
 * Card for displaying a stop
 */
@Composable
fun StopCard(
    stop: StopItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle / order indicator
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Stop info
            Column(modifier = Modifier.weight(1f)) {
                if (stop.label != null) {
                    Text(
                        text = stop.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = stop.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stop.serviceTime} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (stop.hasNotes) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Note,
                            contentDescription = "Has notes",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Actions
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
