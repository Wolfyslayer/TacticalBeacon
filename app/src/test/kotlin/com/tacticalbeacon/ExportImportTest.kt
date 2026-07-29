package com.tacticalbeacon

import com.google.gson.Gson
import com.tacticalbeacon.data.model.*
import org.junit.Assert.*
import org.junit.Test

class ExportImportTest {

    private val samplePins = listOf(
        Pin(
            id = "test-1",
            name = "Alpha Base",
            notes = "Main camp location",
            latitude = 51.5074,
            longitude = -0.1278,
            altitude = 15.0,
            icon = PinIcon.CAMP,
            color = PinColor.OLIVE
        ),
        Pin(
            id = "test-2",
            name = "Vehicle Cache",
            notes = "",
            latitude = 51.5100,
            longitude = -0.1300,
            altitude = 0.0,
            icon = PinIcon.VEHICLE,
            color = PinColor.AMBER
        )
    )

    @Test
    fun `json export contains all pins`() {
        val gson = Gson()
        val exportData = ExportData(
            pins = samplePins.map { pin ->
                ExportPin(
                    id = pin.id,
                    name = pin.name,
                    notes = pin.notes,
                    latitude = pin.latitude,
                    longitude = pin.longitude,
                    altitude = pin.altitude,
                    icon = pin.icon.name,
                    color = pin.color.name,
                    createdAt = pin.createdAt
                )
            }
        )
        val json = gson.toJson(exportData)
        assertTrue(json.contains("Alpha Base"))
        assertTrue(json.contains("Vehicle Cache"))
        assertTrue(json.contains("51.5074"))
    }

    @Test
    fun `pin color values are valid`() {
        PinColor.entries.forEach { color ->
            assertTrue(color.hexColor.startsWith("#"))
            assertEquals(7, color.hexColor.length)
        }
    }

    @Test
    fun `all pin icons have labels`() {
        PinIcon.entries.forEach { icon ->
            assertTrue(icon.label.isNotBlank())
        }
    }

    @Test
    fun `proximity levels cover all distance ranges`() {
        val testDistances = listOf(300.0, 150.0, 75.0, 35.0, 15.0, 7.0, 3.0, 1.0)
        val expectedLevels = listOf(
            ProximityLevel.FAR,
            ProximityLevel.NEAR,
            ProximityLevel.CLOSE,
            ProximityLevel.VERY_CLOSE,
            ProximityLevel.IMMEDIATE,
            ProximityLevel.CRITICAL,
            ProximityLevel.ARRIVED,
            ProximityLevel.ARRIVED
        )
        testDistances.forEachIndexed { i, dist ->
            assertEquals(expectedLevels[i], getProximityLevel(dist))
        }
    }
}
