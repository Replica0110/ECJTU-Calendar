package com.lonx.ecjtu.calendar.domain.model

import kotlinx.serialization.Serializable

/***
 *
 * 选课信息
 * @param courseName 课程名称
 * @param courseRequire 课程属性
 * @param period 学时
 * @param credit 学分
 * @param selectedType 选课模块
 * @param courseType 课程类型
 * @param checkType 考核方式
 * @param courseTeacher 课程教师
 * @param isSelected 是否已选
 * @param className 教学班名称
 * @param classTime 上课时间
 */
@Serializable
data class SelectedCourse(
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