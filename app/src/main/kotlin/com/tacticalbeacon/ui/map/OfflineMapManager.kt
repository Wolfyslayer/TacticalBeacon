package com.tacticalbeacon.ui.map

import android.content.Context
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

/**
 * Manages offline tile downloading for OSMDroid.
 * Allows users to pre-cache map tiles for an area so the app works
 * completely without internet connectivity.
 */
class OfflineMapManager(private val context: Context) {

    /**
     * Download tiles for a bounding box at specified zoom levels.
     * @param mapView The current map view
     * @param boundingBox The geographic area to download
     * @param minZoom Minimum zoom level (e.g., 8 for regional view)
     * @param maxZoom Maximum zoom level (e.g., 16 for street-level detail)
     * @param onProgress Progress callback (0–100)
     * @param onComplete Completion callback
     */
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

            // Estimate tile count before downloading
            val tileCount = cacheManager.possibleTilesInArea(boundingBox, minZoom, maxZoom)

            cacheManager.downloadAreaAsync(
                context,
                boundingBox,
                minZoom,
                maxZoom,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() {
                        onComplete()
                    }

                    override fun onTaskFailed(errors: Int) {
                        onError("Download failed with $errors errors")
                    }

                    override fun updateProgress(
                        progress: Int,
                        currentZoomLevel: Int,
                        zoomMin: Int,
                        zoomMax: Int
                    ) {
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

    /**
     * Get the size of the current tile cache in bytes.
     */
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

    /**
     * Clear all cached tiles.
     */
    fun clearCache() {
        try {
            context.cacheDir.resolve("osmdroid_tiles").deleteRecursively()
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        fun formatCacheSize(bytes: Long): String {
            return when {
                bytes >= 1_073_741_824L -> String.format("%.1f GB", bytes / 1_073_741_824.0)
                bytes >= 1_048_576L     -> String.format("%.1f MB", bytes / 1_048_576.0)
                bytes >= 1_024L         -> String.format("%.1f KB", bytes / 1_024.0)
                else                    -> "$bytes B"
            }
        }
    }
}
