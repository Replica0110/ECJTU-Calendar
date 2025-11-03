package com.lonx.ecjtu.calendar.data.dto

import kotlinx.serialization.Serializable


@Serializable
data class ScoreDTO(
    val courseName: String = "",           // 课程名称
    val courseCode: String = "",           // 课程代码
    val credit: Double = 0.0,              // 学分
    val finalScore: String = "",           // 期末成绩
    val retakeScore: String = "",          // 重考成绩
    val relearnScore: String = "",         // 重修成绩
    val courseType: String = ""            // 课程类型（必修，选修等）
)