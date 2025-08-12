package com.lonx.ecjtu.hjcalendar.logic

import android.content.Context
import android.util.Log
import com.lonx.ecjtu.hjcalendar.BuildConfig
import com.lonx.ecjtu.hjcalendar.R
import com.tencent.mmkv.MMKV

object DataStoreManager {

    private const val KEY_WEIXIN_ID = "key_weixin_id"
    private const val KEY_CHECK_UPDATE_ON_START = "key_check_update_on_start"

    private const val KEY_NO_COURSE_TEXT = "key_no_course_text"

    private val mmkv = MMKV.defaultMMKV()

    fun saveWeiXinId(id: String) {
        mmkv.encode(KEY_WEIXIN_ID, id)
    }

    fun getWeiXinId(): String {
        return mmkv.decodeString(KEY_WEIXIN_ID, "") ?: ""
    }

    fun setUpdateCheckOnStart(enabled: Boolean) {
        mmkv.encode(KEY_CHECK_UPDATE_ON_START, enabled)
    }

    fun isUpdateCheckOnStartEnabled(): Boolean {
        return mmkv.decodeBool(KEY_CHECK_UPDATE_ON_START, true)
    }

    fun saveNoCourseText(text: String) {
        mmkv.encode(KEY_NO_COURSE_TEXT, text)
    }

    fun getNoCourseText(defaultValue: String): String {
        return mmkv.decodeString(KEY_NO_COURSE_TEXT, defaultValue) ?: defaultValue
    }
    /**
     * - 保留唯一一个版本号最高的、且比当前应用新的APK。
     * - 删除所有其他APK文件（包括旧版本和命名不规范的）。
     * - 删除所有残留的.tmp文件。
     */
    fun cleanUpOldApks(context: Context) {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists() || !cacheDir.isDirectory) return

        val appName = context.getString(R.string.app_name)
        val currentVersion = BuildConfig.VERSION_NAME
        Log.i("DataStoreManager", "开始清理缓存目录，当前版本: $currentVersion")

        val allFiles = cacheDir.listFiles() ?: return

        // 找到唯一应该被保留的文件
        val fileToKeep = allFiles
            .filter { it.isFile && it.name.startsWith(appName) && it.name.endsWith(".apk") }
            .mapNotNull { file ->
                // 尝试从文件名解析版本号
                try {
                    val version = file.name.removePrefix(appName).removePrefix("-").removeSuffix(".apk")
                    // 只有版本比当前新的APK才被保留
                    if (isNewerVersion(currentVersion, version)) {
                        Pair(file, version)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null // 解析失败的文件不是我们的目标
                }
            }
            .maxByOrNull { it.second } // 找到版本号最高的那个
            ?.first // 只取它的 File 对象

        if (fileToKeep != null) {
            Log.i("DataStoreManager", "决定保留最新版安装包: ${fileToKeep.name}")
        } else {
            Log.i("DataStoreManager", "没有需要保留的较新版本安装包。")
        }

        allFiles.forEach { file ->
            // 如果这个文件就是我们要保留的那个，就跳过
            if (file == fileToKeep) {
                return@forEach
            }

            // 否则，只要是 .apk 或 .apk.tmp 文件，就删除
            if (file.name.endsWith(".apk") || file.name.endsWith(".apk.tmp")) {
                if (file.delete()) {
                    Log.i("DataStoreManager", "已清理无效或过时的文件: ${file.name}")
                }
            }
        }
    }

    /**
     * 比较版本号的辅助函数。
     * @return 如果 version2 > version1 则返回 true。
     */
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
        return false // 版本号相等
    }
}