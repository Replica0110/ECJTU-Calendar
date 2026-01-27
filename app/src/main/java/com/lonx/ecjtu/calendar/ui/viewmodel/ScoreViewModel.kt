package com.lonx.ecjtu.calendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.domain.usecase.score.GetScoreUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUserConfigUseCase
import com.lonx.ecjtu.calendar.ui.screen.score.ScoreUiState
import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScoreViewModel(
    private val getScoreUseCase: GetScoreUseCase,
    private val getUserConfigUseCase: GetUserConfigUseCase, // 2. Add the dependency to the constructor
    private val localDataSource: LocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoreUiState())
    val uiState: StateFlow<ScoreUiState> = _uiState.asStateFlow()

    private var currentWeiXinID: String? = null

    init {
        observeUserConfig()
        observeTermRefresh()
    }

    private fun observeTermRefresh() {
        viewModelScope.launch {
            uiState.map { it.currentTerm }
                .distinctUntilChanged()
                .collectLatest { term ->
                    if (term.isNotBlank()) {
                        // subscribe to timestamp updates for the active term
                        localDataSource.getScoreLastRefresh(term).collect { ts ->
                            _uiState.update { it.copy(lastRefreshMillis = ts) }
                        }
                    } else {
                        _uiState.update { it.copy(lastRefreshMillis = 0L) }
                    }
                }
        }
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


    fun loadScores(term: String? = null, refresh: Boolean = false, showToast: Boolean = false) {
        val mode = if (refresh) "网络刷新" else "本地加载"
        Logger.d(Tags.VIEWMODEL, "加载成绩: term=${term ?: "null"}, mode=$mode")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (refresh) {
                // manual refresh: fetch from network (may save to local in repository)
                getScoreUseCase(term)
            } else {
                // default: load from local DB
                getScoreUseCase.getFromLocal(term)
            }

            result.onSuccess { scorePage ->
                Logger.d(Tags.VIEWMODEL, "成绩加载成功: ${scorePage.scores.size} 门课程")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scores = scorePage.scores,
                        availableTerms = scorePage.availableTerms,
                        currentTerm = scorePage.currentTerm,
                        toastMessage = if (refresh || showToast) "找到了 ${scorePage.scores.size} 门成绩" else null
                    )
                }
                // timestamp updates are handled by observeTermRefresh()
            }.onFailure { exception ->
                Logger.e(Tags.VIEWMODEL, "成绩加载失败: ${exception.message}", exception)
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
        Logger.logEvent(Tags.SCORE, "学期切换: $newTerm")
        loadScores(term = newTerm, showToast = true)
    }

    fun onToastShown() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}