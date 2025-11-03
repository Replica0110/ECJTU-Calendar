package com.lonx.ecjtu.calendar.data.repository

import android.content.Context
import com.lonx.ecjtu.calendar.data.datasource.remote.UpdateDataSource
import com.lonx.ecjtu.calendar.data.model.UpdateCheckResult
import com.lonx.ecjtu.calendar.data.dto.UpdateDTO
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow

class UpdateRepositoryImpl(
    private val updateDataSource: UpdateDataSource
) : UpdateRepository {

    override suspend fun checkForUpdate(): UpdateCheckResult {
        // 目前只是转发，未来考虑添加缓存逻辑
        return updateDataSource.checkForUpdate()
    }

    override fun downloadUpdate(context: Context, info: UpdateDTO): Flow<DownloadState> {
        // 转发下载请求
        return updateDataSource.downloadUpdate(context, info)
    }
}