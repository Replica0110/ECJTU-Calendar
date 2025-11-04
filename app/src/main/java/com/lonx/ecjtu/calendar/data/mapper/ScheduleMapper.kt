package com.lonx.ecjtu.calendar.data.mapper

import com.lonx.ecjtu.calendar.data.dto.CourseItemDTO
import com.lonx.ecjtu.calendar.data.dto.ScheduleDTO
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.domain.model.DateInfo
import com.lonx.ecjtu.calendar.domain.model.SchedulePage

fun ScheduleDTO.toDomain(): SchedulePage {
    return SchedulePage(
        dateInfo = DateInfo(
            date = this.dateInfo.first,
            weekNumber = this.dateInfo.second,
            dayOfWeek = this.dateInfo.third
        ),
        courses = this.courses.map { it.toDomain() }
    )
}

private fun CourseItemDTO.toDomain(): Course {
    return Course(
        time = this.time,
        name = this.name,
        location = this.location,
        teacher = this.teacher,
        duration = this.courseWeek,
        dayOfWeek = this.dayOfWeek
    )
}
