package com.lonx.ecjtu.calendar.data.datasource.remote

import android.util.Log
import com.lonx.ecjtu.calendar.domain.repository.AcademicCalendar
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import rxhttp.toAwait
import rxhttp.wrapper.param.RxHttp
import java.net.URL

class AcademicCalendarDataSourceImpl(private val htmlParser: HtmlParser) : AcademicCalendarDataSource {
    private val calendarUrl = "https://jwxt.ecjtu.edu.cn/weixin/xiaoli.html"
    
    override suspend fun getAcademicCalendar(): Result<String> {
        return try {
            Log.d("AcademicCalendar", "开始获取校历网页: $calendarUrl")

            val htmlContent = RxHttp.get(calendarUrl).toAwait<String>().await()

            Log.d("AcademicCalendar", "获取校历网页成功，开始解析")
            
            // 解析网页获取图片链接
            val imageUrl = htmlParser.parseAcademicCalendarImageUrl(htmlContent)
            
            if (imageUrl != null) {
                val fullImageUrl = if (imageUrl.startsWith("http")) {
                    imageUrl
                } else {
                    // 处理相对路径
                    URL(URL(calendarUrl), imageUrl).toString()
                }
                Log.d("AcademicCalendar", "解析到校历图片URL: $fullImageUrl")
                Result.success(fullImageUrl)
            } else {
                Log.e("AcademicCalendar", "未能从网页中解析到图片链接")
                Result.failure(Exception("未能从网页中解析到图片链接"))
            }
        } catch (e: Exception) {
            Log.e("AcademicCalendar", "获取校历图片URL失败", e)
            Result.failure(e)
        }
    }
}