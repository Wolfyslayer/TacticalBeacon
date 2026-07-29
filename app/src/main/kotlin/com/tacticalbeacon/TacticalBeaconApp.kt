package com.tacticalbeacon

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class TacticalBeaconApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure OSMDroid
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = filesDir
            osmdroidTileCache = cacheDir.resolve("osmdroid_tiles")
            // Allow offline tile loading
            isDebugMode = false
            isDebugMapView = false
            isDebugTileProviders = false
        }
    }
}
