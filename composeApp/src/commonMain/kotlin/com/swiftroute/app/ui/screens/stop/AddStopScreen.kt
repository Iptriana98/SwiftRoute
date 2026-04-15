package com.swiftroute.app.ui.screens.stop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.data.StopEntity
import kotlinx.coroutines.launch

/**
 * Add/Edit Stop screen
 * Form fields: address, label, service time, notes
 * Saves stop to repository and navigates back after saving
 */
class AddStopScreen(
    private val routeId: String,
    private val stopId: String? = null,
    private val routeRepository: RouteRepository,
    private val onNavigateBack: () -> Unit = {}
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var address by remember { mutableStateOf("") }
        var label by remember { mutableStateOf("") }
        var serviceTime by remember { mutableStateOf("15") }
        var notes by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(stopId != null) }
        var isSaving by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }
        
        // Form validation
        val isValid = address.isNotBlank()

        // Load existing stop if editing
        LaunchedEffect(stopId) {
            if (stopId != null) {
                try {
                    val stops = routeRepository.getStops(routeId)
                    val stop = stops.find { it.id == stopId }
                    if (stop != null) {
                        address = stop.address
                        label = stop.label ?: ""
                        serviceTime = stop.serviceTimeMinutes.toString()
                        notes = stop.notes ?: ""
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    isLoading = false
                }
            } else {
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (stopId == null) "Add Stop" else "Edit Stop") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        if (stopId != null) {
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val stop = StopEntity(
                                            id = stopId ?: "",
                                            routeId = routeId,
                                            address = address.trim(),
                                            label = label.trim().ifBlank { null },
                                            lat = 0.0, // TODO: Geocode address
                                            lng = 0.0, // TODO: Geocode address
                                            order = 0, // TODO: Set proper order
                                            serviceTimeMinutes = serviceTime.toIntOrNull() ?: 15,
                                            notes = notes.trim().ifBlank { null }
                                        )
                                        
                                        if (stopId != null) {
                                            routeRepository.updateStop(stop)
                                        } else {
                                            routeRepository.addStop(stop)
                                        }
                                        onNavigateBack()
                                    } catch (e: Exception) {
                                        // Handle error
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = isValid && !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (stopId == null) "Add" else "Save")
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Address field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    placeholder = { Text("Enter address or location") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Label field
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    placeholder = { Text("e.g., John's House, Office") },
                    leadingIcon = {
                        Icon(Icons.Default.Label, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Service time field
                OutlinedTextField(
                    value = serviceTime,
                    onValueChange = { if (it.all { char -> char.isDigit() }) serviceTime = it },
                    label = { Text("Service Time (minutes)") },
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    },
                    suffix = { Text("min") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider()

                // Notes section
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Saved permanently for this address",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Gate code, parking instructions, contact info...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text("Saved permanently for this address — visible on every visit")
                    }
                )

                // Quick note suggestions
                Text(
                    text = "Quick add:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { 
                            notes += if (notes.isNotEmpty()) "\n" else "" + "Gate code: " 
                        },
                        label = { Text("Gate code") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = { 
                            notes += if (notes.isNotEmpty()) "\n" else "" + "Ring bell" 
                        },
                        label = { Text("Ring bell") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = { 
                            notes += if (notes.isNotEmpty()) "\n" else "" + "Park on side" 
                        },
                        label = { Text("Park") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalParking,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Stop") },
                text = { Text("Are you sure you want to delete this stop?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isDeleting = true
                            scope.launch {
                                try {
                                    routeRepository.deleteStop(stopId!!)
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
