package com.lonx.ecjtu.calendar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScorePage(
    val scores: List<Score>,
    val availableTerms: List<String>,
    val currentTerm: String
)