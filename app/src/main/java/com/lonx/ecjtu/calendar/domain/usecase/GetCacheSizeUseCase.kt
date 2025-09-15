package com.lonx.ecjtu.calendar.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCacheSizeUseCase {

    /**
     * Calculates the size of the application's cache directory and returns it as a formatted string.
     * @param context The application context.
     * @return A formatted string like "12.34 MB" or "计算失败".
     */
    @SuppressLint("DefaultLocale")
    suspend operator fun invoke(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir ?: return@withContext "0.00 MB"
            if (!cacheDir.exists()) return@withContext "0.00 MB"

            val totalBytes = cacheDir.walk().map { it.length() }.sum()

            if (totalBytes == 0L) {
                "0.00 MB"
            } else {
                val sizeInMB = totalBytes / (1024.0 * 1024.0)
                String.format("%.2f MB", sizeInMB)
            }
        } catch (e: Exception) {
            // Log the exception in a real app
            "计算失败"
        }
    }
}