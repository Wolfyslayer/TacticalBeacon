package com.tacticalbeacon.overlays

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Marker

class PinOverlay(private val mapView: MapView) : Overlay() {

    private val pinPaints = mutableMapOf<String, Paint>()
    private val labelPaints = mutableMapOf<String, Paint>()

    private val defaultPinPaint = Paint().apply {
        color = android.graphics.Color.rgb(0x6B, 0x7C, 0x3A)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val defaultLabelPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 36f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
    }

    fun addPin(geoPoint: GeoPoint, label: String, pinColor: PinColor) {
        val paint = Paint(defaultPinPaint).apply {
            color = pinColor.toArgb()
        }
        val labelPaint = Paint(defaultLabelPaint)
        pinPaints[label] = paint
        labelPaints[label] = labelPaint
    }

    fun removePin(label: String) {
        pinPaints.remove(label)
        labelPaints.remove(label)
    }

    fun clear() {
        pinPaints.clear()
        labelPaints.clear()
    }
}

enum class PinColor(val label: String, val hexColor: String) {
    OLIVE("Olive", "#6B7C3A"),
    RED("Red", "#D32F2F"),
    AMBER("Amber", "#F57F17"),
    BLUE("Blue", "#1565C0"),
    WHITE("White", "#FAFAFA"),
    CYAN("Cyan", "#00838F"),
    PURPLE("Purple", "#6A1B9A"),
    ORANGE("Orange", "#E65100");

    fun toArgb(): Int {
        return android.graphics.Color.parseColor(hexColor)
    }
}