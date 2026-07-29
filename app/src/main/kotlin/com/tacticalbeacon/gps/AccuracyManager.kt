package com.tacticalbeacon.gps

import android.content.Context
import android.location.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccuracyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _accuracy = MutableStateFlow(0f)
    val accuracy: StateFlow<Float> = _accuracy.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(false)
    val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    private val _hasGpsFix = MutableStateFlow(false)
    val hasGpsFix: StateFlow<Boolean> = _hasGpsFix.asStateFlow()

    fun updateAccuracy(accuracy: Float) {
        _accuracy.value = accuracy
    }

    fun updateGpsStatus(enabled: Boolean) {
        _isGpsEnabled.value = enabled
    }

    fun updateGpsFix(hasFix: Boolean) {
        _hasGpsFix.value = hasFix
    }

    fun isAccuracyPoor(threshold: Float = 20f): Boolean {
        return _accuracy.value > threshold
    }

    fun getAccuracyLevel(): AccuracyLevel {
        val acc = _accuracy.value
        return when {
            acc <= 5f -> AccuracyLevel.EXCELLENT
            acc <= 10f -> AccuracyLevel.GOOD
            acc <= 20f -> AccuracyLevel.FAIR
            acc <= 50f -> AccuracyLevel.POOR
            else -> AccuracyLevel.VERY_POOR
        }
    }

    enum class AccuracyLevel {
        EXCELLENT, GOOD, FAIR, POOR, VERY_POOR
    }
}