package com.tacticalbeacon.navigation

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacticalbeacon.data.model.*
import com.tacticalbeacon.data.repository.BreadcrumbRepository
import com.tacticalbeacon.data.repository.PinRepository
import com.tacticalbeacon.data.repository.SettingsRepository
import com.tacticalbeacon.location.CompassManager
import com.tacticalbeacon.location.LocationForegroundService
import com.tacticalbeacon.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val locationManager: LocationManager,
    val compassManager: CompassManager,
    val pinRepository: PinRepository,
    val breadcrumbRepository: BreadcrumbRepository,
    val settingsRepository: SettingsRepository,
    val proximityAlertManager: ProximityAlertManager
) : ViewModel() {

    // ─── Settings ─────────────────────────────────────────────────────────────
    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppSettings()
    )

    // ─── Location ─────────────────────────────────────────────────────────────
    val locationState = locationManager.locationState

    // ─── Compass ──────────────────────────────────────────────────────────────
    val azimuth = compassManager.azimuth
    val hasCompass = compassManager.hasCompass

    // ─── Pins ─────────────────────────────────────────────────────────────────
    val pins = pinRepository.getAllPins().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // ─── Navigation State ─────────────────────────────────────────────────────
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // ─── Breadcrumb Session ───────────────────────────────────────────────────
    private val _sessionId = MutableStateFlow(UUID.randomUUID().toString())
    val sessionId: StateFlow<String> = _sessionId.asStateFlow()

    val breadcrumbs = _sessionId.flatMapLatest { sid ->
        breadcrumbRepository.getBreadcrumbsForSession(sid)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // ─── GPS Accuracy Warning ─────────────────────────────────────────────────
    val isGpsAccuracyPoor: StateFlow<Boolean> = locationState.map { loc ->
        loc.isValid && loc.accuracy > 20f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var lastBreadcrumbTime = 0L
    private val BREADCRUMB_MIN_DISTANCE = 5.0 // meters
    private var lastBreadcrumbLat = 0.0
    private var lastBreadcrumbLon = 0.0

    init {
        startLocationTracking()
        observeLocationForNavigation()
        observeLocationForBreadcrumbs()
    }

    private fun startLocationTracking() {
        val settings = settings.value
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_START
            putExtra(LocationForegroundService.EXTRA_INTERVAL, settings.gpsUpdateIntervalMs)
            putExtra(LocationForegroundService.EXTRA_BATTERY_SAVER, settings.batterySaverMode)
        }
        context.startForegroundService(intent)
    }

    private fun observeLocationForNavigation() {
        viewModelScope.launch {
            locationState.collect { loc ->
                val navState = _navigationState.value
                if (navState.isNavigating && navState.targetPin != null && loc.isValid) {
                    val distance = LocationManager.distanceBetween(
                        loc.latitude, loc.longitude,
                        navState.targetPin.latitude, navState.targetPin.longitude
                    )
                    val bearing = LocationManager.bearingTo(
                        loc.latitude, loc.longitude,
                        navState.targetPin.latitude, navState.targetPin.longitude
                    )
                    _navigationState.value = navState.copy(
                        distanceMeters = distance,
                        bearingDegrees = bearing
                    )
                    proximityAlertManager.updateDistance(distance)
                }
            }
        }
    }

    private fun observeLocationForBreadcrumbs() {
        viewModelScope.launch {
            locationState.collect { loc ->
                if (!loc.isValid) return@collect
                val settings = settings.value
                if (!settings.showBreadcrumbs) return@collect

                val distFromLast = if (lastBreadcrumbLat != 0.0) {
                    LocationManager.distanceBetween(
                        lastBreadcrumbLat, lastBreadcrumbLon,
                        loc.latitude, loc.longitude
                    )
                } else Double.MAX_VALUE

                if (distFromLast >= BREADCRUMB_MIN_DISTANCE) {
                    breadcrumbRepository.addBreadcrumb(
                        Breadcrumb(
                            sessionId = _sessionId.value,
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            altitude = loc.altitude,
                            accuracy = loc.accuracy
                        )
                    )
                    lastBreadcrumbLat = loc.latitude
                    lastBreadcrumbLon = loc.longitude
                }
            }
        }
    }

    // ─── Navigation Controls ──────────────────────────────────────────────────

    fun startNavigation(pin: Pin) {
        _navigationState.value = NavigationState(
            targetPin = pin,
            isNavigating = true
        )
        val s = settings.value
        proximityAlertManager.start(s.alertVolume, s.vibrationStrength)
    }

    fun stopNavigation() {
        _navigationState.value = NavigationState()
        proximityAlertManager.stop()
    }

    // ─── Pin Management ───────────────────────────────────────────────────────

    fun savePin(pin: Pin) {
        viewModelScope.launch { pinRepository.savePin(pin) }
    }

    fun updatePin(pin: Pin) {
        viewModelScope.launch { pinRepository.updatePin(pin) }
    }

    fun deletePin(pin: Pin) {
        viewModelScope.launch {
            if (_navigationState.value.targetPin?.id == pin.id) {
                stopNavigation()
            }
            pinRepository.deletePin(pin)
        }
    }

    // ─── Breadcrumb Controls ──────────────────────────────────────────────────

    fun clearBreadcrumbs() {
        viewModelScope.launch {
            breadcrumbRepository.clearSession(_sessionId.value)
            _sessionId.value = UUID.randomUUID().toString()
            lastBreadcrumbLat = 0.0
            lastBreadcrumbLon = 0.0
        }
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
            // Restart location tracking with new interval
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = LocationForegroundService.ACTION_START
                putExtra(LocationForegroundService.EXTRA_INTERVAL, newSettings.gpsUpdateIntervalMs)
                putExtra(LocationForegroundService.EXTRA_BATTERY_SAVER, newSettings.batterySaverMode)
            }
            context.startForegroundService(intent)
            // Update alert settings if active
            if (proximityAlertManager.isRunning()) {
                proximityAlertManager.updateSettings(newSettings.alertVolume, newSettings.vibrationStrength)
            }
        }
    }

    fun saveMapPosition(lat: Double, lon: Double, zoom: Double) {
        viewModelScope.launch {
            settingsRepository.saveMapPosition(lat, lon, zoom)
        }
    }

    override fun onCleared() {
        super.onCleared()
        proximityAlertManager.stop()
    }
}
