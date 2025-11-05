package com.lonx.ecjtu.calendar.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScorePageData(
    val scores: List<ScoreDTO>,
    val availableTerms: List<String>,
    val currentTerm: String,
    val error: String? = null
)