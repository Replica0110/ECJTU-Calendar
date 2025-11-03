package com.lonx.ecjtu.calendar.domain.usecase

import com.lonx.ecjtu.calendar.domain.repository.AcademicCalendar

class GetAcademicCalendarUseCase(private val academicCalendar: AcademicCalendar) {
    suspend operator fun invoke(): Result<String> {
        return academicCalendar.getAcademicCalendar()
    }
}