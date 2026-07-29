package com.tacticalbeacon

import com.tacticalbeacon.data.model.getAlertIntervalMs
import com.tacticalbeacon.data.model.getProximityLevel
import com.tacticalbeacon.data.model.ProximityLevel
import com.tacticalbeacon.location.LocationManager
import org.junit.Assert.*
import org.junit.Test

class LocationManagerTest {

    @Test
    fun `distance format metric`() {
        assertEquals("500 m", LocationManager.formatDistance(500.0, useMetric = true))
        assertEquals("1.50 km", LocationManager.formatDistance(1500.0, useMetric = true))
    }

    @Test
    fun `distance format imperial`() {
        val result = LocationManager.formatDistance(500.0, useMetric = false)
        assertTrue(result.contains("ft"))
    }

    @Test
    fun `proximity level over 200m is FAR`() {
        assertEquals(ProximityLevel.FAR, getProximityLevel(250.0))
    }

    @Test
    fun `proximity level under 2m is ARRIVED`() {
        assertEquals(ProximityLevel.ARRIVED, getProximityLevel(1.0))
    }

    @Test
    fun `alert interval over 200m is null`() {
        assertNull(getAlertIntervalMs(250.0))
    }

    @Test
    fun `alert interval at 150m is 10 seconds`() {
        assertEquals(10_000L, getAlertIntervalMs(150.0))
    }

    @Test
    fun `alert interval at 75m is 5 seconds`() {
        assertEquals(5_000L, getAlertIntervalMs(75.0))
    }

    @Test
    fun `alert interval at 30m is 2 seconds`() {
        assertEquals(2_000L, getAlertIntervalMs(30.0))
    }

    @Test
    fun `alert interval at 15m is 1 second`() {
        assertEquals(1_000L, getAlertIntervalMs(15.0))
    }

    @Test
    fun `alert interval at 7m is 500ms`() {
        assertEquals(500L, getAlertIntervalMs(7.0))
    }

    @Test
    fun `alert interval at 3m is rapid`() {
        assertEquals(200L, getAlertIntervalMs(3.0))
    }

    @Test
    fun `alert interval at 1m is continuous`() {
        assertEquals(0L, getAlertIntervalMs(1.0))
    }
}
