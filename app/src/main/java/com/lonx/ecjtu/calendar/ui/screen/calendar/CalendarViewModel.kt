package com.lonx.ecjtu.calendar.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.GetCoursesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate


class CalendarViewModel(
    private val getCoursesUseCase: GetCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        fetchCourses(LocalDate.now())
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
        _uiState.update { it.copy(isLoading = true, error = null) }
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
                    it.copy(isLoading = false, error = throwable.message ?: "未知错误")
                }
            }
        }
    }
}