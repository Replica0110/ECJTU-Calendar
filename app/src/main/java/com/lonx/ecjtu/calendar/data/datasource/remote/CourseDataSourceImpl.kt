package com.lonx.ecjtu.calendar.data.datasource.remote

import rxhttp.toAwait
import rxhttp.wrapper.param.RxHttp
import java.io.IOException


class CourseDataSourceImpl : CourseDataSource {

    override suspend fun fetchCalendarHtml(url: String, params: Map<String, Any>?): String {
        // 使用 RxHttp 创建请求
        val request = RxHttp.get(url)

        // 如果提供了参数，则添加到请求中
        params?.forEach { (key, value) ->
            request.add(key, value)
        }

        // 发起请求并等待结果
        try {
            val response = request.toAwait<String>().await()
            return response
        } catch (e: IOException) {
            // 网络异常或IO异常
            throw Exception("网络请求失败: $e", e)
        } catch (e: Exception) {
            // 其他异常，如HTTP错误状态码
            throw Exception("请求失败: $e", e)
        }
    }

}