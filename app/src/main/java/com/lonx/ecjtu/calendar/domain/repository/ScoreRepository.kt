package com.lonx.ecjtu.calendar.domain.repository

import com.lonx.ecjtu.calendar.domain.model.ScorePage

interface ScoreRepository {
    suspend fun getScores(term: String?): Result<ScorePage>
    suspend fun getScoresFromLocal(term: String?): Result<ScorePage>
}