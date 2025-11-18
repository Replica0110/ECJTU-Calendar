package com.lonx.ecjtu.calendar.domain.repository

import com.lonx.ecjtu.calendar.domain.model.SelectedCoursePage

interface SelectedCourseRepository {

    suspend fun getSelectedCourses(term: String? = null): Result<SelectedCoursePage>

    suspend fun getSelectedCoursesFromLocal(term: String? = null): Result<SelectedCoursePage>
}