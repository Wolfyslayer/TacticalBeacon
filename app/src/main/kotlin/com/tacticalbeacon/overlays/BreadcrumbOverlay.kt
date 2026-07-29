package com.tacticalbeacon.overlays

import android.graphics.Canvas
import android.graphics.Paint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class BreadcrumbOverlay : Overlay() {

    private val breadcrumbPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 107, 124, 58)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val breadcrumbDotPaint = Paint().apply {
        color = android.graphics.Color.argb(200, 107, 124, 58)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var breadcrumbs: List<GeoPoint> = emptyList()

    fun setBreadcrumbs(points: List<GeoPoint>) {
        breadcrumbs = points
    }

    fun clear() {
        breadcrumbs = emptyList()
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        if (breadcrumbs.size < 2) return

        val projection = mapView.projection

        // Draw breadcrumb trail
        for (i in 1 until breadcrumbs.size) {
            val prev = projection.toPixels(breadcrumbs[i - 1], null)
            val curr = projection.toPixels(breadcrumbs[i], null)
            canvas.drawLine(
                prev.x.toFloat(), prev.y.toFloat(),
                curr.x.toFloat(), curr.y.toFloat(),
                breadcrumbPaint
            )
        }

        // Draw dots at each breadcrumb point
        for (point in breadcrumbs) {
            val pixel = projection.toPixels(point, null)
            canvas.drawCircle(pixel.x.toFloat(), pixel.y.toFloat(), 3f, breadcrumbDotPaint)
        }
    }
}