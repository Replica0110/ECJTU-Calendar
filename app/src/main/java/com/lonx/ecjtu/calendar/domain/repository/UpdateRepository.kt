package com.lonx.ecjtu.calendar.domain.repository

import com.lonx.ecjtu.calendar.data.model.UpdateCheckResult
import com.lonx.ecjtu.calendar.data.model.UpdateInfo
import com.lonx.ecjtu.calendar.data.model.DownloadState
import android.content.Context
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {
    suspend fun checkForUpdate(): UpdateCheckResult
    fun downloadUpdate(context: Context, info: UpdateInfo): Flow<DownloadState>
}