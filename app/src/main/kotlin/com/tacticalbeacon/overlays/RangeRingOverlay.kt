package com.tacticalbeacon.overlays

import android.graphics.Canvas
import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.toRadians

class RangeRingOverlay : Overlay() {

    private val ringPaint = Paint().apply {
        color = android.graphics.Color.argb(100, 107, 124, 58)
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 107, 124, 58)
        textSize = 20f
        typeface = android.graphics.Typeface.MONOSPACE
        isAntiAlias = true
    }

    private var centerPoint: GeoPoint? = null
    private var radiiMeters: List<Double> = listOf(100.0, 200.0, 500.0, 1000.0)
    private var showRangeRings = false

    fun setCenter(point: GeoPoint) {
        centerPoint = point
    }

    fun setRadii(radii: List<Double>) {
        radiiMeters = radii
    }

    fun setVisible(visible: Boolean) {
        showRangeRings = visible
    }

    fun clear() {
        centerPoint = null
        showRangeRings = false
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        if (!showRangeRings) return
        if (centerPoint == null) return

        val projection = mapView.projection
        val centerPixel = projection.toPixels(centerPoint, null)

        for (radiusM in radiiMeters) {
            val radiusPx = metersToPixels(radiusM, centerPoint!!.latitude, mapView)
            canvas.drawCircle(
                centerPixel.x.toFloat(),
                centerPixel.y.toFloat(),
                radiusPx.toFloat(),
                ringPaint
            )

            val labelAngle = toRadians(45.0)
            val labelX = centerPixel.x + (radiusPx * kotlin.math.cos(labelAngle)).toFloat()
            val labelY = centerPixel.y - (radiusPx * kotlin.math.sin(labelAngle)).toFloat()
            canvas.drawText(
                formatRadius(radiusM),
                labelX,
                labelY,
                labelPaint
            )
        }
    }

    private fun metersToPixels(meters: Double, latitude: Double, mapView: MapView): Double {
        val earthCircumference = 40075000.0
        val metersPerPixel = earthCircumference * cos(
            toRadians(latitude)
        ) / (256.0 * pow(2.0, mapView.zoomLevelDouble))
        return meters / metersPerPixel
    }

    private fun formatRadius(meters: Double): String {
        return when {
            meters >= 1000 -> String.format("%.1f km", meters / 1000.0)
            else -> String.format("%.0f m", meters)
        }
    }
}