package com.lonx.ecjtu.calendar.domain.repository

import com.lonx.ecjtu.calendar.domain.model.ScorePage

interface ScoreRepository {
    suspend fun getScores(weiXinID: String, term: String?): Result<ScorePage>
}