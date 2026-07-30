package com.tacticalbeacon.gps

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(com.tacticalbeacon.data.model.LocationState())
    val locationState: StateFlow<com.tacticalbeacon.data.model.LocationState> = _locationState.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isTracking = false

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
            android.os.Looper.getMainLooper()
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

    fun getLastKnownLocation(onResult: (com.tacticalbeacon.data.model.LocationState?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onResult(location?.toLocationState())
        }.addOnFailureListener {
            onResult(null)
        }
    }

    private fun Location.toLocationState() = com.tacticalbeacon.data.model.LocationState(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        bearing = bearing,
        speed = speed,
        timestamp = time,
        isValid = true
    )
}