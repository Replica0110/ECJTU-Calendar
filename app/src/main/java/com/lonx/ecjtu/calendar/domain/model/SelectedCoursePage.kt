package com.lonx.ecjtu.calendar.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class SelectedCoursePage(
    val courses: List<SelectedCourse>,
    val availableTerms: List<String>,
    val currentTerm: String
)