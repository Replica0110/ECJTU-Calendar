package com.lonx.ecjtu.hjcalendar.logic

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.core.net.toUri
import com.google.gson.JsonSyntaxException
import com.lonx.ecjtu.hjcalendar.R
import java.io.IOException


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

    /**
     * 使用系统的 DownloadManager 下载 APK。
     */
    fun downloadUpdate(context: Context, info: UpdateInfo) {
        val request = DownloadManager.Request(info.downloadUrl.toUri())
            .setTitle("正在下载 ${context.getString(R.string.app_name)} ${info.versionName}")
            .setDescription("下载完成后将提示安装")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${context.getString(R.string.app_name)}-${info.versionName}.apk")
            .setMimeType("application/vnd.android.package-archive")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

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
