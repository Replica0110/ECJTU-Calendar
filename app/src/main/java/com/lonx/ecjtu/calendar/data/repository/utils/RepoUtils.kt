package com.lonx.ecjtu.calendar.data.repository.utils

import android.util.Log
import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * 封装网络调用与异常处理，返回 Result<T>
 */
suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(withContext(Dispatchers.IO) { block() })
    } catch (e: Exception) {
        Log.e("Repository", "Error during API call", e)
        Result.failure(e)
    }
}

/**
 * 获取本地 WeiXinID，如果为空则返回失败 Result
 */
suspend fun LocalDataSource.requireWeiXinId(): Result<String> {
    val id = getWeiXinID().first()
    return if (id.isNotBlank()) Result.success(id)
    else Result.failure(Exception("WeiXinID为空"))
}