package com.tacticalbeacon.tiles

import android.content.Context
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

class TileCacheManager(private val context: Context) {

    fun downloadArea(
        mapView: MapView,
        boundingBox: BoundingBox,
        minZoom: Int = 8,
        maxZoom: Int = 16,
        onProgress: (Int, Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val cacheManager = CacheManager(mapView)
            val tileCount = cacheManager.possibleTilesInArea(boundingBox, minZoom, maxZoom)

            cacheManager.downloadAreaAsync(
                context,
                boundingBox,
                minZoom,
                maxZoom,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() = onComplete()
                    override fun onTaskFailed(errors: Int) = onError("Download failed with $errors errors")
                    override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                        onProgress(progress, tileCount)
                    }
                    override fun downloadStarted() {}
                    override fun setPossibleTilesInArea(total: Int) {}
                }
            )
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    fun getCacheSize(): Long {
        return try {
            val cacheDir = context.cacheDir.resolve("osmdroid_tiles")
            if (cacheDir.exists()) {
                cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun clearCache() {
        try {
            context.cacheDir.resolve("osmdroid_tiles").deleteRecursively()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun getTileSourceByName(name: String): org.osmdroid.tileprovider.tilesource.ITileSource {
        return when (name) {
            "SATELLITE" -> SatelliteTileSource
            "TOPO" -> TileSourceFactory.USGS_TOPO
            "HYBRID" -> TileSourceFactory.HYBRID
            "TERRAIN" -> TileSourceFactory.TERRAIN
            "DARK" -> TileSourceFactory.MAPNIK
            else -> TileSourceFactory.MAPNIK
        }
    }

    companion object {
        fun formatCacheSize(bytes: Long): String {
            return when {
                bytes >= 1_073_741_824L -> String.format("%.1f GB", bytes / 1_073_741_824.0)
                bytes >= 1_048_576L -> String.format("%.1f MB", bytes / 1_048_576.0)
                bytes >= 1_024L -> String.format("%.1f KB", bytes / 1_024.0)
                else -> "$bytes B"
            }
        }
    }
}