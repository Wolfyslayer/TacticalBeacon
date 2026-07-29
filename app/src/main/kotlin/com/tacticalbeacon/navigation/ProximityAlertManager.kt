package com.tacticalbeacon.navigation

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tacticalbeacon.data.model.getAlertIntervalMs
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProximityAlertManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var alertJob: Job? = null
    private var toneGenerator: ToneGenerator? = null
    private var currentDistance = Double.MAX_VALUE
    private var isActive = false
    private var alertVolume = 0.8f
    private var vibrationStrength = 3

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(volume: Float = 0.8f, vibStrength: Int = 3) {
        alertVolume = volume
        vibrationStrength = vibStrength
        isActive = true
        initToneGenerator()
        startAlertLoop()
    }

    fun stop() {
        isActive = false
        alertJob?.cancel()
        alertJob = null
        vibrator.cancel()
        toneGenerator?.release()
        toneGenerator = null
    }

    fun updateDistance(distanceMeters: Double) {
        currentDistance = distanceMeters
    }

    fun updateSettings(volume: Float, vibStrength: Int) {
        alertVolume = volume
        vibrationStrength = vibStrength
        if (isActive) {
            toneGenerator?.release()
            initToneGenerator()
        }
    }

    private fun initToneGenerator() {
        try {
            val volumePercent = (alertVolume * 100).toInt().coerceIn(0, 100)
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, volumePercent)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    private fun startAlertLoop() {
        alertJob?.cancel()
        alertJob = scope.launch {
            var lastAlertTime = 0L

            while (isActive) {
                val distance = currentDistance
                val intervalMs = getAlertIntervalMs(distance)

                if (intervalMs == null) {
                    // Silent zone — just wait and check again
                    delay(500L)
                    lastAlertTime = 0L
                    continue
                }

                val now = System.currentTimeMillis()

                if (intervalMs == 0L) {
                    // Continuous alert — arrived at destination
                    triggerAlert(distance, continuous = true)
                    delay(300L)
                    continue
                }

                if (now - lastAlertTime >= intervalMs) {
                    triggerAlert(distance, continuous = false)
                    lastAlertTime = now
                }

                delay(50L) // Poll at 20Hz for responsiveness
            }
        }
    }

    private fun triggerAlert(distanceMeters: Double, continuous: Boolean) {
        val ringerMode = audioManager.ringerMode

        when {
            ringerMode == AudioManager.RINGER_MODE_NORMAL && !continuous -> {
                // Beep mode
                playBeep(distanceMeters)
            }
            ringerMode == AudioManager.RINGER_MODE_NORMAL && continuous -> {
                // Continuous tone
                playContinuousTone()
            }
            else -> {
                // Vibrate mode (silent or vibrate ringer)
                vibrate(distanceMeters, continuous)
            }
        }
    }

    private fun playBeep(distanceMeters: Double) {
        val tone = when {
            distanceMeters > 100.0 -> ToneGenerator.TONE_PROP_BEEP
            distanceMeters > 20.0  -> ToneGenerator.TONE_PROP_BEEP2
            distanceMeters > 5.0   -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            else                   -> ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
        }
        val duration = when {
            distanceMeters > 100.0 -> 150
            distanceMeters > 20.0  -> 100
            else                   -> 80
        }
        try {
            toneGenerator?.startTone(tone, duration)
        } catch (e: Exception) {
            // Reinitialize if tone generator failed
            initToneGenerator()
        }
    }

    private fun playContinuousTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 250)
        } catch (e: Exception) {
            initToneGenerator()
        }
    }

    private fun vibrate(distanceMeters: Double, continuous: Boolean) {
        val amplitude = when (vibrationStrength) {
            1 -> 50
            2 -> 100
            3 -> 150
            4 -> 200
            5 -> 255
            else -> 150
        }.coerceIn(1, 255)

        if (continuous) {
            // Continuous vibration pattern
            val pattern = longArrayOf(0, 200, 100, 200, 100, 200)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitudes = intArrayOf(0, amplitude, 0, amplitude, 0, amplitude)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } else {
            // Single pulse, duration based on distance
            val duration = when {
                distanceMeters > 100.0 -> 200L
                distanceMeters > 20.0  -> 150L
                distanceMeters > 5.0   -> 100L
                else                   -> 80L
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
    }

    fun isRunning() = isActive
}
