package com.lonx.ecjtu.calendar.data.mapper

import com.lonx.ecjtu.calendar.data.dto.SelectedCourseDTO
import com.lonx.ecjtu.calendar.data.dto.SelectedCoursePageData
import com.lonx.ecjtu.calendar.domain.model.SelectedCourse
import com.lonx.ecjtu.calendar.domain.model.SelectedCoursePage


fun SelectedCourseDTO.toDomain(): SelectedCourse {
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

fun SelectedCoursePageData.toDomain(): SelectedCoursePage {
    return SelectedCoursePage(
        courses = this.courses.map { it.toDomain() },
        availableTerms = this.availableTerms,
        currentTerm = this.currentTerm
    )
}