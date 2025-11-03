package com.lonx.ecjtu.calendar.data.mapper

import com.lonx.ecjtu.calendar.data.dto.CourseItemDTO
import com.lonx.ecjtu.calendar.data.dto.ScheduleDTO
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.domain.model.DateInfo
import com.lonx.ecjtu.calendar.domain.model.SchedulePage

object ScheduleMapper {
    fun toDomain(dto: ScheduleDTO): SchedulePage {
        return SchedulePage(
            dateInfo = DateInfo(
                date = dto.dateInfo.first,
                weekNumber = dto.dateInfo.second,
                dayOfWeek = dto.dateInfo.third
            ),
            courses = dto.courses.map { it.toDomain() }
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
}