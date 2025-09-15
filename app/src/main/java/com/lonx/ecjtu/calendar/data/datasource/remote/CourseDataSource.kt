package com.lonx.ecjtu.calendar.data.datasource.remote

interface CourseDataSource {
    suspend fun fetchCalendarHtml(url: String,params: Map<String, Any>? = null): String
}