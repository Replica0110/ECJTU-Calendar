package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.data.model.Course
import com.lonx.ecjtu.hjcalendar.data.model.DailySchedule
import com.lonx.ecjtu.hjcalendar.data.model.ScheduleResult
import com.lonx.ecjtu.hjcalendar.data.repository.CalendarRepository
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager
import com.lonx.ecjtu.hjcalendar.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val calendarRepository = CalendarRepository()

    // 使用 StateFlow 替代 LiveData，提供更好的协程支持
    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Initial)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    // 用于显示 Toast 等一次性事件
    private val _events = MutableLiveData<Event<String>>()
    val events: LiveData<Event<String>> = _events

    // 当前显示的日期
    private val _currentDate = MutableStateFlow(Date())
    val currentDate: StateFlow<Date> = _currentDate.asStateFlow()

    /**
     * UI 状态封装类
     */
    sealed class ScheduleUiState {
        object Initial : ScheduleUiState()
        object Loading : ScheduleUiState()
        data class Success(val schedule: DailySchedule) : ScheduleUiState()
        data class Empty(val schedule: DailySchedule) : ScheduleUiState()
        data class Error(val schedule: DailySchedule) : ScheduleUiState()
    }

    /**
     * 初始化数据，如果是初始状态则刷新。
     */
    fun initialize() {
        if (_uiState.value is ScheduleUiState.Initial) {
            refreshCourses()
        }
    }

    /**
     * 刷新当前日期的课程。
     */
    fun refreshCourses() {
        val date = _currentDate.value
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        val weiXinID = DataStoreManager.getWeiXinId()
        if (weiXinID.isBlank()) {
            handleScheduleResult(ScheduleResult.Error("", "请先在设置中配置 weiXinID"))
            return
        }

        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                val result = calendarRepository.getDailyCourses(weiXinID, formattedDate)
                handleScheduleResult(result)
            } catch (e: Exception) {
                handleScheduleResult(ScheduleResult.Error("", e.message ?: "未知错误"))
            }
        }
    }

    /**
     * 处理课程数据结果
     */
    private fun handleScheduleResult(result: ScheduleResult) {
        when (result) {
            is ScheduleResult.Success -> {
                _uiState.value = ScheduleUiState.Success(result.schedule)
                _events.value = Event("日历已更新")
            }
            is ScheduleResult.Empty -> {
                val emptySchedule = createEmptySchedule(result.dateInfo)
                _uiState.value = ScheduleUiState.Empty(emptySchedule)
                _events.value = Event("当天没有课程")
            }
            is ScheduleResult.Error -> {
                val errorSchedule = createErrorSchedule(result.dateInfo, result.message)
                _uiState.value = ScheduleUiState.Error(errorSchedule)
                _events.value = Event("获取课程失败: ${result.message}")
            }
        }
    }

    /**
     * 创建空课程表显示
     */
    private fun createEmptySchedule(dateInfo: String): DailySchedule {
        val defaultText = getApplication<Application>().getString(R.string.empty_course)
        val message = DataStoreManager.getNoCourseText(defaultText)
        return DailySchedule(
            dateInfo = dateInfo,
            courses = listOf(
                Course(
                    name = "课表为空",
                    time = "",
                    week = "",
                    location = message,
                    teacher = ""
                )
            )
        )
    }

    /**
     * 创建错误状态的课程表显示
     */
    private fun createErrorSchedule(dateInfo: String, errorMessage: String): DailySchedule {
        val message = errorMessage.ifBlank { 
            getApplication<Application>().getString(R.string.error_course_message)
        }
        return DailySchedule(
            dateInfo = dateInfo,
            courses = listOf(
                Course(
                    name = "课表加载错误",
                    time = "",
                    week = "",
                    location = message,
                    teacher = ""
                )
            )
        )
    }

    /**
     * 选择一个新的日期并刷新课程。
     */
    fun selectDate(newDate: Date) {
        _currentDate.value = newDate
        refreshCourses()
    }

    /**
     * 重置日期到今天并刷新课程。
     */
    fun resetToToday() {
        _currentDate.value = Date()
        _events.value = Event("已显示今天的课程")
        refreshCourses()
    }
}