package com.swiftroute.app.shared.optimizer

import com.swiftroute.app.shared.model.Location
import com.swiftroute.app.shared.model.Stop
import kotlin.math.*

/**
 * SwiftRoute Optimizer — Phase 1: On-device TSP Solver.
 */
class RouteOptimizer {

    /**
     * Optimizes a list of stops between a start and end location.
     */
    fun optimize(
        start: Location,
        stops: List<Stop>,
        end: Location? = null
    ): List<Stop> {
        if (stops.size <= 1) return stops

        // Phase 1: Nearest Neighbor (Greedy)
        var currentPath = nearestNeighbor(start, stops, end)

        // Phase 2: 2-opt (Improvement)
        currentPath = twoOpt(start, currentPath, end)

        return currentPath
    }

    private fun nearestNeighbor(
        start: Location,
        stops: List<Stop>,
        end: Location?
    ): MutableList<Stop> {
        val unvisited = stops.toMutableList()
        val path = mutableListOf<Stop>()
        var currentPos = start

        while (unvisited.isNotEmpty()) {
            val nextStop = unvisited.minByOrNull { calculateDistance(currentPos, it.location) }!!
            path.add(nextStop)
            unvisited.remove(nextStop)
            currentPos = nextStop.location
        }

        return path
    }

    private fun twoOpt(
        start: Location,
        stops: List<Stop>,
        end: Location?
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
                    val currentDist = calculatePathSegmentDist(start, result, end, i, j)
                    val swappedDist = calculatePathSegmentDistSwapped(start, result, end, i, j)

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
        j: Int
    ): Double {
        val prev = if (i == 0) start else path[i - 1].location
        val first = path[i].location
        val last = path[j].location
        val next = if (j == path.size - 1) end else path[j + 1]?.location

        var dist = calculateDistance(prev, first) + calculateDistance(last, next ?: last)
        return dist
    }

    private fun calculatePathSegmentDistSwapped(
        start: Location,
        path: List<Stop>,
        end: Location?,
        i: Int,
        j: Int
    ): Double {
        val prev = if (i == 0) start else path[i - 1].location
        val first = path[i].location
        val last = path[j].location
        val next = if (j == path.size - 1) end else path[j + 1]?.location

        // Swap links: prev -> last and first -> next
        var dist = calculateDistance(prev, last) + calculateDistance(first, next ?: first)
        return dist
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
     */
    fun calculateDistance(loc1: Location, loc2: Location): Double {
        val r = 6371.0 // Earth radius in KM
        val dLat = (loc2.lat - loc1.lat).toRadians()
        val dLon = (loc2.lng - loc1.lng).toRadians()
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(loc1.lat.toRadians()) * cos(loc2.lat.toRadians()) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}
