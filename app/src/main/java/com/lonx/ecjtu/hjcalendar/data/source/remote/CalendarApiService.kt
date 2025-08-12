package com.lonx.ecjtu.hjcalendar.data.source.remote

import com.lonx.ecjtu.hjcalendar.utils.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class CalendarApiService {

    private val client = NetworkModule.okHttpClient

    suspend fun getCalendarHtml(weiXinID: String, date: String): String = withContext(Dispatchers.IO) {
        val url = "https://jwxt.ecjtu.edu.cn/weixin/CalendarServlet?weiXinID=$weiXinID&date=$date"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw java.io.IOException("网络请求失败: ${response.code}")
        }

        // 返回响应体，如果为空则抛出异常
        response.body.string()
    }
}