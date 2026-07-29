package com.tacticalbeacon.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tacticalbeacon.data.model.LocationState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    @SuppressLint("MissingPermission")
    fun startTracking(intervalMs: Long = 1000L, batterySaver: Boolean = false) {
        if (isTracking) return
        isTracking = true

        val priority = if (batterySaver)
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        else
            Priority.PRIORITY_HIGH_ACCURACY

        val request = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMaxUpdateDelayMillis(intervalMs * 2)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _locationState.value = location.toLocationState()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        isTracking = false
    }

    fun updateInterval(intervalMs: Long, batterySaver: Boolean = false) {
        if (isTracking) {
            stopTracking()
            startTracking(intervalMs, batterySaver)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(onResult: (LocationState?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onResult(location?.toLocationState())
        }.addOnFailureListener {
            onResult(null)
        }
    }

    private fun Location.toLocationState() = LocationState(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        bearing = bearing,
        speed = speed,
        timestamp = time,
        isValid = true
    )

    companion object {
        /**
         * Calculate distance in meters between two lat/lon points using the
         * Haversine formula for accuracy over short and medium distances.
         */
        fun distanceBetween(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val results = FloatArray(1)
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return results[0].toDouble()
        }

        /**
         * Calculate bearing from point 1 to point 2 in degrees (0–360).
         */
        fun bearingTo(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Float {
            val results = FloatArray(2)
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return (results[1] + 360f) % 360f
        }

        /**
         * Format distance for display based on unit preference.
         */
        fun formatDistance(meters: Double, useMetric: Boolean): String {
            return if (useMetric) {
                if (meters >= 1000.0) {
                    String.format("%.2f km", meters / 1000.0)
                } else {
                    String.format("%.0f m", meters)
                }
            } else {
                val feet = meters * 3.28084
                if (feet >= 5280.0) {
                    String.format("%.2f mi", feet / 5280.0)
                } else {
                    String.format("%.0f ft", feet)
                }
            }
        }
    }
}
