package com.lonx.ecjtu.calendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.calendar.GetAcademicCalendarUseCase
import com.lonx.ecjtu.calendar.ui.screen.academiccalendar.AcademicCalendarUiState
import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class AcademicCalendarViewModel(
    private val getAcademicCalendarUseCase: GetAcademicCalendarUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(AcademicCalendarUiState())

    val uiState: StateFlow<AcademicCalendarUiState> = _uiState.asStateFlow()

    init {
        loadAcademicCalendar()
    }
    
    fun loadAcademicCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getAcademicCalendarUseCase(url = com.lonx.ecjtu.calendar.data.network.Constants.ACADEMIC_CALENDAR_URL).fold(
                onSuccess = { url ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            imageUrl = url,
                            error = null
                        ) 
                    }
                    
                    // 异步下载图片到内存缓存
                    cacheImage(url)
                },
                onFailure = { throwable ->
                    val errorMsg = throwable.message ?: "未知错误"
                    Logger.e(Tags.ACADEMIC_CALENDAR, "获取校历图片URL失败: $errorMsg", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    }
                }
            )
        }
    }

    private fun cacheImage(url: String) {
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    URL(url).openStream().use { it.readBytes() }
                }
                _uiState.update { it.copy(imageData = bytes) }
            } catch (e: Exception) {
                Logger.e(Tags.ACADEMIC_CALENDAR, "缓存图片失败: ${e.message}", e)
            }
        }
    }
    
    fun refresh() {
        loadAcademicCalendar()
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun onToastShown() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}