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
     * 清理缓存目录中所有版本号小于或等于当前应用版本的 APK 文件。
     */
    fun cleanUpOldApks(context: Context) {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists() || !cacheDir.isDirectory) return

        val appName = context.getString(R.string.app_name)
        val currentVersion = BuildConfig.VERSION_NAME

        Log.i("DataStoreManager", "开始清理旧的APK文件，当前版本: $currentVersion")

        cacheDir.listFiles()?.forEach { file ->
            // 检查文件名是否符合我们的命名规则
            if (file.isFile && file.name.startsWith(appName) && file.name.endsWith(".apk")) {
                try {
                    val fileVersion = file.name
                        .removePrefix(appName)
                        .removePrefix("-")
                        .removeSuffix(".apk")

                    // 如果文件版本不比当前版本新，就删除它
                    if (!isNewerVersion(currentVersion, fileVersion)) {
                        if (file.delete()) {
                            Log.i("DataStoreManager", "已删除过时的安装包: ${file.name}")
                        }
                    } else {
                        Log.i("DataStoreManager", "保留较新的安装包: ${file.name}")
                    }
                } catch (e: Exception) {
                    Log.e("DataStoreManager", "处理文件 ${file.name} 时出错", e)
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