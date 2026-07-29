package com.tacticalbeacon.navigation

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashLogger @Inject constructor(
    private val context: Context
) {

    private val logDir = File(context.filesDir, "crash_logs")
    private val logQueue = ConcurrentLinkedQueue<String>()
    private var isLogging = false

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
    }

    fun logCrash(throwable: Throwable) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val logFile = File(logDir, "crash_$timestamp.log")

        try {
            FileWriter(logFile).use { writer ->
                writer.write("=== CRASH LOG ===\n")
                writer.write("Timestamp: ${Date()}\n")
                writer.write("Exception: ${throwable.javaClass.name}\n")
                writer.write("Message: ${throwable.message}\n")
                writer.write("Stack Trace:\n")
                throwable.printStackTrace(writer)
                writer.write("\n=== END CRASH LOG ===\n")
            }
        } catch (e: IOException) {
            // Failed to write crash log
        }
    }

    fun logEvent(event: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        logQueue.add("[$timestamp] $event")

        if (logQueue.size > 1000) {
            logQueue.poll()
        }
    }

    fun getRecentLogs(count: Int = 50): List<String> {
        return logQueue.toList().takeLast(count)
    }

    fun clearLogs() {
        logQueue.clear()
        logDir.listFiles()?.forEach { it.delete() }
    }
}