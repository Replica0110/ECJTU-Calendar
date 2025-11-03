package com.lonx.ecjtu.calendar.ui.screen.score

import com.lonx.ecjtu.calendar.domain.model.Score

data class ScoreScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    var scoreList: List<Score> = emptyList(),
    var isEmpty: Boolean = false
)