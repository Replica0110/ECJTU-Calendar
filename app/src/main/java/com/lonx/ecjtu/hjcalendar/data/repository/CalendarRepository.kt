package com.lonx.ecjtu.hjcalendar.data.repository

import com.lonx.ecjtu.hjcalendar.data.model.ScheduleResult
import com.lonx.ecjtu.hjcalendar.data.parser.CalendarHtmlParser
import com.lonx.ecjtu.hjcalendar.data.parser.InvalidWeiXinIdException
import com.lonx.ecjtu.hjcalendar.data.source.remote.CalendarApiService
import com.lonx.ecjtu.hjcalendar.data.source.remote.CalendarApiService.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 日历仓库类，负责协调数据源和数据解析。
 */
class CalendarRepository {
    private val apiService = CalendarApiService()
    private val parser = CalendarHtmlParser()

    /**
     * 获取每日课程。
     * @param weiXinID 微信ID
     * @param date 日期字符串，格式为 yyyy-MM-dd
     * @return [ScheduleResult] 封装了课程数据或错误信息
     */
    suspend fun getDailyCourses(weiXinID: String, date: String): ScheduleResult {
        return withContext(Dispatchers.IO) {
            try {
                val html = apiService.getCalendarHtml(weiXinID, date)
                parser.parse(html)
            } catch (e: Exception) {
                when (e) {
                    is ApiException.NetworkException -> 
                        ScheduleResult.Error(date, "网络连接失败，请检查网络后重试")
                    is ApiException.ServerException -> 
                        ScheduleResult.Error(date, "服务器错误：${e.message}")
                    is InvalidWeiXinIdException ->
                        ScheduleResult.Error(date, "无效的微信ID，请在设置中重新配置")
                    else -> ScheduleResult.Error(date, e.message ?: "未知错误")
                }
            }
        }
    }
}