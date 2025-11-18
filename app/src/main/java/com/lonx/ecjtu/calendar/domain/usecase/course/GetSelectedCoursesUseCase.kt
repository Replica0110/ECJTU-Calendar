package com.lonx.ecjtu.calendar.domain.usecase.course

import com.lonx.ecjtu.calendar.domain.model.SelectedCoursePage
import com.lonx.ecjtu.calendar.domain.repository.SelectedCourseRepository

class GetSelectedCoursesUseCase(private val repository: SelectedCourseRepository) {
    suspend operator fun invoke(term: String? = null): Result<SelectedCoursePage> {
        return repository.getSelectedCourses(term)
    }

    suspend fun getFromLocal(term: String? = null): Result<SelectedCoursePage> {
        return repository.getSelectedCoursesFromLocal(term)
    }
}