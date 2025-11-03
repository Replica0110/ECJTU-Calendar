package com.lonx.ecjtu.calendar.data.datasource.remote

import rxhttp.toAwait
import rxhttp.wrapper.param.RxHttp


class JwxtDataSourceImpl : JwxtDataSource {

    override suspend fun fetchHtml(url: String, params: Map<String, Any>?): Result<String>{
        // 使用 RxHttp 创建请求
        val request = RxHttp.get(url)

        // 如果提供了参数，则添加到请求中
        params?.forEach { (key, value) ->
            request.add(key, value)
        }

        // 发起请求并等待结果
        try {
            val response = request.toAwait<String>().await()
            return Result.success(response)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

}