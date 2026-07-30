package com.tacticalbeacon.offline

import android.content.Context
import org.osmdroid.util.BoundingBox

data class DownloadRegion(
    val name: String,
    val boundingBox: BoundingBox,
    val minZoom: Int = 8,
    val maxZoom: Int = 16,
    val mapTypes: List<String> = listOf("STANDARD", "SATELLITE")
)

class RegionManager(private val context: Context) {

    private val regions = mutableListOf<DownloadRegion>()

    fun addRegion(region: DownloadRegion) {
        regions.add(region)
    }

    fun removeRegion(region: DownloadRegion) {
        regions.remove(region)
    }

    fun getRegions(): List<DownloadRegion> = regions.toList()

    fun getRegionByName(name: String): DownloadRegion? {
        return regions.find { it.name == name }
    }

    fun clearRegions() {
        regions.clear()
    }
}