package com.lonx.ecjtu.calendar.domain.model

data class SchedulePage(
    val dateInfo: Triple<String, String, String>,
    val courses: List<Course>
)