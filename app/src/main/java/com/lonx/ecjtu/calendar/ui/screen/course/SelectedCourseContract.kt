package com.lonx.ecjtu.calendar.ui.screen.course

import com.lonx.ecjtu.calendar.domain.model.SelectedCourse

data class SelectedCourseUiState(
    val isLoading: Boolean = false,
    val courses: List<SelectedCourse> = emptyList(),
    val availableTerms: List<String> = emptyList(),
    val currentTerm: String = "",
    val error: String? = null,
    val lastRefreshMillis: Long = 0L
)

sealed interface SelectedCourseEffect {
    data class ShowToast(val message: String) : SelectedCourseEffect
}