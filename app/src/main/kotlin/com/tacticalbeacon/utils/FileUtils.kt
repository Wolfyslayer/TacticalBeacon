package com.tacticalbeacon.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtils {

    fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (nameIndex >= 0) cursor.getString(nameIndex) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileSize(context: Context, uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                if (sizeIndex >= 0) cursor.getLong(sizeIndex) else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
