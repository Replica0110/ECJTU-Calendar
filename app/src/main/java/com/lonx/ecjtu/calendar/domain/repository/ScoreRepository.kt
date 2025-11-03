package com.lonx.ecjtu.calendar.domain.repository

import com.lonx.ecjtu.calendar.domain.model.Score

interface ScoreRepository {
    suspend fun getScores(term: String?): Result<List<Score>>
}