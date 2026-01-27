package com.lonx.ecjtu.calendar.data.datasource.remote

import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
import rxhttp.toAwait
import rxhttp.wrapper.param.RxHttp


class JwxtDataSourceImpl : JwxtDataSource {

    override suspend fun fetchHtml(url: String, params: Map<String, Any>?): Result<String> {
        Logger.logRequestStart(Tags.JWXT_API, url, params?.size ?: 0)

        // 使用 RxHttp 创建请求
        val request = RxHttp.get(url)

        // 如果提供了参数，则添加到请求中
        params?.forEach { (key, value) ->
            request.add(key, value)
        }

        // 发起请求并等待结果
        return try {
            val response = request.toAwait<String>().await()
            Logger.logRequestSuccess(Tags.JWXT_API, response.length)
            Result.success(response)
        } catch (e: Exception) {
            Logger.logRequestError(Tags.JWXT_API, e.message ?: "未知错误", e)
            Result.failure(e)
        }
    }

}