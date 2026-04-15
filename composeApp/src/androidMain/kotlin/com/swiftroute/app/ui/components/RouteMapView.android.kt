package com.swiftroute.app.ui.components

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.geojson
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.MapView
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.Style
import com.mapbox.maps.layerapis.LineLayerApi
import com.mapbox.maps.satellite.SatelliteStyle
import com.swiftroute.app.ApiKeys
import com.swiftroute.app.shared.model.Location
import com.swiftroute.app.shared.model.Stop

/**
 * Route map view for Android using Mapbox
 * Shows numbered markers for each stop and route line connecting them
 * 
 * Setup:
 * 1. Get a free Mapbox access token at https://account.mapbox.com/
 * 2. Add MAPBOX_ACCESS_TOKEN to your .env file (see .env.example)
 */
@Composable
actual fun RouteMapView(
    stops: List<Stop>,
    startLocation: Location?,
    endLocation: Location?,
    currentLocation: Location?,
    onMapClick: ((Location) -> Unit)?,
    modifier: Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    
    // Token from ApiKeys (loaded from .env file)
    val accessToken = ApiKeys.mapbox

    // Fallback UI when no token
    if (accessToken.isEmpty()) {
        MapPlaceholderView(stops = stops, modifier = modifier)
        return
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx, MapInitOptions(ctx)).also { mv ->
                    mapView = mv
                    
                    // Get MapboxMap instance via property
                    val mboxMap: MapboxMap = mv.mapboxMap
                    mapboxMap = mboxMap
                    
                    // Load style
                    mboxMap.loadStyle(Style.MAPBAR_STREETS) { style ->
                        // Center and zoom on stops if available
                        if (stops.isNotEmpty()) {
                            val firstStop = stops.first()
                            mboxMap.setCamera(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(firstStop.location.lng, firstStop.location.lat))
                                    .zoom(12.0)
                                    .build()
                            )
                        }
                        
                        // Add route line layer
                        updateRouteLine(mv, stops, startLocation, endLocation)
                    }
                }
            },
            update = { view ->
                // Update route line when stops change
                updateRouteLine(view, stops, startLocation, endLocation)
                
                // Update camera when stops change
                if (stops.isNotEmpty()) {
                    mapboxMap?.let { mboxMap ->
                        val firstStop = stops.first()
                        mboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(firstStop.location.lng, firstStop.location.lat))
                                .zoom(12.0)
                                .build()
                        )
                    }
                }
            }
        )

        // Zoom controls overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    mapboxMap?.let { mboxMap ->
                        val currentZoom = mboxMap.cameraState.zoom
                        mboxMap.setCamera(
                            CameraOptions.Builder()
                                .zoom(currentZoom + 1.0)
                                .build()
                        )
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FloatingActionButton(
                onClick = {
                    mapboxMap?.let { mboxMap ->
                        val currentZoom = mboxMap.cameraState.zoom
                        mboxMap.setCamera(
                            CameraOptions.Builder()
                                .zoom(currentZoom - 1.0)
                                .build()
                        )
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FloatingActionButton(
                onClick = {
                    // Center on user location
                    currentLocation?.let { loc ->
                        mapboxMap?.setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(loc.lng, loc.lat))
                                .zoom(15.0)
                                .build()
                        )
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My location")
            }
        }
        
        // Stops count overlay
        if (stops.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "${stops.size} stops",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Update the route line on the map to connect all stops
 */
private fun updateRouteLine(
    mapView: MapView,
    stops: List<Stop>,
    startLocation: Location?,
    endLocation: Location?
) {
    if (stops.isEmpty()) return
    
    try {
        val style = mapView.mapboxMap.style ?: return
        
        // Build list of points: start -> stops -> end
        val points = mutableListOf<Point>()
        
        // Add start location if provided
        startLocation?.let {
            points.add(Point.fromLngLat(it.lng, it.lat))
        }
        
        // Add all stops in order
        stops.forEach { stop ->
            points.add(Point.fromLngLat(stop.location.lng, stop.location.lat))
        }
        
        // Add end location if provided
        endLocation?.let {
            points.add(Point.fromLngLat(it.lng, it.lat))
        }
        
        if (points.size < 2) return
        
        // Create line feature
        val lineString = LineString.fromLngLats(points)
        val feature = Feature.fromGeometry(lineString)
        val featureCollection = FeatureCollection.fromFeature(feature)
        
        // Add or update source and layer
        val sourceId = "route_line_source"
        val layerId = "route_line_layer"
        
        if (style.styleSourceExists(sourceId)) {
            style.updateSourceGeoJSON(sourceId, featureCollection)
        } else {
            style.addSource(com.mapbox.maps.source.GeoJsonSource(sourceId, featureCollection))
            style.addLayerBelow(
                com.mapbox.maps.layer.LineLayer(layerId, sourceId).apply {
                    lineColor(android.graphics.Color.parseColor("#3B82F6")) // Blue color
                    lineWidth(4.0)
                    lineOpacity(0.8)
                },
                "settlement-major-label"
            )
        }
    } catch (e: Exception) {
        // Silently handle map errors
    }
}

/**
 * Placeholder view when Mapbox is not configured
 */
@Composable
private fun MapPlaceholderView(
    stops: List<Stop>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Map Configuration Required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "To enable maps:\n1. Get a free token at mapbox.com\n2. Add MAPBOX_ACCESS_TOKEN to .env file",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (stops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${stops.size} stops on route",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
