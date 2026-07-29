package com.tacticalbeacon.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// ─── Pin Icon Types ───────────────────────────────────────────────────────────

enum class PinIcon(val label: String, val iconRes: String) {
    CAMP("Camp", "ic_pin_camp"),
    VEHICLE("Vehicle", "ic_pin_vehicle"),
    CACHE("Cache", "ic_pin_cache"),
    HUNTING_STAND("Hunting Stand", "ic_pin_hunting"),
    WAYPOINT("Waypoint", "ic_pin_waypoint"),
    DANGER("Danger", "ic_pin_danger"),
    OBJECTIVE("Objective", "ic_pin_objective"),
    EXTRACTION("Extraction", "ic_pin_extraction"),
    MEDICAL("Medical", "ic_pin_medical"),
    WATER("Water Source", "ic_pin_water"),
    FOOD("Food Cache", "ic_pin_food"),
    OBSERVATION("Observation Post", "ic_pin_observation")
}

// ─── Pin Color ────────────────────────────────────────────────────────────────

enum class PinColor(val label: String, val hexColor: String) {
    OLIVE("Olive", "#6B7C3A"),
    RED("Red", "#D32F2F"),
    AMBER("Amber", "#F57F17"),
    BLUE("Blue", "#1565C0"),
    WHITE("White", "#FAFAFA"),
    CYAN("Cyan", "#00838F"),
    PURPLE("Purple", "#6A1B9A"),
    ORANGE("Orange", "#E65100")
}

// ─── Pin Entity (Room) ────────────────────────────────────────────────────────

@Entity(tableName = "pins")
data class Pin(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val notes: String = "",
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val icon: PinIcon = PinIcon.WAYPOINT,
    val color: PinColor = PinColor.OLIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ─── Breadcrumb Entity (Room) ─────────────────────────────────────────────────

@Entity(tableName = "breadcrumbs")
data class Breadcrumb(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── Location State ───────────────────────────────────────────────────────────

data class LocationState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val bearing: Float = 0f,
    val speed: Float = 0f,
    val timestamp: Long = 0L,
    val isValid: Boolean = false
)

// ─── Navigation State ─────────────────────────────────────────────────────────

data class NavigationState(
    val targetPin: Pin? = null,
    val distanceMeters: Double = 0.0,
    val bearingDegrees: Float = 0f,
    val isNavigating: Boolean = false
)

// ─── Proximity Level ─────────────────────────────────────────────────────────

enum class ProximityLevel(val label: String) {
    OUT_OF_RANGE("Out of Range"),
    FAR("Far"),          // > 200m
    NEAR("Near"),        // 100–200m
    CLOSE("Close"),      // 50–100m
    VERY_CLOSE("Very Close"),  // 20–50m
    IMMEDIATE("Immediate"),    // 10–20m
    CRITICAL("Critical"),      // 5–10m
    ARRIVED("Arrived")         // < 5m (or < 2m for continuous)
}

fun getProximityLevel(distanceMeters: Double): ProximityLevel {
    return when {
        distanceMeters > 200.0 -> ProximityLevel.FAR
        distanceMeters > 100.0 -> ProximityLevel.NEAR
        distanceMeters > 50.0  -> ProximityLevel.CLOSE
        distanceMeters > 20.0  -> ProximityLevel.VERY_CLOSE
        distanceMeters > 10.0  -> ProximityLevel.IMMEDIATE
        distanceMeters > 5.0   -> ProximityLevel.CRITICAL
        distanceMeters > 2.0   -> ProximityLevel.ARRIVED
        else                   -> ProximityLevel.ARRIVED
    }
}

// ─── Alert Interval (ms) per proximity level ─────────────────────────────────

fun getAlertIntervalMs(distanceMeters: Double): Long? {
    return when {
        distanceMeters > 200.0 -> null           // Silent
        distanceMeters > 100.0 -> 10_000L        // Every 10 seconds
        distanceMeters > 50.0  -> 5_000L         // Every 5 seconds
        distanceMeters > 20.0  -> 2_000L         // Every 2 seconds
        distanceMeters > 10.0  -> 1_000L         // Every second
        distanceMeters > 5.0   -> 500L           // Twice per second
        distanceMeters > 2.0   -> 200L           // Rapid
        else                   -> 0L             // Continuous
    }
}

// ─── GPX / JSON Export Models ─────────────────────────────────────────────────

data class ExportPin(
    val id: String,
    val name: String,
    val notes: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val icon: String,
    val color: String,
    val createdAt: Long
)

data class ExportData(
    val version: String = "1.0",
    val appName: String = "TacticalBeacon",
    val exportedAt: Long = System.currentTimeMillis(),
    val pins: List<ExportPin>
)

// ─── Map Settings ─────────────────────────────────────────────────────────────

enum class MapType(val label: String) {
    STANDARD("Standard"),
    SATELLITE("Satellite"),
    TOPO("Topographic"),
    OFFLINE("Offline")
}
