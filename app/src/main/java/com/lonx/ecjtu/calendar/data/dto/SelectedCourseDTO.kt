package com.lonx.ecjtu.calendar.data.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for selected courses.
 */
@Serializable
data class SelectedCourseDTO(
    val courseName: String = "", // 课程名称
    val courseRequire: String = "", // e.g.必修课/选修课
    val period: Double = 0.0, // 学时
    val credit: Double = 0.0, // 学分
    val selectedType: String = "", // 选课模块
    val courseType: String = "", // 选课类型
    val checkType: String = "", // 考核方式
    val courseTeacher: String = "", // 课程教师
    val isSelected: String = "", // 是否已选
    val className: String = "", // 教学班名称
    val classTime: String = "", // 上课时间
)