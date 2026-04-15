package com.swiftroute.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.RouteEntity
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.ui.EmptyState
import com.swiftroute.app.ui.ErrorState
import com.swiftroute.app.ui.LoadingIndicator
import com.swiftroute.app.ui.RouteCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Home screen - displays list of saved routes for a user
 * Loads routes from repository and supports CRUD operations
 */
class HomeScreen(
    private val userId: String,
    private val routeRepository: RouteRepository,
    private val onNavigateToCreateRoute: (String?) -> Unit = {},
    private val onNavigateToRouteOverview: (String) -> Unit = {}
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var routes by remember { mutableStateOf<List<RouteEntity>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var showCreateDialog by remember { mutableStateOf(false) }
        var routeToDelete by remember { mutableStateOf<RouteEntity?>(null) }
        var isDeleting by remember { mutableStateOf(false) }

        // Load routes from repository
        LaunchedEffect(userId) {
            try {
                routeRepository.getRoutes(userId).collectLatest { loadedRoutes ->
                    routes = loadedRoutes
                    isLoading = false
                    error = null
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load routes"
                isLoading = false
            }
        }

        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    isLoading -> {
                        LoadingIndicator(message = "Loading routes...")
                    }
                    error != null -> {
                        ErrorState(
                            message = error!!,
                            onRetry = {
                                isLoading = true
                                error = null
                                // Trigger reload
                                scope.launch {
                                    try {
                                        routeRepository.getRoutes(userId).collectLatest { loadedRoutes ->
                                            routes = loadedRoutes
                                            isLoading = false
                                        }
                                    } catch (e: Exception) {
                                        error = e.message ?: "Failed to load routes"
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                    routes.isEmpty() -> {
                        // Empty state
                        EmptyState(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            },
                            title = "No Routes Yet",
                            description = "Create your first route to start planning your journey",
                            actionLabel = "Create Route",
                            onAction = { showCreateDialog = true }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Routes",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledTonalButton(
                                        onClick = { showCreateDialog = true }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("New")
                                    }
                                }
                            }
                            items(routes, key = { it.id }) { route ->
                                RouteCard(
                                    name = route.name,
                                    stopCount = 0, // Will be updated with actual count
                                    lastUsed = formatLastUsed(route.updatedAt),
                                    onClick = { onNavigateToRouteOverview(route.id) },
                                    onEdit = { onNavigateToCreateRoute(route.id) },
                                    onDelete = { routeToDelete = route }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        // Create Route Dialog
        if (showCreateDialog) {
            CreateRouteDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    scope.launch {
                        try {
                            val newRoute = RouteEntity(
                                id = "",
                                userId = userId,
                                name = name,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            val routeId = routeRepository.createRoute(newRoute)
                            showCreateDialog = false
                            // Navigate to create/edit the new route
                            onNavigateToCreateRoute(routeId)
                        } catch (e: Exception) {
                            // Handle error
                            showCreateDialog = false
                        }
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        routeToDelete?.let { route ->
            AlertDialog(
                onDismissRequest = { routeToDelete = null },
                title = { Text("Delete Route") },
                text = { Text("Are you sure you want to delete \"${route.name}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isDeleting = true
                            scope.launch {
                                try {
                                    routeRepository.deleteRoute(route.id)
                                    routeToDelete = null
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
                    TextButton(onClick = { routeToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    private fun formatLastUsed(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> "${days / 7}w ago"
        }
    }
}

/**
 * Dialog for creating a new route
 */
@Composable
fun CreateRouteDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var routeName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Route") },
        text = {
            OutlinedTextField(
                value = routeName,
                onValueChange = { routeName = it },
                label = { Text("Route Name") },
                placeholder = { Text("e.g., Monday Errands") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(routeName) },
                enabled = routeName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
