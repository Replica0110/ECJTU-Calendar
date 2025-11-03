package com.lonx.ecjtu.calendar.data.datasource.remote


/***
 * 微信教务系统数据源
 */

interface JwxtDataSource {
    suspend fun fetchHtml(url: String, params: Map<String, Any>? = null): Result<String>
}