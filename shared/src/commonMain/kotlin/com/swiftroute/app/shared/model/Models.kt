package com.swiftroute.app.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val preferredNavApp: String = "google_maps",
    val defaultServiceTimeMinutes: Int = 15,
    val createdAt: Long = 0
)

@Serializable
data class Route(
    val id: String,
    val userId: String,
    val name: String,
    val startLocation: Location,
    val endLocation: Location? = null,
    val estimatedTotalMinutes: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class Stop(
    val id: String,
    val routeId: String,
    val address: String,
    val label: String? = null,
    val location: Location,
    val order: Int = 0,
    val status: StopStatus = StopStatus.PENDING,
    val serviceTimeMinutes: Int = 15,
    val notes: String? = null
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

enum class StopStatus {
    PENDING, COMPLETED, SKIPPED
}

@Serializable
data class RouteSession(
    val id: String,
    val routeId: String,
    val userId: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val completedStopIds: List<String> = emptyList(),
    val skippedStopIds: List<String> = emptyList(),
    val currentStopIndex: Int = 0,
    val totalDurationMinutes: Int? = null,
    val endedManually: Boolean = false
)
