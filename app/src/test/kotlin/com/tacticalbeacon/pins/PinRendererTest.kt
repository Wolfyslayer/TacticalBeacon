package com.tacticalbeacon.pins

import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinPriority
import org.junit.Assert.assertTrue
import org.junit.Test

class PinRendererTest {

    @Test
    fun markerScaleIncreasesForHigherPriorityPins() {
        val renderer = PinRenderer()

        val normalPin = Pin(
            name = "Normal Pin",
            latitude = 0.0,
            longitude = 0.0,
            priority = PinPriority.NORMAL
        )
        val criticalPin = Pin(
            name = "Critical Pin",
            latitude = 0.0,
            longitude = 0.0,
            priority = PinPriority.CRITICAL
        )

        assertTrue(renderer.getMarkerScale(normalPin, false) < renderer.getMarkerScale(criticalPin, false))
        assertTrue(renderer.getMarkerScale(criticalPin, true) > renderer.getMarkerScale(criticalPin, false))
    }
}
