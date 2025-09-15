package com.lonx.ecjtu.calendar.domain.usecase

import android.content.Context
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.data.model.UpdateInfo
import com.lonx.ecjtu.calendar.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow

class DownloadUpdateUseCase(
    private val repository: UpdateRepository
) {
    suspend operator fun invoke(context: Context, info: UpdateInfo): Flow<DownloadState> {
        return repository.downloadUpdate(context, info)
    }
}