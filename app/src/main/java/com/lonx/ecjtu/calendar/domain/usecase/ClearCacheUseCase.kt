package com.lonx.ecjtu.calendar.domain.usecase

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ClearCacheUseCase {

    /**
     * Clears the application's cache directory.
     * @param context The application context.
     * @return A Result containing the number of bytes freed, or an exception on failure.
     */
    suspend operator fun invoke(context: Context): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            if (cacheDir == null || !cacheDir.exists()) {
                return@withContext Result.success(0L)
            }

            val freedBytes = cacheDir.walk().map { it.length() }.sum()

            if (cacheDir.deleteRecursively()) {
                Result.success(freedBytes)
            } else {
                Result.failure(IOException("未能完全删除缓存目录"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}