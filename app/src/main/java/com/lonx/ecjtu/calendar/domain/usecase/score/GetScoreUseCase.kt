package com.lonx.ecjtu.calendar.domain.usecase.score

import com.lonx.ecjtu.calendar.domain.model.ScorePage
import com.lonx.ecjtu.calendar.domain.repository.ScoreRepository


class GetScoreUseCase(private val repository: ScoreRepository) {
    suspend operator fun invoke(weiXinID: String, term: String?): Result<ScorePage> {
        return repository.getScores(weiXinID, term)
    }
}