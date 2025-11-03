package com.lonx.ecjtu.calendar.domain.usecase.course

import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import java.time.LocalDate

class GetCoursesUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(date: LocalDate): Result<SchedulePage> {
        return repository.getCourses(date)
    }
}