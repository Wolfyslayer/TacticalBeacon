package com.tacticalbeacon.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tactical_beacon_settings")

data class AppSettings(
    // Units
    val useMetric: Boolean = true,
    // GPS
    val gpsUpdateIntervalMs: Long = 1000L,
    val batterySaverMode: Boolean = false,
    // Display
    val keepScreenAwake: Boolean = true,
    val showGrid: Boolean = false,
    val showBreadcrumbs: Boolean = true,
    val showCompassOverlay: Boolean = true,
    val showRangeRings: Boolean = false,
    val showMeasurementOverlay: Boolean = false,
    // Theme
    val themeMode: String = "DARK",
    val redLightMode: Boolean = false,
    // Coordinate format
    val coordinateFormat: String = "DECIMAL_DEGREES",
    // Navigation
    val autoCenterOnUser: Boolean = true,
    val showHeadingCone: Boolean = true,
    // Alert
    val alertVolume: Float = 0.8f,
    val vibrationStrength: Int = 3,
    val proximityAlert200m: Boolean = true,
    val proximityAlert100m: Boolean = true,
    val proximityAlert50m: Boolean = true,
    val proximityAlert20m: Boolean = true,
    val proximityAlert10m: Boolean = true,
    val proximityAlert5m: Boolean = true,
    val proximityAlert2m: Boolean = true,
    // Custom distances (meters)
    val customDist1: Int = 200,
    val customDist2: Int = 100,
    val customDist3: Int = 50,
    val customDist4: Int = 20,
    val customDist5: Int = 10,
    val customDist6: Int = 5,
    val customDist7: Int = 2,
    // Map
    val defaultMapType: String = "STANDARD",
    val lastLatitude: Double = 0.0,
    val lastLongitude: Double = 0.0,
    val lastZoom: Double = 14.0
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val USE_METRIC = booleanPreferencesKey("use_metric")
        val GPS_INTERVAL = longPreferencesKey("gps_interval_ms")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val SHOW_GRID = booleanPreferencesKey("show_grid")
        val SHOW_BREADCRUMBS = booleanPreferencesKey("show_breadcrumbs")
        val SHOW_COMPASS_OVERLAY = booleanPreferencesKey("show_compass_overlay")
        val SHOW_RANGE_RINGS = booleanPreferencesKey("show_range_rings")
        val SHOW_MEASUREMENT_OVERLAY = booleanPreferencesKey("show_measurement_overlay")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val RED_LIGHT_MODE = booleanPreferencesKey("red_light_mode")
        val COORDINATE_FORMAT = stringPreferencesKey("coordinate_format")
        val AUTO_CENTER = booleanPreferencesKey("auto_center")
        val SHOW_HEADING_CONE = booleanPreferencesKey("show_heading_cone")
        val ALERT_VOLUME = floatPreferencesKey("alert_volume")
        val VIBRATION_STRENGTH = intPreferencesKey("vibration_strength")
        val PROX_200 = booleanPreferencesKey("prox_200")
        val PROX_100 = booleanPreferencesKey("prox_100")
        val PROX_50 = booleanPreferencesKey("prox_50")
        val PROX_20 = booleanPreferencesKey("prox_20")
        val PROX_10 = booleanPreferencesKey("prox_10")
        val PROX_5 = booleanPreferencesKey("prox_5")
        val PROX_2 = booleanPreferencesKey("prox_2")
        val DIST_1 = intPreferencesKey("dist_1")
        val DIST_2 = intPreferencesKey("dist_2")
        val DIST_3 = intPreferencesKey("dist_3")
        val DIST_4 = intPreferencesKey("dist_4")
        val DIST_5 = intPreferencesKey("dist_5")
        val DIST_6 = intPreferencesKey("dist_6")
        val DIST_7 = intPreferencesKey("dist_7")
        val MAP_TYPE = stringPreferencesKey("map_type")
        val LAST_LAT = doublePreferencesKey("last_lat")
        val LAST_LON = doublePreferencesKey("last_lon")
        val LAST_ZOOM = doublePreferencesKey("last_zoom")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            AppSettings(
                useMetric = prefs[Keys.USE_METRIC] ?: true,
                gpsUpdateIntervalMs = prefs[Keys.GPS_INTERVAL] ?: 1000L,
                batterySaverMode = prefs[Keys.BATTERY_SAVER] ?: false,
                keepScreenAwake = prefs[Keys.KEEP_SCREEN_AWAKE] ?: true,
                showGrid = prefs[Keys.SHOW_GRID] ?: false,
                showBreadcrumbs = prefs[Keys.SHOW_BREADCRUMBS] ?: true,
                showCompassOverlay = prefs[Keys.SHOW_COMPASS_OVERLAY] ?: true,
                showRangeRings = prefs[Keys.SHOW_RANGE_RINGS] ?: false,
                showMeasurementOverlay = prefs[Keys.SHOW_MEASUREMENT_OVERLAY] ?: false,
                themeMode = prefs[Keys.THEME_MODE] ?: "DARK",
                redLightMode = prefs[Keys.RED_LIGHT_MODE] ?: false,
                coordinateFormat = prefs[Keys.COORDINATE_FORMAT] ?: "DECIMAL_DEGREES",
                autoCenterOnUser = prefs[Keys.AUTO_CENTER] ?: true,
                showHeadingCone = prefs[Keys.SHOW_HEADING_CONE] ?: true,
                alertVolume = prefs[Keys.ALERT_VOLUME] ?: 0.8f,
                vibrationStrength = prefs[Keys.VIBRATION_STRENGTH] ?: 3,
                proximityAlert200m = prefs[Keys.PROX_200] ?: true,
                proximityAlert100m = prefs[Keys.PROX_100] ?: true,
                proximityAlert50m = prefs[Keys.PROX_50] ?: true,
                proximityAlert20m = prefs[Keys.PROX_20] ?: true,
                proximityAlert10m = prefs[Keys.PROX_10] ?: true,
                proximityAlert5m = prefs[Keys.PROX_5] ?: true,
                proximityAlert2m = prefs[Keys.PROX_2] ?: true,
                customDist1 = prefs[Keys.DIST_1] ?: 200,
                customDist2 = prefs[Keys.DIST_2] ?: 100,
                customDist3 = prefs[Keys.DIST_3] ?: 50,
                customDist4 = prefs[Keys.DIST_4] ?: 20,
                customDist5 = prefs[Keys.DIST_5] ?: 10,
                customDist6 = prefs[Keys.DIST_6] ?: 5,
                customDist7 = prefs[Keys.DIST_7] ?: 2,
                defaultMapType = prefs[Keys.MAP_TYPE] ?: "STANDARD",
                lastLatitude = prefs[Keys.LAST_LAT] ?: 0.0,
                lastLongitude = prefs[Keys.LAST_LON] ?: 0.0,
                lastZoom = prefs[Keys.LAST_ZOOM] ?: 14.0
            )
        }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_COMPASS_OVERLAY] = settings.showCompassOverlay
            prefs[Keys.SHOW_RANGE_RINGS] = settings.showRangeRings
            prefs[Keys.SHOW_MEASUREMENT_OVERLAY] = settings.showMeasurementOverlay
            prefs[Keys.THEME_MODE] = settings.themeMode
            prefs[Keys.RED_LIGHT_MODE] = settings.redLightMode
            prefs[Keys.COORDINATE_FORMAT] = settings.coordinateFormat
            prefs[Keys.AUTO_CENTER] = settings.autoCenterOnUser
            prefs[Keys.SHOW_HEADING_CONE] = settings.showHeadingCone
            prefs[Keys.USE_METRIC] = settings.useMetric
            prefs[Keys.GPS_INTERVAL] = settings.gpsUpdateIntervalMs
            prefs[Keys.BATTERY_SAVER] = settings.batterySaverMode
            prefs[Keys.KEEP_SCREEN_AWAKE] = settings.keepScreenAwake
            prefs[Keys.SHOW_GRID] = settings.showGrid
            prefs[Keys.SHOW_BREADCRUMBS] = settings.showBreadcrumbs
            prefs[Keys.ALERT_VOLUME] = settings.alertVolume
            prefs[Keys.VIBRATION_STRENGTH] = settings.vibrationStrength
            prefs[Keys.PROX_200] = settings.proximityAlert200m
            prefs[Keys.PROX_100] = settings.proximityAlert100m
            prefs[Keys.PROX_50] = settings.proximityAlert50m
            prefs[Keys.PROX_20] = settings.proximityAlert20m
            prefs[Keys.PROX_10] = settings.proximityAlert10m
            prefs[Keys.PROX_5] = settings.proximityAlert5m
            prefs[Keys.PROX_2] = settings.proximityAlert2m
            prefs[Keys.DIST_1] = settings.customDist1
            prefs[Keys.DIST_2] = settings.customDist2
            prefs[Keys.DIST_3] = settings.customDist3
            prefs[Keys.DIST_4] = settings.customDist4
            prefs[Keys.DIST_5] = settings.customDist5
            prefs[Keys.DIST_6] = settings.customDist6
            prefs[Keys.DIST_7] = settings.customDist7
            prefs[Keys.MAP_TYPE] = settings.defaultMapType
        }
    }

    suspend fun saveMapPosition(lat: Double, lon: Double, zoom: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_LAT] = lat
            prefs[Keys.LAST_LON] = lon
            prefs[Keys.LAST_ZOOM] = zoom
        }
    }
}
