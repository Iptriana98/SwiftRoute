package com.swiftroute.app.ui.components

import androidx.compose.foundation.background
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
import com.swiftroute.app.shared.model.Location
import com.swiftroute.app.shared.model.Stop

/**
 * Route map view - platform-specific implementation
 * Android: Uses MapLibre
 * iOS: Uses MapKit
 * Desktop: Placeholder
 */
@Composable
expect fun RouteMapView(
    stops: List<Stop>,
    startLocation: Location?,
    endLocation: Location?,
    currentLocation: Location?,
    onMapClick: ((Location) -> Unit)? = null,
    modifier: Modifier = Modifier
)

/**
 * Stop marker for map
 */
@Composable
fun StopMarker(
    stop: Stop,
    index: Int,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stopLabel = stop.label
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                if (stopLabel != null) {
                    Text(
                        text = stopLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = stop.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Route path line (placeholder)
 */
data class RoutePath(
    val points: List<Location>
)
