package com.tacticalbeacon.team

import javax.inject.Singleton

@Singleton
class BeaconManager {

    private var isBeaconActive = false
    private var beaconLatitude = 0.0
    private var beaconLongitude = 0.0
    private var beaconAltitude = 0.0

    fun activateBeacon(latitude: Double, longitude: Double, altitude: Double = 0.0) {
        beaconLatitude = latitude
        beaconLongitude = longitude
        beaconAltitude = altitude
        isBeaconActive = true
    }

    fun deactivateBeacon() {
        isBeaconActive = false
    }

    fun isActive(): Boolean = isBeaconActive

    fun getBeaconLocation(): Triple<Double, Double, Double> {
        return Triple(beaconLatitude, beaconLongitude, beaconAltitude)
    }
}