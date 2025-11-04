package com.lonx.ecjtu.calendar.data.dto

data class ScorePageData(
    val scores: List<ScoreDTO>,
    val availableTerms: List<String>,
    val currentTerm: String,
    val error: String? = null
)