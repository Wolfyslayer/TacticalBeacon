package com.tacticalbeacon.navigation

import com.tacticalbeacon.data.model.LocationState
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationEngine @Inject constructor(
    private val locationManager: LocationManager
) {

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private val _targetPin = MutableStateFlow<Pin?>(null)
    val targetPin: StateFlow<Pin?> = _targetPin.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0.0)
    val distanceMeters: StateFlow<Double> = _distanceMeters.asStateFlow()

    private val _bearingDegrees = MutableStateFlow(0f)
    val bearingDegrees: StateFlow<Float> = _bearingDegrees.asStateFlow()

    private val _etaSeconds = MutableStateFlow(0L)
    val etaSeconds: StateFlow<Long> = _etaSeconds.asStateFlow()

    private val _waypoints = MutableStateFlow<List<Pin>>(emptyList())
    val waypoints: StateFlow<List<Pin>> = _waypoints.asStateFlow()

    private val _isOffCourse = MutableStateFlow(false)
    val isOffCourse: StateFlow<Boolean> = _isOffCourse.asStateFlow()

    private val _turnNotification = MutableStateFlow<String?>(null)
    val turnNotification: StateFlow<String?> = _turnNotification.asStateFlow()

    fun startNavigation(target: Pin, waypoints: List<Pin> = emptyList()) {
        _targetPin.value = target
        _waypoints.value = waypoints
        _isNavigating.value = true
    }

    fun stopNavigation() {
        _targetPin.value = null
        _waypoints.value = emptyList()
        _isNavigating.value = false
        _distanceMeters.value = 0.0
        _bearingDegrees.value = 0f
        _etaSeconds.value = 0L
        _isOffCourse.value = false
        _turnNotification.value = null
    }

    fun updateLocation(location: LocationState) {
        if (!_isNavigating.value) return
        val target = _targetPin.value ?: return

        val distance = LocationManager.distanceBetween(
            location.latitude, location.longitude,
            target.latitude, target.longitude
        )
        val bearing = LocationManager.bearingTo(
            location.latitude, location.longitude,
            target.latitude, target.longitude
        )

        _distanceMeters.value = distance
        _bearingDegrees.value = bearing

        // ETA based on speed
        val speed = location.speed
        if (speed > 0.1f) {
            _etaSeconds.value = (distance / speed.toDouble()).toLong()
        }

        // Off-course detection (deviation > 20m from bearing line)
        _isOffCourse.value = distance > 20.0 && kotlin.math.abs(bearing - location.bearing) > 30f

        // Turn notifications
        _turnNotification.value = when {
            distance < 5.0 -> "ARRIVED"
            distance < 10.0 -> "ARRIVING"
            distance < 50.0 -> "CLOSE"
            distance < 100.0 -> "APPROACHING"
            else -> null
        }
    }
}