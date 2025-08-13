package com.lonx.ecjtu.hjcalendar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 代表一节具体的课程。
 * 只包含纯粹的数据，没有业务逻辑或UI前缀。
 */
@Parcelize
data class Course(
    val name: String,
    val time: String,      // e.g., "1,2"
    val week: String,      // e.g., "1-16"
    val location: String,
    val teacher: String
) : Parcelable

/**
 * 代表一天的课程安排。
 */
@Parcelize
data class DailySchedule(
    val dateInfo: String, // e.g., "2022-05-13 星期五（第12周）"
    val courses: List<Course>
) : Parcelable

/**
 * 这是一个密封类，用于封装从数据层返回的所有可能结果。
 * 它比返回一个可空的 DailySchedule 或包含特殊课程的列表要健壮和清晰得多。
 */
sealed class ScheduleResult {
    /** 成功，并且当天有课 */
    data class Success(val schedule: DailySchedule) : ScheduleResult()

    /** 成功，但当天无课 */
    data class Empty(val dateInfo: String) : ScheduleResult()

    /** 获取数据时发生错误 */
    data class Error(val dateInfo: String, val message: String) : ScheduleResult()
}
