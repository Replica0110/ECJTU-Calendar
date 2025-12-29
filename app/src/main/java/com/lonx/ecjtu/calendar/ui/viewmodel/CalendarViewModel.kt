package com.lonx.ecjtu.calendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.course.GetCoursesUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUserConfigUseCase
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarEvent
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getUserConfigUseCase: GetUserConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    private var currentWeiXinID: String? = null

    init {
        _uiState.update {
            it.copy(
                selectedDate = LocalDate.now()
            )
        }
        observeUserConfig()
        fetchCourses(LocalDate.now())
    }

    private fun observeUserConfig() {
        viewModelScope.launch {
            getUserConfigUseCase().distinctUntilChanged().collect { newWeiXinID ->
                val oldWeiXinID = currentWeiXinID
                currentWeiXinID = newWeiXinID
                // 如果微信ID发生变化，刷新当前日期的课程数据
                if (oldWeiXinID != newWeiXinID) {
                    fetchCourses(uiState.value.selectedDate)
                }
            }
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.OnDateChange -> {
                _uiState.update { it.copy(selectedDate = event.date) }
                fetchCourses(event.date)
            }
            is CalendarEvent.Refresh -> {
                fetchCourses(uiState.value.selectedDate)
            }
            is CalendarEvent.GoToTodayAndRefresh -> {
                val today = LocalDate.now()
                // 只有当选择的日期不是今天时，才进行状态更新和数据获取
                if (uiState.value.selectedDate != today) {
                    _uiState.update { it.copy(selectedDate = today) }
                    fetchCourses(today)
                } else {
                    // 如果已经是今天，就只执行刷新操作
                    fetchCourses(today)
                }
            }

        }
    }

    private fun fetchCourses(date: LocalDate) {
        _uiState.update { it.copy(isLoading = true, error = null, courses = emptyList()) }
        viewModelScope.launch {
            val result = getCoursesUseCase(date)
            result.onSuccess { schedulePage ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dateInfo = schedulePage.dateInfo,
                        courses = schedulePage.courses
                    )
                }
            }.onFailure { throwable ->

                _uiState.update {
                    it.copy(isLoading = false, error = throwable.message)
                }
            }
        }
    }
}