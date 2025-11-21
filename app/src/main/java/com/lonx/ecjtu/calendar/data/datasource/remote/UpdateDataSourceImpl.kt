package com.lonx.ecjtu.calendar.data.datasource.remote

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lonx.ecjtu.calendar.BuildConfig
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.data.dto.GitHubReleaseDTO
import com.lonx.ecjtu.calendar.data.dto.OutputMetadataDTO
import com.lonx.ecjtu.calendar.data.dto.UpdateDTO
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.data.model.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import rxhttp.wrapper.param.RxHttp
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException

class UpdateDataSourceImpl: UpdateDataSource {
    private val gson = Gson()
    private val GITHUB_API_URL = "https://api.github.com/repos/Replica0110/ECJTU-Calendar/releases/latest"
    private val TAG = "UpdateDataSourceImpl"

    override suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val response = RxHttp.get(GITHUB_API_URL)
                .execute()

            if (!response.isSuccessful) {
                return@withContext UpdateCheckResult.ApiError(response.code, response.message)
            }

            val responseBody = response.body?.string()
            val release: GitHubReleaseDTO? = try {
                gson.fromJson(responseBody, GitHubReleaseDTO::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parsing failed", e)
                return@withContext UpdateCheckResult.ParsingError
            }

            val downloadUrl =
                release?.assetDTOS?.firstOrNull { it.browserDownloadUrl?.endsWith(".apk") == true }?.browserDownloadUrl
            val metadataUrl =
                release?.assetDTOS?.firstOrNull { it.browserDownloadUrl?.endsWith(".json") == true }?.browserDownloadUrl

            val size = release?.assetDTOS?.firstOrNull()?.size

            val releaseNotes = release?.body?.trim()?.ifBlank { "没有提供具体的更新说明。" } ?: "没有提供具体的更新说明。"

            if (downloadUrl == null || metadataUrl == null || size == null) {
                Log.e(TAG, "Required assets (APK or metadata) are missing.")
                return@withContext UpdateCheckResult.ParsingError
            }

            // 获取 metadata.json
            val metadataResponse = RxHttp.get(metadataUrl).execute()

            if (!metadataResponse.isSuccessful) {
                return@withContext UpdateCheckResult.ApiError(metadataResponse.code, metadataResponse.message)
            }

            val metadata: OutputMetadataDTO = try {
                gson.fromJson(metadataResponse.body?.string(), OutputMetadataDTO::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Metadata JSON parsing failed", e)
                return@withContext UpdateCheckResult.ParsingError
            }

            val latestVersionCode = metadata.elementsDTO.firstOrNull()?.versionCode
            val latestVersionName = metadata.elementsDTO.firstOrNull()?.versionName

            if (latestVersionCode == null || latestVersionName == null) {
                Log.e(TAG, "Version information is missing in metadata.")
                return@withContext UpdateCheckResult.ParsingError
            }

            val currentVersionCode = BuildConfig.VERSION_CODE

            return@withContext if (latestVersionCode > currentVersionCode) {
                UpdateCheckResult.NewVersion(
                    UpdateDTO(
                        versionName = latestVersionName,
                        downloadUrl = downloadUrl,
                        releaseNotes = releaseNotes,
                        size = size,
                    )
                )
            } else {
                UpdateCheckResult.NoUpdateAvailable
            }

        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException -> {
                    Log.e(TAG, "Connection timed out", e)
                    return@withContext UpdateCheckResult.TimeoutError
                }
                is IOException -> {
                    Log.e(TAG, "Network error during update check", e)
                    return@withContext UpdateCheckResult.NetworkError(e)
                }
                else -> {
                    Log.e(TAG, "Unexpected error during update check", e)
                    throw e
                }
            }
        }
    }
    override fun downloadUpdate(context: Context, info:UpdateDTO): Flow<DownloadState> = flow {
        val appName = context.getString(R.string.app_name)
        val finalFile = File(context.cacheDir, "${appName}-${info.versionName}.apk")
        val tempFile = File(context.cacheDir, "${appName}-${info.versionName}.apk.tmp")

        if (finalFile.exists()) {
            emit(DownloadState.Success(finalFile))
            return@flow
        }

        try {
            val response = RxHttp.get(info.downloadUrl).execute()
            if (!response.isSuccessful) throw IOException("下载失败: ${response.code}")

            val body = response.body ?: throw IOException("响应体为空")
            val totalBytes = body.contentLength()
            val inputStream: InputStream = body.byteStream()

            var bytesCopied: Long = 0
            var lastEmittedProgress = -1

            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    currentCoroutineContext().ensureActive()
                    output.write(buffer, 0, bytesRead)
                    bytesCopied += bytesRead

                    if (totalBytes > 0) {
                        val progress = (100.0f * bytesCopied / totalBytes).toInt()

                        if (progress > lastEmittedProgress) {
                            emit(DownloadState.InProgress(progress))
                            lastEmittedProgress = progress
                        }
                    }
                }
            }

            if (!tempFile.renameTo(finalFile)) {
                throw IOException("重命名临时文件失败")
            }

            emit(DownloadState.Success(finalFile))

        } catch (e: Exception) {
            // 确保任何失败都会清理临时文件
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e // 将异常重新抛出给 ViewModel 处理
        }

    }.flowOn(Dispatchers.IO)
}