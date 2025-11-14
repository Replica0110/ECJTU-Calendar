package com.lonx.ecjtu.calendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetColorModeUseCase
import com.lonx.ecjtu.calendar.ui.screen.academiccalendar.AcademicCalendarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AcademicCalendarViewModel(
    private val getColorModeUseCase: GetColorModeUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(AcademicCalendarUiState())

    val uiState: StateFlow<AcademicCalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getColorModeUseCase().collect { colorMode ->
                _uiState.update { it.copy(colorMode = colorMode) }
            }
        }
    }
}