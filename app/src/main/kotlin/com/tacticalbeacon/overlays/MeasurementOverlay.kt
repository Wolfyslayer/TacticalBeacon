package com.tacticalbeacon.overlays

import android.graphics.Canvas
import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class MeasurementOverlay : Overlay() {

    private val linePaint = Paint().apply {
        color = android.graphics.Color.argb(220, 245, 127, 23)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(15f, 10f), 0f)
    }

    private val textPaint = Paint().apply {
        color = android.graphics.Color.argb(220, 245, 127, 23)
        textSize = 28f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val fillPaint = Paint().apply {
        color = android.graphics.Color.argb(60, 245, 127, 23)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var measurementPoints: List<GeoPoint> = emptyList()
    private var measurementType: MeasurementType = MeasurementType.DISTANCE

    enum class MeasurementType {
        DISTANCE, AREA, BEARING
    }

    fun setMeasurementPoints(points: List<GeoPoint>, type: MeasurementType) {
        measurementPoints = points
        measurementType = type
    }

    fun clear() {
        measurementPoints = emptyList()
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        if (measurementPoints.size < 2) return

        val projection = mapView.projection

        when (measurementType) {
            MeasurementType.DISTANCE -> drawDistanceMeasurement(canvas, projection)
            MeasurementType.AREA -> drawAreaMeasurement(canvas, projection)
            MeasurementType.BEARING -> drawBearingMeasurement(canvas, projection)
        }
    }

    private fun drawDistanceMeasurement(canvas: Canvas, projection: org.osmdroid.views.Projection) {
        val points = measurementPoints.map { projection.toPixels(it, null) }

        for (i in 1 until points.size) {
            canvas.drawLine(
                points[i - 1].x.toFloat(), points[i - 1].y.toFloat(),
                points[i].x.toFloat(), points[i].y.toFloat(),
                linePaint
            )

            val midX = (points[i - 1].x + points[i].x) / 2f
            val midY = (points[i - 1].y + points[i].y) / 2f - 20f

            val distance = computeDistance(measurementPoints[i - 1], measurementPoints[i])
            canvas.drawText(
                formatDistance(distance),
                midX, midY,
                textPaint
            )
        }
    }

    private fun drawAreaMeasurement(canvas: Canvas, projection: org.osmdroid.views.Projection) {
        if (measurementPoints.size < 3) return

        val points = measurementPoints.map { projection.toPixels(it, null) }
        val path = android.graphics.Path().apply {
            moveTo(points[0].x.toFloat(), points[0].y.toFloat())
            for (i in 1 until points.size) {
                lineTo(points[i].x.toFloat(), points[i].y.toFloat())
            }
            close()
        }
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, linePaint)

        // Compute approximate area
        val area = computeArea(measurementPoints)
        val centroid = computeCentroid(points)
        canvas.drawText(
            formatArea(area),
            centroid.x.toFloat(),
            centroid.y.toFloat() - 20f,
            textPaint
        )
    }

    private fun drawBearingMeasurement(canvas: Canvas, projection: org.osmdroid.views.Projection) {
        if (measurementPoints.size < 2) return

        val p1 = projection.toPixels(measurementPoints[0], null)
        val p2 = projection.toPixels(measurementPoints[1], null)

        canvas.drawLine(
            p1.x.toFloat(), p1.y.toFloat(),
            p2.x.toFloat(), p2.y.toFloat(),
            linePaint
        )

        val midX = (p1.x + p2.x) / 2f
        val midY = (p1.y + p2.y) / 2f - 20f

        val bearing = computeBearing(measurementPoints[0], measurementPoints[1])
        canvas.drawText(
            "${bearing.toInt()}°",
            midX, midY,
            textPaint
        )
    }

    private fun computeDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0].toDouble()
    }

    private fun computeArea(points: List<GeoPoint>): Double {
        if (points.size < 3) return 0.0
        var area = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            area += points[i].longitude * points[j].latitude
            area -= points[j].longitude * points[i].latitude
        }
        return kotlin.math.abs(area) / 2.0
    }

    private fun computeCentroid(points: List<android.graphics.Point>): android.graphics.Point {
        var sumX = 0f
        var sumY = 0f
        for (p in points) {
            sumX += p.x
            sumY += p.y
        }
        return android.graphics.Point(
            (sumX / points.size).toInt(),
            (sumY / points.size).toInt()
        )
    }

    private fun computeBearing(p1: GeoPoint, p2: GeoPoint): Float {
        val results = FloatArray(2)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return (results[1] + 360f) % 360f
    }

    private fun formatDistance(meters: Double): String {
        return when {
            meters >= 1000 -> String.format("%.1f km", meters / 1000.0)
            else -> String.format("%.0f m", meters)
        }
    }

    private fun formatArea(squareDegrees: Double): String {
        val areaMeters = squareDegrees * 111000.0 * 111000.0
        return when {
            areaMeters >= 1_000_000 -> String.format("%.1f km²", areaMeters / 1_000_000.0)
            else -> String.format("%.0f m²", areaMeters)
        }
    }
}