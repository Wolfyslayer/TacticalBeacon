package com.tacticalbeacon.overlays

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class UserLocationOverlay(ctx: android.content.Context, mapView: MapView) : Overlay() {

    private val myLocationOverlay = MyLocationNewOverlay(
        GpsMyLocationProvider(ctx),
        mapView
    )

    private val accuracyPaint = Paint().apply {
        color = android.graphics.Color.argb(80, 107, 124, 58)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = android.graphics.Color.argb(200, 107, 124, 58)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val centerPaint = Paint().apply {
        color = android.graphics.Color.argb(255, 107, 124, 58)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val headingPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
        strokeWidth = 2f
    }

    private val shadowPaint = Paint().apply {
        color = android.graphics.Color.argb(100, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var currentAccuracy = 0f
    private var currentBearing = 0f
    private var isMoving = false
    private var pulsePhase = 0f

    init {
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
    }

    fun setAccuracy(accuracy: Float) {
        currentAccuracy = accuracy
    }

    fun setBearing(bearing: Float) {
        currentBearing = bearing
    }

    fun setMoving(moving: Boolean) {
        isMoving = moving
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        if (!myLocationOverlay.isMyLocationEnabled) return

        val location = myLocationOverlay.myLocation
        if (location == null) return

        val projection = mapView.projection
        val screenCoords = projection.toPixels(location, null)

        pulsePhase += 0.05f
        val pulseScale = 1f + 0.1f * kotlin.math.sin(pulsePhase.toDouble()).toFloat()

        // Accuracy ring
        val accuracyRadius = (currentAccuracy * 50f * pulseScale).coerceAtLeast(10f)
        canvas.drawCircle(
            screenCoords.x.toFloat(),
            screenCoords.y.toFloat(),
            accuracyRadius,
            accuracyPaint
        )
        canvas.drawCircle(
            screenCoords.x.toFloat(),
            screenCoords.y.toFloat(),
            accuracyRadius,
            borderPaint
        )

        // Shadow
        canvas.drawCircle(
            screenCoords.x.toFloat() + 3f,
            screenCoords.y.toFloat() + 3f,
            18f * pulseScale,
            shadowPaint
        )

        // Center dot
        canvas.drawCircle(
            screenCoords.x.toFloat(),
            screenCoords.y.toFloat(),
            8f * pulseScale,
            centerPaint
        )

        // Heading arrow
        if (isMoving && currentBearing != 0f) {
            val bearingRad = Math.toRadians(currentBearing.toDouble())
            val arrowLength = 30f * pulseScale
            val tipX = screenCoords.x + (arrowLength * kotlin.math.sin(bearingRad)).toFloat()
            val tipY = screenCoords.y - (arrowLength * kotlin.math.cos(bearingRad)).toFloat()

            val leftAngle = bearingRad + Math.PI * 0.6
            val rightAngle = bearingRad - Math.PI * 0.6
            val baseLength = 12f * pulseScale

            val baseLeftX = screenCoords.x + (baseLength * kotlin.math.sin(leftAngle)).toFloat()
            val baseLeftY = screenCoords.y - (baseLength * kotlin.math.cos(leftAngle)).toFloat()
            val baseRightX = screenCoords.x + (baseLength * kotlin.math.sin(rightAngle)).toFloat()
            val baseRightY = screenCoords.y - (baseLength * kotlin.math.cos(rightAngle)).toFloat()

            val path = android.graphics.Path().apply {
                moveTo(tipX, tipY)
                lineTo(baseLeftX, baseLeftY)
                lineTo(baseRightX, baseRightY)
                close()
            }
            canvas.drawPath(path, headingPaint)
        }
    }

    fun getMyLocationOverlay(): MyLocationNewOverlay = myLocationOverlay
}