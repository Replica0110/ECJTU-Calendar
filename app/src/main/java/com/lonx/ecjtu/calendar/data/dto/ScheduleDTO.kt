package com.lonx.ecjtu.calendar.data.dto

data class ScheduleDTO(
    val title: String,
    val dateInfo: Triple<String, String, String>, // 例如："2023-05-24 星期三（第14周）"
    val courses: List<CourseItemDTO>
)

data class CourseItemDTO(
    val time: String,      // 例如："3-4节"
    val name: String,
    val courseWeek: String,
    val location: String,
    val teacher: String,
    val dayOfWeek: String
)