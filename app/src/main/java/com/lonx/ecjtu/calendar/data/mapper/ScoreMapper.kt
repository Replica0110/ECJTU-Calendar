package com.lonx.ecjtu.calendar.data.mapper

import com.lonx.ecjtu.calendar.data.dto.ScoreDTO
import com.lonx.ecjtu.calendar.data.dto.ScorePageData
import com.lonx.ecjtu.calendar.domain.model.Score
import com.lonx.ecjtu.calendar.domain.model.ScorePage

fun ScoreDTO.toDomain(): Score {
    return Score(
        courseName = this.courseName,
        credit = this.credit,
        finalScore = this.finalScore,
        courseType = this.courseType,
        courseCode = this.courseCode,
        retakeScore = this.retakeScore,
        relearnScore = this.relearnScore
    )
}
fun ScorePageData.toDomain(): ScorePage {
    return ScorePage(
        scores = this.scores.map { it.toDomain() }, // 复用 ScoreDTO -> Score 映射
        availableTerms = this.availableTerms,
        currentTerm = this.currentTerm
    )
}