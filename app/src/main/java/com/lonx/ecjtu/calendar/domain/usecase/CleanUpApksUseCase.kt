package com.lonx.ecjtu.calendar.domain.usecase

import android.content.Context
import android.util.Log
import com.lonx.ecjtu.calendar.BuildConfig
import com.lonx.ecjtu.calendar.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CleanUpApksUseCase {
    private val TAG = "CleanUpApksUseCase"

    /**
     * - 保留唯一一个版本号最高的、且比当前应用新的APK。
     * - 删除所有其他APK文件（包括旧版本和命名不规范的）。
     * - 删除所有残留的.tmp文件。
     * @param context 应用的 Context
     */
    suspend operator fun invoke(context: Context) = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir ?: return@withContext
            if (!cacheDir.exists() || !cacheDir.isDirectory) return@withContext

            val appName = context.getString(R.string.app_name)
            val currentVersion = BuildConfig.VERSION_NAME
            Log.i(TAG, "开始清理缓存目录，当前版本: $currentVersion")

            val allFiles = cacheDir.listFiles() ?: return@withContext

            val fileToKeep = allFiles
                .filter { it.isFile && it.name.startsWith(appName) && it.name.endsWith(".apk") }
                .mapNotNull { file ->
                    try {
                        val version = file.name.removePrefix(appName).removePrefix("-").removeSuffix(".apk")
                        if (isNewerVersion(currentVersion, version)) {
                            Pair(file, version)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                .maxByOrNull { it.second }
                ?.first

            if (fileToKeep != null) {
                Log.i(TAG, "决定保留最新版安装包: ${fileToKeep.name}")
            } else {
                Log.i(TAG, "没有需要保留的较新版本安装包。")
            }

            allFiles.forEach { file ->
                if (file == fileToKeep) {
                    return@forEach
                }

                if (file.name.endsWith(".apk") || file.name.endsWith(".apk.tmp")) {
                    if (file.delete()) {
                        Log.i(TAG, "已清理无效或过时的文件: ${file.name}")
                    } else {
                        Log.w(TAG, "清理文件失败: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理旧 APK 时发生意外错误", e)
        }
    }

    private fun isNewerVersion(version1: String, version2: String): Boolean {
        val v1 = version1.replace("[^0-9.]".toRegex(), "").split(".").map { it.toIntOrNull() ?: 0 }
        val v2 = version2.replace("[^0-9.]".toRegex(), "").split(".").map { it.toIntOrNull() ?: 0 }
        val size = maxOf(v1.size, v2.size)

        for (i in 0 until size) {
            val c1 = v1.getOrNull(i) ?: 0
            val c2 = v2.getOrNull(i) ?: 0
            if (c2 > c1) return true
            if (c2 < c1) return false
        }
        return false
    }
}