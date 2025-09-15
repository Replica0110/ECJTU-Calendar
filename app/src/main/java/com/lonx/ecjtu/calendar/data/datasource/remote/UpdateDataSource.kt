package com.lonx.ecjtu.calendar.data.datasource.remote

import android.content.Context
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.data.model.UpdateCheckResult
import com.lonx.ecjtu.calendar.data.model.UpdateInfo
import kotlinx.coroutines.flow.Flow

interface UpdateDataSource {
    suspend fun checkForUpdate(): UpdateCheckResult
    fun downloadUpdate(context: Context, info: UpdateInfo) : Flow<DownloadState>
}