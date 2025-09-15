package com.lonx.ecjtu.calendar.domain.model

data class SchedulePage(
    val dateInfo: String,
    val courses: List<Course>
)