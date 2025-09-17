package com.lonx.ecjtu.calendar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DateInfo(
    val date: String,
    val weekNumber: String,
    val dayOfWeek: String
)