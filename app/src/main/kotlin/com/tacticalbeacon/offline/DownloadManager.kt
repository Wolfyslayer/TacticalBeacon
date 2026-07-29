package com.tacticalbeacon.offline

import android.content.Context

class DownloadManager(private val context: Context) {

    interface DownloadCallback {
        fun onProgress(progress: Int, total: Int)
        fun onComplete()
        fun onError(message: String)
    }

    private var isDownloading = false

    fun isDownloading(): Boolean = isDownloading

    fun cancelDownload() {
        isDownloading = false
    }

    fun downloadRegion(
        region: DownloadRegion,
        callback: DownloadCallback
    ) {
        isDownloading = true
        // Delegate to OfflineMapManager for actual tile download
        // This is a placeholder for the download coordination logic
        callback.onProgress(0, 100)
        callback.onComplete()
        isDownloading = false
    }
}