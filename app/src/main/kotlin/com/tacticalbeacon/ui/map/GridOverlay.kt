package com.tacticalbeacon.ui.map

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * Draws a coordinate grid overlay on the OSMDroid map.
 * Grid lines are spaced based on the current zoom level.
 */
class GridOverlay : Overlay() {

    private val gridPaint = Paint().apply {
        color = android.graphics.Color.argb(60, 107, 124, 58) // Olive green, semi-transparent
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 107, 124, 58)
        textSize = 22f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val zoomLevel = mapView.zoomLevelDouble
        val gridSpacingDeg = when {
            zoomLevel >= 16 -> 0.001
            zoomLevel >= 14 -> 0.005
            zoomLevel >= 12 -> 0.01
            zoomLevel >= 10 -> 0.05
            zoomLevel >= 8  -> 0.1
            zoomLevel >= 6  -> 0.5
            else            -> 1.0
        }

        val projection = mapView.projection
        val north = projection.fromPixels(0, 0)
        val south = projection.fromPixels(mapView.width, mapView.height)

        val minLat = minOf(north.latitude, south.latitude)
        val maxLat = maxOf(north.latitude, south.latitude)
        val minLon = minOf(north.longitude, south.longitude)
        val maxLon = maxOf(north.longitude, south.longitude)

        // Draw latitude lines
        var lat = Math.floor(minLat / gridSpacingDeg) * gridSpacingDeg
        while (lat <= maxLat) {
            val pt1 = projection.toPixels(GeoPoint(lat, minLon), null)
            val pt2 = projection.toPixels(GeoPoint(lat, maxLon), null)
            canvas.drawLine(pt1.x.toFloat(), pt1.y.toFloat(), pt2.x.toFloat(), pt2.y.toFloat(), gridPaint)

            // Label
            val label = String.format("%.${gridSpacingDeg.toString().length - 2}f°", lat)
            canvas.drawText(label, 8f, pt1.y.toFloat() - 4f, labelPaint)
            lat += gridSpacingDeg
        }

        // Draw longitude lines
        var lon = Math.floor(minLon / gridSpacingDeg) * gridSpacingDeg
        while (lon <= maxLon) {
            val pt1 = projection.toPixels(GeoPoint(maxLat, lon), null)
            val pt2 = projection.toPixels(GeoPoint(minLat, lon), null)
            canvas.drawLine(pt1.x.toFloat(), pt1.y.toFloat(), pt2.x.toFloat(), pt2.y.toFloat(), gridPaint)

            // Label
            val label = String.format("%.${gridSpacingDeg.toString().length - 2}f°", lon)
            canvas.drawText(label, pt1.x.toFloat() + 4f, mapView.height.toFloat() - 8f, labelPaint)
            lon += gridSpacingDeg
        }
    }
}
