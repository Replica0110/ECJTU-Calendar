package com.lonx.ecjtu.calendar.domain.usecase.calendar

import com.lonx.ecjtu.calendar.data.datasource.remote.AcademicCalendarDataSource

class GetAcademicCalendarUseCase(private val repository: AcademicCalendarDataSource) {
    suspend operator fun invoke(): Result<String> {
        return repository.getAcademicCalendar()
    }
}