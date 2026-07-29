package com.tacticalbeacon.navigation

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Singleton

@Singleton
class ReliabilityManager @Inject constructor(
    private val context: Context
) {

    private val backupDir = File(context.filesDir, "backups")
    private val dbName = "tactical_beacon.db"

    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }

    fun createBackup(): File {
        val dbFile = File(context.filesDir.parent ?: context.filesDir.absolutePath, "databases/$dbName")
        val timestamp = System.currentTimeMillis()
        val backupFile = File(backupDir, "backup_$timestamp.db")

        try {
            if (dbFile.exists()) {
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: IOException) {
            // Log error
        }

        return backupFile
    }

    fun restoreBackup(backupFile: File): Boolean {
        val dbFile = File(context.filesDir.parent ?: context.filesDir.absolutePath, "databases/$dbName")
        return try {
            if (backupFile.exists()) {
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: IOException) {
            false
        }
    }

    fun getBackups(): List<File> {
        return backupDir.listFiles()?.filter { it.name.endsWith(".db") }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun deleteBackup(file: File) {
        file.delete()
    }

    fun checkDatabaseIntegrity(): IntegrityResult {
        val dbFile = File(context.filesDir.parent ?: context.filesDir.absolutePath, "databases/$dbName")
        return if (!dbFile.exists()) {
            IntegrityResult.MISSING
        } else {
            val size = dbFile.length()
            if (size == 0L) {
                IntegrityResult.CORRUPT
            } else {
                IntegrityResult.OK
            }
        }
    }

    enum class IntegrityResult {
        OK, CORRUPT, MISSING
    }
}