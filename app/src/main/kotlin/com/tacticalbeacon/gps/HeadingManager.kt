package com.tacticalbeacon.gps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeadingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _hasCompass = MutableStateFlow(false)
    val hasCompass: StateFlow<Boolean> = _hasCompass.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val alpha = 0.1f
    private var currentAzimuth = 0f

    init {
        _hasCompass.value = (accelerometerSensor != null && magnetometerSensor != null) ||
                rotationVectorSensor != null
    }

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magnetometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthRad = orientation[0]
                val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                val normalizedAzimuth = (azimuthDeg + 360f) % 360f
                updateAzimuth(normalizedAzimuth)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                lowPassFilter(event.values, gravity)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lowPassFilter(event.values, geomagnetic)
                val success = SensorManager.getRotationMatrix(
                    rotationMatrix, inclinationMatrix, gravity, geomagnetic
                )
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthRad = orientation[0]
                    val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                    val normalizedAzimuth = (azimuthDeg + 360f) % 360f
                    updateAzimuth(normalizedAzimuth)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateAzimuth(newAzimuth: Float) {
        var diff = newAzimuth - currentAzimuth
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        currentAzimuth = (currentAzimuth + alpha * diff + 360f) % 360f
        _azimuth.value = currentAzimuth
    }

    private fun lowPassFilter(input: FloatArray, output: FloatArray) {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }
}