package com.tacticalbeacon.overlays

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class NavigationOverlay : Overlay() {

    private val routePaint = Paint().apply {
        color = android.graphics.Color.argb(200, 107, 124, 58)
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(30f, 15f), 0f)
    }

    private val arrowPaint = Paint().apply {
        color = android.graphics.Color.argb(255, 107, 124, 58)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val bearingPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 245, 127, 23)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }

    private var targetGeoPoint: GeoPoint? = null
    private var userGeoPoint: GeoPoint? = null
    private var routePoints: List<GeoPoint> = emptyList()

    fun setTarget(point: GeoPoint) {
        targetGeoPoint = point
    }

    fun setUserLocation(point: GeoPoint) {
        userGeoPoint = point
    }

    fun setRoute(points: List<GeoPoint>) {
        routePoints = points
    }

    fun clearRoute() {
        routePoints = emptyList()
        targetGeoPoint = null
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val projection = mapView.projection

        // Draw route line
        if (routePoints.size > 1) {
            val points = routePoints.map { projection.toPixels(it, null) }
            val path = Path().apply {
                moveTo(points[0].x.toFloat(), points[0].y.toFloat())
                for (i in 1 until points.size) {
                    lineTo(points[i].x.toFloat(), points[i].y.toFloat())
                }
            }
            canvas.drawPath(path, routePaint)
        }

        // Draw bearing line from user to target
        if (userGeoPoint != null && targetGeoPoint != null) {
            val userPixel = projection.toPixels(userGeoPoint, null)
            val targetPixel = projection.toPixels(targetGeoPoint, null)

            canvas.drawLine(
                userPixel.x.toFloat(), userPixel.y.toFloat(),
                targetPixel.x.toFloat(), targetPixel.y.toFloat(),
                bearingPaint
            )

            // Draw direction arrow at target
            val angle = kotlin.math.atan2(
                (targetPixel.x - userPixel.x).toDouble(),
                (targetPixel.y - userPixel.y).toDouble()
            ).toFloat()

            val arrowLength = 20f
            val arrowWidth = 10f

            val tipX = targetPixel.x.toFloat()
            val tipY = targetPixel.y.toFloat()
            val base1X = tipX - arrowLength * kotlin.math.sin(angle - 0.4f).toFloat()
            val base1Y = tipY + arrowLength * kotlin.math.cos(angle - 0.4f).toFloat()
            val base2X = tipX - arrowLength * kotlin.math.sin(angle + 0.4f).toFloat()
            val base2Y = tipY + arrowLength * kotlin.math.cos(angle + 0.4f).toFloat()

            val arrowPath = Path().apply {
                moveTo(tipX, tipY)
                lineTo(base1X, base1Y)
                lineTo(base2X, base2Y)
                close()
            }
            canvas.drawPath(arrowPath, arrowPaint)
        }
    }
}