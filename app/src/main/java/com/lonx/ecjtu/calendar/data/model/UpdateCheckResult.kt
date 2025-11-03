package com.lonx.ecjtu.calendar.data.model

import com.lonx.ecjtu.calendar.data.dto.UpdateDTO
import java.io.File
import java.io.IOException

sealed class UpdateCheckResult {
    /** 成功：发现了新版本 */
    data class NewVersion(val info: UpdateDTO) : UpdateCheckResult()

    /** 成功：当前已是最新版本 */
    object NoUpdateAvailable : UpdateCheckResult()

    /** 失败：网络连接问题 */
    data class NetworkError(val exception: IOException) : UpdateCheckResult()

    /** 失败：网络请求超时 */
    object TimeoutError : UpdateCheckResult()

    /** 失败：GitHub API 返回了错误（如 404, 403） */
    data class ApiError(val code: Int, val message: String) : UpdateCheckResult()

    /** 失败：无法解析返回的 JSON 数据 */
    object ParsingError : UpdateCheckResult()
}

sealed class DownloadState {
    /** 空闲状态 */
    object Idle : DownloadState()
    /** 下载中状态，包含进度 */
    data class InProgress(val progress: Int) : DownloadState()
    /** 下载成功状态，包含APK文件 */
    data class Success(val file: File) : DownloadState()
    /** 下载失败状态，包含异常信息 */
    data class Error(val exception: Throwable) : DownloadState()
}