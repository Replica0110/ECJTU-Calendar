package com.lonx.ecjtu.calendar.data.datasource.remote

import android.util.Log
import com.lonx.ecjtu.calendar.domain.repository.AcademicCalendar
import java.net.URL

class AcademicCalendarRemoteDataSource : AcademicCalendar {
    private val baseUrl = "https://jwxt.ecjtu.edu.cn/weixin/" // 假设基础URL相同
    
    override suspend fun getAcademicCalendar(): Result<String> {
        return try {
            // 根据你提供的HTML内容，实际图片路径应该是这样的
            // 注意：这里可能需要根据实际情况调整，比如根据学期返回不同图片
            val imageUrl = "${baseUrl}imgs/xiaoli/xiaoli_xia.png"
            Log.d("AcademicCalendar", "获取到校历图片URL: $imageUrl")
            Result.success(imageUrl)
        } catch (e: Exception) {
            Log.e("AcademicCalendar", "获取校历图片URL失败", e)
            Result.failure(e)
        }
    }
}