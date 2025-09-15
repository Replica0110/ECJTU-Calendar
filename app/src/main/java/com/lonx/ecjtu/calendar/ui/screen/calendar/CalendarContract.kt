package com.lonx.ecjtu.calendar.ui.screen.calendar

import com.lonx.ecjtu.calendar.domain.model.Course
import java.time.LocalDate


data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val dateInfo: String = "",
    val courses: List<Course> = emptyList(),
    val error: String? = null
)

sealed interface CalendarEvent {
    data object Refresh : CalendarEvent
    data class OnDateChange(val date: LocalDate) : CalendarEvent

    data object GoToTodayAndRefresh : CalendarEvent

}