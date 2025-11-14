package com.lonx.ecjtu.calendar.ui.screen.academiccalendar

data class AcademicCalendarUiState(
    val isLoading: Boolean = false,
    val imageUrl: String? = null,
    val error: String? = null,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val colorMode: Int = 0
)