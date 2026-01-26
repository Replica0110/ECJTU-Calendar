package com.lonx.ecjtu.calendar.util

import android.util.Log
import com.lonx.ecjtu.calendar.BuildConfig

/**
 * 统一日志工具类
 *
 * 功能特性：
 * - 仅在 BuildConfig.DEBUG 时输出日志
 * - 支持不同日志级别（DEBUG、INFO、WARN、ERROR）
 * - 模块化标签常量定义
 * - 敏感信息脱敏功能
 */
object Logger {

    /**
     * 日志标签常量
     * 按模块分类，便于日志过滤和查找
     */
    object Tags {
        const val JWXT_API = "ECJTU_JwxtApi"
        const val PARSER = "ECJTU_Parser"
        const val REPOSITORY = "ECJTU_Repository"
        const val LOCAL_DATA = "ECJTU_LocalData"
        const val VIEWMODEL = "ECJTU_ViewModel"
        const val CALENDAR = "ECJTU_Calendar"
        const val SCORE = "ECJTU_Score"
        const val SELECTED_COURSE = "ECJTU_SelectedCourse"
        const val SETTINGS = "ECJTU_Settings"
        const val ACADEMIC_CALENDAR = "ECJTU_AcademicCal"
        const val UPDATE = "ECJTU_Update"
        const val NAVIGATION = "ECJTU_Navigation"
        const val APP = "ECJTU_App"
    }

    /**
     * 敏感信息脱敏
     *
     * @param raw 原始敏感信息
     * @param visibleChars 保留可见字符数（默认 3）
     * @return 脱敏后的字符串，格式：前几位***后几位
     */
    fun mask(raw: String?, visibleChars: Int = 3): String {
        if (raw.isNullOrEmpty()) return "***"
        if (raw.length <= visibleChars * 2) return "***"

        val prefix = raw.take(visibleChars)
        val suffix = raw.takeLast(visibleChars)
        val maskedLength = raw.length - visibleChars * 2
        return "$prefix${"*".repeat(maskedLength)}$suffix"
    }

    /**
     * Cookie 脱敏（保留键，脱敏值）
     *
     * @param cookie Cookie 字符串
     * @return 脱敏后的 Cookie
     */
    fun maskCookie(cookie: String?): String {
        if (cookie.isNullOrEmpty()) return "***"

        return cookie.split(";").joinToString("; ") { part ->
            val keyValue = part.split("=", limit = 2)
            if (keyValue.size == 2) {
                "${keyValue[0].trim()}=${mask(keyValue[1].trim())}"
            } else {
                part
            }
        }
    }

    /**
     * DEBUG 级别日志
     * 仅在 Debug 版本输出
     */
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    /**
     * INFO 级别日志
     * 仅在 Debug 版本输出
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    /**
     * WARN 级别日志
     * 仅在 Debug 版本输出
     */
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }

    /**
     * ERROR 级别日志（仅消息）
     * 仅在 Debug 版本输出
     */
    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }

    /**
     * ERROR 级别日志（消息 + 异常）
     * 仅在 Debug 版本输出
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }

    // ========== 便捷方法 ==========

    /**
     * 网络请求开始日志
     */
    fun logRequestStart(tag: String, url: String, paramCount: Int = 0) {
        d(tag, "请求开始: URL=$url${if (paramCount > 0) ", 参数数量=$paramCount" else ""}")
    }

    /**
     * 网络请求成功日志
     */
    fun logRequestSuccess(tag: String, responseLength: Int) {
        d(tag, "请求成功: 响应长度=$responseLength")
    }

    /**
     * 网络请求失败日志
     */
    fun logRequestError(tag: String, error: String, throwable: Throwable? = null) {
        if (throwable != null) {
            e(tag, "请求失败: $error", throwable)
        } else {
            e(tag, "请求失败: $error")
        }
    }

    /**
     * 解析开始日志
     */
    fun logParseStart(tag: String, functionName: String) {
        d(tag, "开始解析: $functionName")
    }

    /**
     * 解析成功日志
     */
    fun logParseSuccess(tag: String, dataCount: Int, dataType: String = "条数据") {
        d(tag, "解析成功: 获取到 $dataCount $dataType")
    }

    /**
     * 跳过无效数据日志
     */
    fun logSkipInvalid(tag: String, reason: String) {
        w(tag, "跳过无效数据: $reason")
    }

    /**
     * 数据库操作日志
     */
    fun logDbOperation(tag: String, operation: String, count: Int) {
        d(tag, "$operation $count 条记录")
    }

    /**
     * ViewModel 事件触发日志
     */
    fun logEvent(tag: String, event: String) {
        d(tag, "事件触发: $event")
    }

    /**
     * ViewModel 状态变化日志
     */
    fun logStateChange(tag: String, state: String) {
        d(tag, "状态变化: $state")
    }
}
