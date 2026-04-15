package com.swiftroute.app.ui.screens.optimization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.swiftroute.app.shared.data.RouteEntity
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.data.StopEntity
import com.swiftroute.app.shared.model.Location
import com.swiftroute.app.shared.optimizer.OptimizationStrategy
import com.swiftroute.app.shared.optimizer.RouteOptimizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Optimization Setup screen
 * Configures and runs route optimization with strategy selection
 */
@OptIn(ExperimentalMaterial3Api::class)
class OptimizationScreen(
    private val routeId: String,
    private val routeRepository: RouteRepository,
    private val onNavigateBack: () -> Unit = {},
    private val onOptimizationComplete: () -> Unit = {}
) : Screen {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val routeOptimizer = remember { RouteOptimizer() }
        
        var route by remember { mutableStateOf<RouteEntity?>(null) }
        var stops by remember { mutableStateOf<List<StopEntity>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var isOptimizing by remember { mutableStateOf(false) }
        var optimizationResult by remember { mutableStateOf<OptimizationResult?>(null) }
        
        // Optimization settings
        var selectedStrategy by remember { mutableIntStateOf(0) } // 0 = Distance, 1 = Time
        var startLocationType by remember { mutableStateOf(LocationType.CURRENT_LOCATION) }
        var startCustomAddress by remember { mutableStateOf("") }
        var endLocationType by remember { mutableStateOf(LocationType.SAME_AS_START) }
        var endCustomAddress by remember { mutableStateOf("") }
        
        // Mock locations for demo (in real app, geocode addresses or use GPS)
        val mockCurrentLocation = remember { Location(lat = 40.7128, lng = -74.0060) } // NYC
        val mockWarehouse = remember { Location(lat = 40.7200, lng = -74.0100) }
        
        // Load route and stops
        LaunchedEffect(routeId) {
            try {
                route = routeRepository.getRoute(routeId)
                stops = routeRepository.getStops(routeId)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }

        val stopCount = stops.size
        val strategy = if (selectedStrategy == 0) OptimizationStrategy.MINIMIZE_DISTANCE 
                       else OptimizationStrategy.MINIMIZE_TIME

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
                    title = { Text("Route Optimization") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stops info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Stops to optimize",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$stopCount stops",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Optimization Strategy Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Optimization Goal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = selectedStrategy == 0,
                                onClick = { selectedStrategy = 0 },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                icon = { Icon(Icons.Default.Route, null, modifier = Modifier.size(18.dp)) }
                            ) {
                                Text("Shortest Distance")
                            }
                            SegmentedButton(
                                selected = selectedStrategy == 1,
                                onClick = { selectedStrategy = 1 },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                icon = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp)) }
                            ) {
                                Text("Fastest Time")
                            }
                        }
                        
                        Text(
                            text = if (selectedStrategy == 0) 
                                "Optimizes route to minimize total travel distance" 
                            else 
                                "Optimizes route to minimize total travel time (40 km/h avg)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Starting Point
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Starting Point",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        LocationSelector(
                            selectedType = startLocationType,
                            onTypeSelected = { startLocationType = it },
                            customAddress = startCustomAddress,
                            onCustomAddressChange = { startCustomAddress = it }
                        )
                    }
                }
                
                // Ending Point
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ending Point",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        LocationSelector(
                            selectedType = endLocationType,
                            onTypeSelected = { endLocationType = it },
                            customAddress = endCustomAddress,
                            onCustomAddressChange = { endCustomAddress = it }
                        )
                    }
                }

                // Results
                if (optimizationResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Est. Time",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = "${optimizationResult!!.estimatedMinutes} min",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Route,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Distance",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = "${String.format("%.1f", optimizationResult!!.distanceKm)} km",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            if (optimizationResult!!.savedPercent > 0) {
                                Text(
                                    text = "Saved ${optimizationResult!!.savedPercent}% vs original order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isOptimizing = true
                        scope.launch {
                            try {
                                // Determine start and end locations
                                val startLoc = when (startLocationType) {
                                    LocationType.CURRENT_LOCATION -> mockCurrentLocation
                                    LocationType.CUSTOM_ADDRESS -> {
                                        // In real app, geocode this address
                                        mockWarehouse
                                    }
                                    LocationType.SAME_AS_START -> mockWarehouse
                                }
                                
                                val endLoc = when (endLocationType) {
                                    LocationType.CURRENT_LOCATION -> mockCurrentLocation
                                    LocationType.CUSTOM_ADDRESS -> {
                                        // In real app, geocode this address
                                        mockWarehouse
                                    }
                                    LocationType.SAME_AS_START -> startLoc
                                }
                                
                                // Convert StopEntity to Stop for optimizer
                                val stopModels = stops.map { entity ->
                                    com.swiftroute.app.shared.model.Stop(
                                        id = entity.id,
                                        routeId = entity.routeId,
                                        address = entity.address,
                                        label = entity.label,
                                        location = Location(entity.latitude, entity.longitude),
                                        order = entity.order,
                                        serviceTimeMinutes = entity.serviceTimeMinutes,
                                        notes = entity.notes
                                    )
                                }
                                
                                // Run optimization
                                val result = withContext(Dispatchers.Default) {
                                    // Calculate original cost
                                    val originalCost = routeOptimizer.calculateRouteCost(
                                        startLoc, stopModels, endLoc, strategy
                                    )
                                    
                                    // Optimize
                                    val optimizedStops = routeOptimizer.optimize(
                                        start = startLoc,
                                        stops = stopModels,
                                        end = endLoc,
                                        strategy = strategy
                                    )
                                    
                                    // Calculate optimized cost
                                    val optimizedCost = routeOptimizer.calculateRouteCost(
                                        startLoc, optimizedStops, endLoc, strategy
                                    )
                                    
                                    // Calculate metrics
                                    val distanceKm = routeOptimizer.calculateRouteCost(
                                        startLoc, optimizedStops, endLoc, 
                                        OptimizationStrategy.MINIMIZE_DISTANCE
                                    )
                                    
                                    // Time estimate: travel time + service time
                                    val serviceTime = stopModels.sumOf { it.serviceTimeMinutes }
                                    val travelTimeMin = optimizedCost.toInt()
                                    val totalTime = travelTimeMin + serviceTime
                                    
                                    // Savings percentage
                                    val savedPercent = if (originalCost > 0) {
                                        ((originalCost - optimizedCost) / originalCost * 100).toInt()
                                    } else 0
                                    
                                    Triple(optimizedStops, OptimizationMetrics(
                                        estimatedMinutes = totalTime,
                                        distanceKm = distanceKm,
                                        savedPercent = savedPercent
                                    ), distanceKm)
                                }
                                
                                // Update stop orders
                                result.first.forEachIndexed { index, stop ->
                                    val updatedStop = stops.first { it.id == stop.id }.copy(order = index)
                                    routeRepository.updateStop(updatedStop)
                                }
                                
                                optimizationResult = result.second
                                
                                // Refresh stops
                                stops = routeRepository.getStops(routeId)
                            } catch (e: Exception) {
                                // Handle error
                            } finally {
                                isOptimizing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isOptimizing && stopCount >= 2,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isOptimizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizing...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (optimizationResult != null) "Re-optimize" else "Optimize Route", 
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                    }
                }

                if (stopCount < 2) {
                    Text(
                        text = "Add at least 2 stops to optimize",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (optimizationResult != null) {
                    OutlinedButton(
                        onClick = onOptimizationComplete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Done")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Location type selection
 */
enum class LocationType {
    CURRENT_LOCATION,
    CUSTOM_ADDRESS,
    SAME_AS_START
}

/**
 * Location selector component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelector(
    selectedType: LocationType,
    onTypeSelected: (LocationType) -> Unit,
    customAddress: String,
    onCustomAddressChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LocationType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        when (type) {
                            LocationType.CURRENT_LOCATION -> "📍 Current Location"
                            LocationType.CUSTOM_ADDRESS -> "🏠 Custom Address"
                            LocationType.SAME_AS_START -> "↩️ Same as Start"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (selectedType == LocationType.CUSTOM_ADDRESS) {
            OutlinedTextField(
                value = customAddress,
                onValueChange = onCustomAddressChange,
                placeholder = { Text("Enter address...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/**
 * Optimization result
 */
data class OptimizationResult(
    val estimatedMinutes: Int,
    val distanceKm: Double,
    val savedPercent: Int
)

/**
 * Internal metrics for optimization
 */
private data class OptimizationMetrics(
    val estimatedMinutes: Int,
    val distanceKm: Double,
    val savedPercent: Int
)
