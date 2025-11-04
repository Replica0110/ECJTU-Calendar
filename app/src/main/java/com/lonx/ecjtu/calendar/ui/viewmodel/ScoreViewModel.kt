package com.lonx.ecjtu.calendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.score.GetScoreUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUserConfigUseCase // 1. Import the new Use Case
import com.lonx.ecjtu.calendar.ui.screen.score.ScoreScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScoreViewModel(
    private val getScoreUseCase: GetScoreUseCase,
    private val getUserConfigUseCase: GetUserConfigUseCase // 2. Add the dependency to the constructor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoreScreenState())
    val uiState = _uiState.asStateFlow()

    private var currentWeiXinID: String? = null

    init {
        observeUserConfig()
    }

    private fun observeUserConfig() {
        viewModelScope.launch {
            getUserConfigUseCase().distinctUntilChanged().collect { newWeiXinID ->
                currentWeiXinID = newWeiXinID
                if (newWeiXinID.isNotBlank()) {
                    loadScores()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "用户未配置，请先在设置中绑定账号"
                        )
                    }
                }
            }
        }
    }


    fun loadScores(term: String? = null) {
        val id = currentWeiXinID
        if (id.isNullOrBlank()) {
            _uiState.update { it.copy(error = "加载失败，未填写weiXinID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getScoreUseCase(id, term)

            result.onSuccess { scorePage ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scores = scorePage.scores,
                        availableTerms = scorePage.availableTerms,
                        currentTerm = scorePage.currentTerm
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "加载失败，请重试"
                    )
                }
            }
        }
    }

    fun onTermSelected(newTerm: String) {
        loadScores(term = newTerm)
    }
}