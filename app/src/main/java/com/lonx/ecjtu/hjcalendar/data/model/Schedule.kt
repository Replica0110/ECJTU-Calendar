package com.lonx.ecjtu.hjcalendar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 课程类型，用于区分不同类型的课程。
 */
enum class CourseType {
    /** 正常课程 */
    NORMAL,

    /** 空课程（当天无课） */
    EMPTY,

    /** 错误状态 */
    ERROR;

    val isValid: Boolean get() = this == NORMAL
}

/**
 * 代表一节课程的基础信息。
 */
@Parcelize
data class Course(
    val name: String = "",
    val time: String = "",      // e.g., "1,2"
    val week: String = "",      // e.g., "1-16"
    val location: String = "",
    val teacher: String = "",
    val msg: String = "",
    val type: CourseType = CourseType.NORMAL
) : Parcelable {
    companion object {
        /**
         * 创建空课程表显示
         */
        fun createEmpty(message: String) = Course(
            msg = message,
            type = CourseType.EMPTY
        )

        /**
         * 创建错误状态显示
         */
        fun createError(message: String) = Course(
            msg = message,
            type = CourseType.ERROR
        )
    }

    /**
     * 判断是否为有效的课程
     */
    val isValid: Boolean get() = type.isValid

    /**
     * 格式化显示字段
     */
    val displayTime: String get() = if (time.isNotBlank()) "节次：$time" else ""
    val displayWeek: String get() = if (week.isNotBlank()) "上课周：$week" else ""
    val displayLocation: String get() = if (location.isNotBlank()) "地点：$location" else ""
    val displayTeacher: String get() = if (teacher.isNotBlank()) "教师：$teacher" else ""
}

/**
 * 代表一天的课程安排。包含课程列表和元数据。
 */
@Parcelize
data class DailySchedule(
    val dateInfo: String, // e.g., "2022-05-13 星期五（第12周）"
    val courses: List<Course>
) : Parcelable {
    companion object {
        /**
         * 创建空课程表
         */
        fun empty(dateInfo: String, message: String) = DailySchedule(
            dateInfo = dateInfo,
            courses = listOf(Course.createEmpty(message))
        )

        /**
         * 创建错误状态课程表
         */
        fun error(dateInfo: String, message: String) = DailySchedule(
            dateInfo = dateInfo,
            courses = listOf(Course.createError(message))
        )
    }

    /**
     * 获取有效的课程列表（过滤掉空课程和错误状态）
     */
    val validCourses: List<Course> get() = courses.filter { it.isValid }

    /**
     * 判断当天是否有课
     */
    val hasValidCourses: Boolean get() = validCourses.isNotEmpty()

    /**
     * 获取课程状态
     */
    val scheduleType: CourseType get() = courses.firstOrNull()?.type ?: CourseType.EMPTY
}

/**
 * 这是一个密封类，用于封装从数据层返回的所有可能结果。
 */
sealed interface ScheduleResult {
    /** 成功获取课程数据 */
    data class Success(val schedule: DailySchedule) : ScheduleResult

    /** 成功，但当天无课 */
    data class Empty(val dateInfo: String) : ScheduleResult

    /** 获取数据时发生错误 */
    data class Error(val dateInfo: String, val message: String) : ScheduleResult
}
