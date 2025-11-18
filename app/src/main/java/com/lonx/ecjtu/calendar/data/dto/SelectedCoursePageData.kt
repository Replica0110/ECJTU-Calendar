package com.lonx.ecjtu.calendar.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SelectedCoursePageData(
    val courses: List<SelectedCourseDTO>,
    val availableTerms: List<String>,
    val currentTerm: String,
    val error: String? = null
)