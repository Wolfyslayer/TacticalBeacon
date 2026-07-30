package com.tacticalbeacon.navigation

import com.tacticalbeacon.data.model.Pin
import org.osmdroid.util.GeoPoint

data class RouteSegment(
    val from: GeoPoint,
    val to: GeoPoint,
    val distanceMeters: Double,
    val bearingDegrees: Float
)

data class RouteInfo(
    val totalDistanceMeters: Double,
    val totalBearingDegrees: Float,
    val etaSeconds: Long,
    val segments: List<RouteSegment>
)

class RouteRenderer {

    fun computeRoute(from: GeoPoint, to: GeoPoint): List<GeoPoint> {
        // Simple straight-line route for now
        // Future: implement routing algorithm
        return listOf(from, to)
    }

    fun computeRouteInfo(from: GeoPoint, to: GeoPoint): RouteInfo {
        val segments = listOf(
            RouteSegment(
                from = from,
                to = to,
                distanceMeters = computeDistance(from, to),
                bearingDegrees = computeBearing(from, to)
            )
        )

        val totalDistance = segments.sumOf { it.distanceMeters }
        val totalBearing = segments.firstOrNull()?.bearingDegrees ?: 0f

        return RouteInfo(
            totalDistanceMeters = totalDistance,
            totalBearingDegrees = totalBearing,
            etaSeconds = 0L,
            segments = segments
        )
    }

    fun computeDistance(from: GeoPoint, to: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun computeBearing(from: GeoPoint, to: GeoPoint): Float {
        val results = FloatArray(2)
        android.location.Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return (results[1] + 360f) % 360f
    }
}