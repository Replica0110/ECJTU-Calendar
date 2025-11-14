package com.lonx.ecjtu.calendar.ui.screen.score

import com.lonx.ecjtu.calendar.domain.model.Score

/**
 * 代表成绩屏幕的UI状态。
 *
 * @param isLoading 是否正在加载数据（用于显示加载指示器）。
 * @param scores 要在屏幕上显示的成绩列表。
 * @param availableTerms 所有可供选择的学期列表（用于填充下拉菜单）。
 * @param currentTerm 当前显示的成绩所属的学期。
 * @param error 如果发生错误，则包含错误消息，否则为null。
 */
data class ScoreScreenState(
    val isLoading: Boolean = false,
    val scores: List<Score> = emptyList(),
    val availableTerms: List<String> = emptyList(),
    val currentTerm: String = "",
    val error: String? = null,
    val colorMode: Int = 0
)