package com.swiftroute.app.shared.optimizer

import com.swiftroute.app.shared.model.Location
import com.swiftroute.app.shared.model.Stop
import kotlin.math.*

/**
 * Strategy for route optimization
 */
enum class OptimizationStrategy {
    /** Minimize total travel distance */
    MINIMIZE_DISTANCE,
    
    /** Minimize total travel time (assumes average speed) */
    MINIMIZE_TIME
}

/**
 * SwiftRoute Optimizer — Phase 1: On-device TSP Solver.
 * 
 * Supports two optimization strategies:
 * - MINIMIZE_DISTANCE: Finds shortest path by distance
 * - MINIMIZE_TIME: Finds fastest path considering typical travel speeds
 */
class RouteOptimizer {
    
    companion object {
        /** Average speed in km/h for time estimation */
        const val AVERAGE_SPEED_KMH = 40.0
        
        /** Typical distance factor for urban travel (actual vs straight-line) */
        const val URBAN_DISTANCE_FACTOR = 1.4
    }

    /**
     * Optimizes a list of stops between a start and end location.
     * 
     * @param start Starting location (e.g., current position or warehouse)
     * @param stops List of stops to visit
     * @param end Optional ending location (e.g., return to base or different destination)
     * @param strategy Optimization strategy (distance or time)
     * @return Optimized list of stops
     */
    fun optimize(
        start: Location,
        stops: List<Stop>,
        end: Location? = null,
        strategy: OptimizationStrategy = OptimizationStrategy.MINIMIZE_DISTANCE
    ): List<Stop> {
        if (stops.size <= 1) return stops

        // Phase 1: Nearest Neighbor (Greedy)
        val greedyPath = nearestNeighbor(start, stops, end, strategy)

        // Phase 2: 2-opt (Improvement)
        val optimizedPath = twoOpt(start, greedyPath, end, strategy)

        return optimizedPath
    }

    /**
     * Calculate total route cost (distance or time) for a given path.
     */
    fun calculateRouteCost(
        start: Location,
        stops: List<Stop>,
        end: Location?,
        strategy: OptimizationStrategy
    ): Double {
        if (stops.isEmpty()) return 0.0
        
        var cost = 0.0
        var currentPos = start
        
        for (stop in stops) {
            val legCost = when (strategy) {
                OptimizationStrategy.MINIMIZE_DISTANCE -> 
                    calculateDistance(currentPos, stop.location)
                OptimizationStrategy.MINIMIZE_TIME -> 
                    calculateTravelTime(currentPos, stop.location)
            }
            cost += legCost
            currentPos = stop.location
        }
        
        // Add cost to end location
        if (end != null) {
            cost += when (strategy) {
                OptimizationStrategy.MINIMIZE_DISTANCE -> 
                    calculateDistance(currentPos, end)
                OptimizationStrategy.MINIMIZE_TIME -> 
                    calculateTravelTime(currentPos, end)
            }
        }
        
        return cost
    }

    private fun nearestNeighbor(
        start: Location,
        stops: List<Stop>,
        end: Location?,
        strategy: OptimizationStrategy
    ): MutableList<Stop> {
        val unvisited = stops.toMutableList()
        val path = mutableListOf<Stop>()
        var currentPos = start

        while (unvisited.isNotEmpty()) {
            val nextStop = unvisited.minByOrNull { stop ->
                when (strategy) {
                    OptimizationStrategy.MINIMIZE_DISTANCE -> 
                        calculateDistance(currentPos, stop.location)
                    OptimizationStrategy.MINIMIZE_TIME -> 
                        calculateTravelTime(currentPos, stop.location)
                }
            }!!
            path.add(nextStop)
            unvisited.remove(nextStop)
            currentPos = nextStop.location
        }

        return path
    }

    private fun twoOpt(
        start: Location,
        stops: List<Stop>,
        end: Location?,
        strategy: OptimizationStrategy
    ): MutableList<Stop> {
        val result = stops.toMutableList()
        var improved = true
        
        // Safety cap for iterations
        var iterations = 0
        val maxIterations = 500

        while (improved && iterations < maxIterations) {
            improved = false
            iterations++

            for (i in 0 until result.size - 1) {
                for (j in i + 1 until result.size) {
                    val currentDist = calculatePathSegmentDist(start, result, end, i, j, strategy)
                    val swappedDist = calculatePathSegmentDistSwapped(start, result, end, i, j, strategy)

                    if (swappedDist < currentDist) {
                        reverseSegment(result, i, j)
                        improved = true
                    }
                }
            }
        }
        return result
    }

    private fun calculatePathSegmentDist(
        start: Location,
        path: List<Stop>,
        end: Location?,
        i: Int,
        j: Int,
        strategy: OptimizationStrategy
    ): Double {
        val prev = if (i == 0) start else path[i - 1].location
        val first = path[i].location
        val last = path[j].location
        val next = if (j == path.size - 1) end else path[j + 1]?.location

        val costFn: (Location, Location) -> Double = when (strategy) {
            OptimizationStrategy.MINIMIZE_DISTANCE -> { a, b -> calculateDistance(a, b) }
            OptimizationStrategy.MINIMIZE_TIME -> { a, b -> calculateTravelTime(a, b) }
        }

        return costFn(prev, first) + costFn(last, next ?: last)
    }

    private fun calculatePathSegmentDistSwapped(
        start: Location,
        path: List<Stop>,
        end: Location?,
        i: Int,
        j: Int,
        strategy: OptimizationStrategy
    ): Double {
        val prev = if (i == 0) start else path[i - 1].location
        val first = path[i].location
        val last = path[j].location
        val next = if (j == path.size - 1) end else path[j + 1]?.location

        val costFn: (Location, Location) -> Double = when (strategy) {
            OptimizationStrategy.MINIMIZE_DISTANCE -> { a, b -> calculateDistance(a, b) }
            OptimizationStrategy.MINIMIZE_TIME -> { a, b -> calculateTravelTime(a, b) }
        }

        // Swap links: prev -> last and first -> next
        return costFn(prev, last) + costFn(first, next ?: first)
    }

    private fun reverseSegment(path: MutableList<Stop>, i: Int, j: Int) {
        var left = i
        var right = j
        while (left < right) {
            val temp = path[left]
            path[left] = path[right]
            path[right] = temp
            left++
            right--
        }
    }

    /**
     * Distance calculation using Haversine formula (Great Circle Distance).
     * Returns distance in kilometers.
     */
    fun calculateDistance(loc1: Location, loc2: Location): Double {
        val r = 6371.0 // Earth radius in KM
        val dLat = (loc2.lat - loc1.lat).toRadians()
        val dLon = (loc2.lng - loc1.lng).toRadians()
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(loc1.lat.toRadians()) * cos(loc2.lat.toRadians()) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        // Apply urban factor for more realistic distance
        return r * c * URBAN_DISTANCE_FACTOR
    }
    
    /**
     * Estimate travel time between two locations.
     * Returns time in minutes.
     */
    fun calculateTravelTime(loc1: Location, loc2: Location): Double {
        val distanceKm = calculateDistance(loc1, loc2)
        // Time in hours = distance / speed
        // Time in minutes = time_hours * 60
        return (distanceKm / AVERAGE_SPEED_KMH) * 60.0
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}
