package com.tacticalbeacon.location

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tacticalbeacon.MainActivity
import com.tacticalbeacon.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var locationManager: LocationManager

    companion object {
        const val CHANNEL_ID = "tactical_beacon_location"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.tacticalbeacon.START_TRACKING"
        const val ACTION_STOP = "com.tacticalbeacon.STOP_TRACKING"
        const val EXTRA_INTERVAL = "interval_ms"
        const val EXTRA_BATTERY_SAVER = "battery_saver"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val intervalMs = intent.getLongExtra(EXTRA_INTERVAL, 1000L)
                val batterySaver = intent.getBooleanExtra(EXTRA_BATTERY_SAVER, false)
                startForeground(NOTIFICATION_ID, buildNotification())
                locationManager.startTracking(intervalMs, batterySaver)
            }
            ACTION_STOP -> {
                locationManager.stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        locationManager.stopTracking()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, LocationForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tactical Beacon")
            .setContentText("GPS tracking active")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active GPS tracking for navigation"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
