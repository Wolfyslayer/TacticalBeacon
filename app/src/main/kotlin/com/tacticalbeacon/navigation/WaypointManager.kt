package com.tacticalbeacon.navigation

import com.tacticalbeacon.data.model.Pin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaypointManager {

    private val _waypoints = mutableListOf<Pin>()
    val waypoints: List<Pin> get() = _waypoints.toList()

    fun addWaypoint(pin: Pin) {
        _waypoints.add(pin)
    }

    fun removeWaypoint(pin: Pin) {
        _waypoints.remove(pin)
    }

    fun clearWaypoints() {
        _waypoints.clear()
    }

    fun getNextWaypoint(currentIndex: Int): Pin? {
        return if (currentIndex + 1 < _waypoints.size) {
            _waypoints[currentIndex + 1]
        } else null
    }

    fun getCurrentWaypoint(index: Int): Pin? {
        return if (index < _waypoints.size) {
            _waypoints[index]
        } else null
    }

    fun getWaypointCount(): Int = _waypoints.size
}