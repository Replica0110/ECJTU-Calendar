package com.lonx.ecjtu.hjcalendar.data.repository

import com.lonx.ecjtu.hjcalendar.data.parser.CalendarHtmlParser
import com.lonx.ecjtu.hjcalendar.data.parser.InvalidWeiXinIdException
import com.lonx.ecjtu.hjcalendar.data.source.remote.CalendarApiService
import com.lonx.ecjtu.hjcalendar.utils.CourseData

class CalendarRepository {

    private val apiService = CalendarApiService()
    private val parser = CalendarHtmlParser()

    /**
     * 获取每日课程，向上层返回一个 Result 对象，封装了成功或失败。
     */
    suspend fun getDailyCourses(weiXinID: String, date: String): Result<CourseData.DayCourses> {
        return try {
            val html = apiService.getCalendarHtml(weiXinID, date)
            val dayCourses = parser.parse(html)
            Result.success(dayCourses)
        } catch (e: InvalidWeiXinIdException) {
            // 捕获我们自定义的异常，并包装成特定的失败结果
            Result.failure(e)
        } catch (e: Exception) {
            // 捕获所有其他异常（如网络问题）
            Result.failure(e)
        }
    }
}