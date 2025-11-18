package com.lonx.ecjtu.calendar.data.datasource.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lonx.ecjtu.calendar.domain.model.SelectedCourse

@Entity(tableName = "selected_course")
data class SelectedCourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val term: String = "" // 学期
)

fun SelectedCourseEntity.toDomain(): SelectedCourse {
    return SelectedCourse(
        courseName = this.courseName,
        courseRequire = this.courseRequire,
        period = this.period,
        credit = this.credit,
        selectedType = this.selectedType,
        courseType = this.courseType,
        checkType = this.checkType,
        courseTeacher = this.courseTeacher,
        isSelected = this.isSelected,
        className = this.className,
        classTime = this.classTime
    )
}

fun SelectedCourse.toEntity(term: String): SelectedCourseEntity {
    return SelectedCourseEntity(
        courseName = this.courseName,
        courseRequire = this.courseRequire,
        period = this.period,
        credit = this.credit,
        selectedType = this.selectedType,
        courseType = this.courseType,
        checkType = this.checkType,
        courseTeacher = this.courseTeacher,
        isSelected = this.isSelected,
        className = this.className,
        classTime = this.classTime,
        term = term
    )
}