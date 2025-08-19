package com.lonx.ecjtu.hjcalendar.logic

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.ensureActive
import android.util.Log
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import androidx.core.net.toUri
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.NetworkModule
import kotlinx.coroutines.currentCoroutineContext


class UpdateManager {

    private val client = NetworkModule.okHttpClient
    private val gson = Gson()
    private val GITHUB_API_URL = "https://api.github.com/repos/Replica0110/ECJTU-Calendar/releases/latest"
    private val TAG = "UpdateManager"

    data class UpdateInfo(
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String
    )

    data class GitHubRelease(
        val tag_name: String?,
        val body: String?,
        val assets: List<Asset>?
    ) {
        data class Asset(
            val browser_download_url: String?,
            val name: String?
        )
    }

    data class OutputMetadata(
        val version: Int,
        val elements: List<Element>
    ) {
        data class Element(
            val versionCode: Int,
            val versionName: String
        )
    }

    /**
     * 检查新版本，返回一个包含所有可能结果的 UpdateCheckResult。
     */
    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(GITHUB_API_URL).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext UpdateCheckResult.ApiError(response.code, response.message)
            }

            val responseBody = response.body.string()
            val release: GitHubRelease? = try {
                gson.fromJson(responseBody, GitHubRelease::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parsing failed", e)
                return@withContext UpdateCheckResult.ParsingError
            }

            val downloadUrl = release?.assets?.firstOrNull { it.browser_download_url?.endsWith(".apk") == true }?.browser_download_url
            val metadataUrl = release?.assets?.firstOrNull { it.browser_download_url?.endsWith(".json") == true }?.browser_download_url

            val releaseNotes = release?.body?.trim()?.ifBlank { "没有提供具体的更新说明。" } ?: "没有提供具体的更新说明。"

            if (downloadUrl == null || metadataUrl == null) {
                Log.e(TAG, "Required assets (APK or metadata) are missing.")
                return@withContext UpdateCheckResult.ParsingError
            }

            // 获取 metadata.json
            val metadataRequest = Request.Builder().url(metadataUrl).build()
            val metadataResponse = client.newCall(metadataRequest).execute()
            
            if (!metadataResponse.isSuccessful) {
                return@withContext UpdateCheckResult.ApiError(metadataResponse.code, metadataResponse.message)
            }

            val metadata: OutputMetadata = try {
                gson.fromJson(metadataResponse.body.string(), OutputMetadata::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Metadata JSON parsing failed", e)
                return@withContext UpdateCheckResult.ParsingError
            }

            val latestVersionCode = metadata.elements.firstOrNull()?.versionCode
            val latestVersionName = metadata.elements.firstOrNull()?.versionName

            if (latestVersionCode == null || latestVersionName == null) {
                Log.e(TAG, "Version information is missing in metadata.")
                return@withContext UpdateCheckResult.ParsingError
            }

            val currentVersionCode = BuildConfig.VERSION_CODE

            return@withContext if (latestVersionCode > currentVersionCode) {
                UpdateCheckResult.NewVersion(UpdateInfo(latestVersionName, downloadUrl, releaseNotes))
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

    fun installApk(context: Context, apkFile: File) {
        val authority = "${context.packageName}.provider"
        val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 检查安装未知来源应用的权限（Android 8.0+）
        if (!context.packageManager.canRequestPackageInstalls()) {
            // 跳转到设置页面请求权限
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData("package:${context.packageName}".toUri())
            context.startActivity(settingsIntent)
            // 提示用户开启权限后再试
            Log.w(TAG, "用户需要开启“安装未知应用”的权限")
            return
        }

        context.startActivity(intent)
    }


    fun downloadUpdate(context: Context, info: UpdateInfo): Flow<DownloadState> = flow {

        val appName = context.getString(R.string.app_name)
        val finalFile = File(context.cacheDir, "${appName}-${info.versionName}.apk")
        val tempFile = File(context.cacheDir, "${appName}-${info.versionName}.apk.tmp")


        if (finalFile.exists()) {
            Log.i(TAG, "找到了已下载的安装包: ${finalFile.name}")
            emit(DownloadState.Success(finalFile))
            return@flow
        }
        try {
            // 如果旧的临时文件存在，先删除它
            if (tempFile.exists()) {
                tempFile.delete()
            }

            Log.i(TAG, "开始下载，临时文件为: ${tempFile.name}")
            emit(DownloadState.InProgress(0))

            val request = Request.Builder().url(info.downloadUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("下载失败: ${response.code}")
            val body = response.body
            val totalBytes = body.contentLength()

            var bytesCopied: Long = 0
            val buffer = ByteArray(8 * 1024)
            var bytes = body.byteStream().read(buffer)

            // 所有写入操作都针对临时文件
            FileOutputStream(tempFile).use { output ->
                while (bytes >= 0) {
                    currentCoroutineContext().ensureActive()
                    output.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    if (totalBytes > 0) {
                        val progress = (100 * bytesCopied / totalBytes).toInt()
                        emit(DownloadState.InProgress(progress))
                    }
                    bytes = body.byteStream().read(buffer)
                }
            }

            // 下载成功后，将临时文件重命名为最终文件
            if (!tempFile.renameTo(finalFile)) {
                throw IOException("重命名临时文件失败")
            }

            emit(DownloadState.Success(finalFile))

        } catch (e: Exception) {
            // 捕获任何异常（包括取消），并删除临时文件
            if (tempFile.exists()) {
                tempFile.delete()
                Log.i(TAG, "下载被取消或失败，已删除临时文件: ${tempFile.name}")
            }
            // 将异常重新抛出，让 ViewModel 处理
            throw e
        }

    }.flowOn(Dispatchers.IO)

}
