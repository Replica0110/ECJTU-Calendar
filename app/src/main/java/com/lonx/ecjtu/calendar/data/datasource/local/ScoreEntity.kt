package com.lonx.ecjtu.calendar.data.datasource.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lonx.ecjtu.calendar.domain.model.Score

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseName: String,
    val courseCode: String,
    val credit: Double,
    val finalScore: String,
    val retakeScore: String,
    val relearnScore: String,
    val courseType: String,
    val term: String
)

fun ScoreEntity.toDomain(): Score {
    return Score(
        courseName = this.courseName,
        courseCode = this.courseCode,
        credit = this.credit,
        finalScore = this.finalScore,
        retakeScore = this.retakeScore,
        relearnScore = this.relearnScore,
        courseType = this.courseType
    )
}

fun Score.toEntity(term: String): ScoreEntity {
    return ScoreEntity(
        courseName = this.courseName,
        courseCode = this.courseCode,
        credit = this.credit,
        finalScore = this.finalScore,
        retakeScore = this.retakeScore,
        relearnScore = this.relearnScore,
        courseType = this.courseType,
        term = term
    )
}