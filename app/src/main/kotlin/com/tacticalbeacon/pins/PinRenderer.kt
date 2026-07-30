package com.tacticalbeacon.pins

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.tacticalbeacon.R
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinColor
import com.tacticalbeacon.data.model.PinPriority
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

    fun getMarkerScale(pin: Pin, isSelected: Boolean): Float {
        val baseScale = when (pin.priority) {
            PinPriority.CRITICAL -> 1.22f
            PinPriority.HIGH -> 1.10f
            PinPriority.NORMAL -> 1.0f
            PinPriority.LOW -> 0.92f
        }
        val selectionBoost = if (isSelected) 0.06f else 0.0f
        return baseScale + selectionBoost
    }

    fun createTacticalBitmap(context: Context, pin: Pin, isSelected: Boolean = false): Bitmap {
        val scale = getMarkerScale(pin, isSelected)
        val size = (64 * scale).toInt().coerceAtLeast(48)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val outerRadius = size / 2f - 4f
        val innerRadius = outerRadius - 10f
        val accentColor = pinColorToArgb(pin.color)

        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(80, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(center + 2f, center + 2f, outerRadius, shadowPaint)

        val outerPaint = Paint().apply {
            this.color = accentColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, outerRadius, outerPaint)

        val borderPaint = Paint().apply {
            this.color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, outerRadius - 2f, borderPaint)

        val innerPaint = Paint().apply {
            this.color = android.graphics.Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, innerRadius, innerPaint)

        val iconDrawable = ContextCompat.getDrawable(context, PinIcons.getIconResource(pin.icon))?.mutate()
        iconDrawable?.setTint(android.graphics.Color.WHITE)
        val iconSize = (size * 0.36f).toInt().coerceAtLeast(18)
        val left = center - iconSize / 2f
        val top = center - iconSize / 2f
        iconDrawable?.setBounds(left.toInt(), top.toInt(), (left + iconSize).toInt(), (top + iconSize).toInt())
        iconDrawable?.draw(canvas)

        if (isSelected) {
            val highlightPaint = Paint().apply {
                color = android.graphics.Color.argb(180, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawCircle(center, center, outerRadius + 3f, highlightPaint)
        }

        return bitmap
    }

    fun pinColorToArgb(color: PinColor): Int {
        return android.graphics.Color.parseColor(color.hexColor)
    }
}