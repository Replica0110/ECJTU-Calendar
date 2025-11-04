package com.lonx.ecjtu.calendar.domain.model

data class ScorePage(
    val scores: List<Score>,
    val availableTerms: List<String>,
    val currentTerm: String
)