package com.lonx.ecjtu.calendar.domain.model

import kotlinx.serialization.Serializable


/**
 * Domain model for a course.
 * Represents a clean business object used within the app (UI, business logic).
 * @param duration 上课周 (e.g., "1-16")
 * @param time 时间段 (e.g., "第1,2节")
 * @param name 课程名称
 * @param location 地点
 * @param teacher 老师
 * @param dayOfWeek 星期几 (e.g., "星期一")
 */
@Serializable
data class Course(
    val duration: String,
    val time: String,
    val name: String,
    val location: String,
    val teacher: String,
    val dayOfWeek: String
)