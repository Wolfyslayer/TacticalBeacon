package com.tacticalbeacon.overlays

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class CompassOverlay : Overlay() {

    private val compassPaint = Paint().apply {
        color = android.graphics.Color.argb(200, 107, 124, 58)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 30f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val cardinalPaint = Paint().apply {
        color = android.graphics.Color.argb(255, 211, 47, 47)
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val northArrowPaint = Paint().apply {
        color = android.graphics.Color.argb(255, 211, 47, 47)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var bearingDegrees: Float = 0f
    private var showCompass = true

    fun setBearing(bearing: Float) {
        bearingDegrees = bearing
    }

    fun setVisible(visible: Boolean) {
        showCompass = visible
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        if (!showCompass) return

        val size = kotlin.math.min(mapView.width, mapView.height) * 0.25f
        val centerX = mapView.width - size - 30f
        val centerY = size + 30f
        val radius = size * 0.5f

        // Outer ring
        canvas.drawCircle(centerX, centerY, radius, compassPaint)

        // Cardinal points
        val cardinals = listOf("N", "E", "S", "W")
        val cardinalAngles = listOf(0f, 90f, 180f, 270f)

        for (i in cardinals.indices) {
            val angle = Math.toRadians((cardinalAngles[i] - bearingDegrees).toDouble())
            val labelR = radius * 0.75f
            val x = centerX + labelR * kotlin.math.sin(angle).toFloat()
            val y = centerY - labelR * kotlin.math.cos(angle).toFloat()

            val paint = if (cardinals[i] == "N") cardinalPaint else labelPaint
            canvas.drawText(cardinals[i], x, y + paint.textSize / 3, paint)
        }

        // Intercardinal points
        val intercardinals = listOf("NE", "SE", "SW", "NW")
        val intercardinalAngles = listOf(45f, 135f, 225f, 315f)

        for (i in intercardinals.indices) {
            val angle = Math.toRadians((intercardinalAngles[i] - bearingDegrees).toDouble())
            val labelR = radius * 0.75f
            val x = centerX + labelR * kotlin.math.sin(angle).toFloat()
            val y = centerY - labelR * kotlin.math.cos(angle).toFloat()
            canvas.drawText(intercardinals[i], x, y + labelPaint.textSize / 3, labelPaint)
        }

        // North indicator triangle
        val northAngle = Math.toRadians((-bearingDegrees).toDouble())
        val tipX = centerX + (radius * 0.85f * kotlin.math.sin(northAngle)).toFloat()
        val tipY = centerY - (radius * 0.85f * kotlin.math.cos(northAngle)).toFloat()

        val triPath = android.graphics.Path().apply {
            moveTo(tipX, tipY)
            lineTo(
                centerX + 8f * kotlin.math.sin(northAngle - 0.5f).toFloat(),
                centerY - 8f * kotlin.math.cos(northAngle - 0.5f).toFloat()
            )
            lineTo(
                centerX + 8f * kotlin.math.sin(northAngle + 0.5f).toFloat(),
                centerY - 8f * kotlin.math.cos(northAngle + 0.5f).toFloat()
            )
            close()
        }
        canvas.drawPath(triPath, northArrowPaint)

        // Center dot
        canvas.drawCircle(centerX, centerY, 4f, northArrowPaint)
    }
}