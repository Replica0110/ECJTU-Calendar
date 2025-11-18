package com.lonx.ecjtu.calendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.domain.usecase.course.GetSelectedCoursesUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUserConfigUseCase
import com.lonx.ecjtu.calendar.ui.screen.course.SelectedCourseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectedCourseViewModel(
    private val getSelectedCoursesUseCase: GetSelectedCoursesUseCase,
    private val getUserConfigUseCase: GetUserConfigUseCase,
    private val localDataSource: LocalDataSource
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectedCourseUiState())
    val uiState: StateFlow<SelectedCourseUiState> = _uiState.asStateFlow()

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
                        localDataSource.getSelectedCourseLastRefresh(term).collect { ts ->
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
                    loadCourses()
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

    fun loadCourses(term: String? = null, refresh: Boolean = false) {

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (refresh) {
                getSelectedCoursesUseCase(term)
            } else {
                getSelectedCoursesUseCase.getFromLocal(term)
            }

            result.onSuccess { coursePage ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        courses = coursePage.courses,
                        availableTerms = coursePage.availableTerms,
                        currentTerm = coursePage.currentTerm
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
        loadCourses(term = newTerm)
    }
}