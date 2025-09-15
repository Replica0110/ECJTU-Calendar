package com.lonx.ecjtu.calendar.data.model

// 这个 DTO 代表了从一个页面解析出来的所有信息
data class Schedule(
    val dateInfo: String, // 例如："2023-05-24 星期三（第14周）"
    val courses: List<CourseItem>
)


data class CourseItem(
    val time: String,      // 例如："3-4节"
    val name: String,
    val courseWeek: String,
    val location: String,
    val teacher: String,
    val dayOfWeek: String
)

