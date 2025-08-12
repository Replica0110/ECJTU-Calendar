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
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.net.toUri
import com.lonx.ecjtu.hjcalendar.R
import kotlinx.coroutines.currentCoroutineContext


class UpdateManager {

    private val client = OkHttpClient()
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
        data class Asset(val browser_download_url: String?)
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

            val latestVersion = release?.tag_name
            val downloadUrl = release?.assets?.firstOrNull { it.browser_download_url?.endsWith(".apk") == true }?.browser_download_url

            val releaseNotes = release?.body?.trim()?.ifBlank { "没有提供具体的更新说明。" } ?: "没有提供具体的更新说明。"

            if (latestVersion == null || downloadUrl == null) {
                Log.e(TAG, "Required fields (tag_name, browser_download_url) are missing.")
                return@withContext UpdateCheckResult.ParsingError
            }

            val currentVersion = BuildConfig.VERSION_NAME

            return@withContext if (isNewerVersion(currentVersion, latestVersion)) {
                UpdateCheckResult.NewVersion(UpdateInfo(latestVersion, downloadUrl, releaseNotes))
            } else {
                UpdateCheckResult.NoUpdateAvailable
            }

        } catch (e: IOException) {
            Log.e(TAG, "Network error during update check", e)
            return@withContext UpdateCheckResult.NetworkError(e)
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
        val apkFile = File(context.cacheDir, "${appName}-${info.versionName}.apk")

        if (apkFile.exists()) {
            Log.i(TAG, "找到了已下载的安装包: ${apkFile.name}")
            emit(DownloadState.Success(apkFile))
            return@flow // 结束 Flow，不再执行后续的网络请求
        }
        Log.i(TAG, "本地未找到安装包，开始从网络下载...")
        emit(DownloadState.InProgress(0))

        val request = Request.Builder().url(info.downloadUrl).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("下载失败: ${response.code}")
        }

        val body = response.body
        val totalBytes = body.contentLength()

        var bytesCopied: Long = 0
        val buffer = ByteArray(8 * 1024)
        var bytes = body.byteStream().read(buffer)

        FileOutputStream(apkFile).use { output ->
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

        emit(DownloadState.Success(apkFile))

    }.flowOn(Dispatchers.IO)

    private fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        // 移除版本号中的 'v' 或其他非数字前缀
        val current = currentVersion.replace("[^0-9.]".toRegex(), "").split(".").map { it.toIntOrNull() ?: 0 }
        val latest = latestVersion.replace("[^0-9.]".toRegex(), "").split(".").map { it.toIntOrNull() ?: 0 }
        val size = maxOf(current.size, latest.size)

        for (i in 0 until size) {
            val c = current.getOrNull(i) ?: 0
            val l = latest.getOrNull(i) ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
