package com.tacticalbeacon.navigation

import android.graphics.Path
import com.tacticalbeacon.data.model.LocationState
import com.tacticalbeacon.data.model.Pin
import org.osmdroid.util.GeoPoint
import javax.inject.Singleton

@Singleton
class MeasurementTool {

    private val _measurementPoints = mutableListOf<GeoPoint>()
    val measurementPoints: List<GeoPoint> get() = _measurementPoints.toList()

    private var measurementType = MeasurementType.DISTANCE

    enum class MeasurementType {
        DISTANCE, AREA, BEARING, PATROL, DANGER_ZONE, SEARCH_SECTOR
    }

    fun startMeasurement(type: MeasurementType) {
        _measurementPoints.clear()
        measurementType = type
    }

    fun addPoint(point: GeoPoint) {
        _measurementPoints.add(point)
    }

    fun removeLastPoint() {
        if (_measurementPoints.isNotEmpty()) {
            _measurementPoints.removeAt(_measurementPoints.size - 1)
        }
    }

    fun clearMeasurement() {
        _measurementPoints.clear()
    }

    fun getDistance(): Double {
        if (_measurementPoints.size < 2) return 0.0
        var totalDistance = 0.0
        for (i in 1 until _measurementPoints.size) {
            totalDistance += computeDistance(_measurementPoints[i - 1], _measurementPoints[i])
        }
        return totalDistance
    }

    fun getArea(): Double {
        if (_measurementPoints.size < 3) return 0.0
        var area = 0.0
        for (i in _measurementPoints.indices) {
            val j = (i + 1) % _measurementPoints.size
            area += _measurementPoints[i].longitude * _measurementPoints[j].latitude
            area -= _measurementPoints[j].longitude * _measurementPoints[i].latitude
        }
        return kotlin.math.abs(area) / 2.0
    }

    fun getBearing(): Float {
        if (_measurementPoints.size < 2) return 0f
        return computeBearing(_measurementPoints[0], _measurementPoints.last())
    }

    fun isComplete(): Boolean {
        return when (measurementType) {
            MeasurementType.DISTANCE -> _measurementPoints.size >= 2
            MeasurementType.AREA -> _measurementPoints.size >= 3
            MeasurementType.BEARING -> _measurementPoints.size >= 2
            MeasurementType.PATROL -> _measurementPoints.size >= 2
            MeasurementType.DANGER_ZONE -> _measurementPoints.size >= 3
            MeasurementType.SEARCH_SECTOR -> _measurementPoints.size >= 3
        }
    }

    private fun computeDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0].toDouble()
    }

    private fun computeBearing(p1: GeoPoint, p2: GeoPoint): Float {
        val results = FloatArray(2)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return (results[1] + 360f) % 360f
    }
}