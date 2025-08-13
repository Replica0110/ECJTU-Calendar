package com.lonx.ecjtu.hjcalendar.data.source.remote

import com.lonx.ecjtu.hjcalendar.utils.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.net.UnknownHostException

class CalendarApiService {
    private val client = NetworkModule.okHttpClient

    // 定义 API 相关的自定义异常
    sealed class ApiException(message: String) : Exception(message) {
        class NetworkException(message: String = "网络连接失败") : ApiException(message)
        class ServerException(code: Int, message: String = "服务器错误") : 
            ApiException("服务器错误 $code: $message")
        class UnknownException(message: String = "未知错误") : ApiException(message)
    }

    /**
     * 获取课程日历的 HTML 数据。
     * @throws ApiException 当发生网络错误、服务器错误或其他错误时
     */
    suspend fun getCalendarHtml(weiXinID: String, date: String): String = 
        withContext(Dispatchers.IO) {
            try {
                val url = "https://jwxt.ecjtu.edu.cn/weixin/CalendarServlet?" +
                    "weiXinID=$weiXinID&date=$date"
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    throw ApiException.ServerException(
                        response.code,
                        response.message
                    )
                }

                response.body.let { body ->
                    body.string().takeIf { it.isNotBlank() } ?: throw ApiException.ServerException(
                        response.code,
                        "服务器返回数据为空"
                    )
                }
            } catch (e: UnknownHostException) {
                throw ApiException.NetworkException("无法连接到服务器，请检查网络连接")
            } catch (e: IOException) {
                throw ApiException.NetworkException("网络连接失败：${e.message}")
            } catch (e: Exception) {
                when (e) {
                    is ApiException -> throw e
                    else -> throw ApiException.UnknownException(e.message ?: "未知错误")
                }
            }
        }
}