package com.tacticalbeacon.pins

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinColor
import com.tacticalbeacon.data.model.PinIcon
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import javax.inject.Singleton

@Singleton
class PinRenderer {

    fun createMarker(mapView: MapView, pin: Pin): Marker {
        val marker = Marker(mapView).apply {
            position = GeoPoint(pin.latitude, pin.longitude)
            title = pin.name
            snippet = pin.notes.ifBlank { null }
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ -> true }
        }
        return marker
    }

    fun createTacticalBitmap(pin: Pin): Bitmap {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val color = pinColorToArgb(pin.color)

        // Outer circle
        val outerPaint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, outerPaint)

        // Inner circle
        val innerPaint = Paint().apply {
            this.color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 4f, innerPaint)

        // Center dot
        val centerPaint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 8f, centerPaint)

        return bitmap
    }

    fun pinColorToArgb(color: PinColor): Int {
        return android.graphics.Color.parseColor(color.hexColor)
    }
}