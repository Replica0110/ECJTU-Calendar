package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.data.parser.InvalidWeiXinIdException
import com.lonx.ecjtu.hjcalendar.data.repository.CalendarRepository
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager
import com.lonx.ecjtu.hjcalendar.utils.CourseData
import com.lonx.ecjtu.hjcalendar.utils.Event
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val calendarRepository = CalendarRepository()
    private enum class SpecialState { EMPTY, ERROR }
    // 课程列表数据
    private val _courseList = MutableLiveData<List<CourseData.DayCourses>>()
    val courseList: LiveData<List<CourseData.DayCourses>> = _courseList

    // 是否正在加载/刷新
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 用于显示 Toast 等一次性事件
    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    // 当前显示的日期
    private val _currentDate = MutableLiveData(Date()) // 初始化为今天
    val currentDate: LiveData<Date> = _currentDate


    /**
     * 初始化数据，如果列表为空则刷新。
     */
    fun initialize() {
        if (_courseList.value.isNullOrEmpty()) {
            refreshCourses()
        }
    }

    /**
     * 刷新当前日期的课程。
     */
    fun refreshCourses() {
        val date = _currentDate.value ?: Date()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        val weiXinID = DataStoreManager.getWeiXinId()
        if (weiXinID.isBlank()) {
            _toastMessage.value = Event("请先在设置中配置 weiXinID")
            _courseList.value = createSpecialStateList(formattedDate, SpecialState.ERROR, "未配置weiXinID")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // 使用仓库获取数据，并处理 Result
            val result = calendarRepository.getDailyCourses(weiXinID, formattedDate)

            result.onSuccess { dayCourses ->
                // 处理成功的情况
                if (dayCourses.courses.isEmpty()) {
                    _courseList.value = createSpecialStateList(formattedDate, SpecialState.EMPTY)
                } else {
                    _courseList.value = listOf(dayCourses)
                }
                _toastMessage.value = Event("日历已更新")

            }.onFailure { exception ->
                // 处理失败的情况
                val errorMessage = when (exception) {
                    is InvalidWeiXinIdException -> "无效的weiXinID"
                    else -> exception.message ?: "未知错误"
                }
                _courseList.value = createSpecialStateList(formattedDate, SpecialState.ERROR, errorMessage)
                _toastMessage.value = Event("获取课程失败: $errorMessage")
            }

            _isLoading.value = false
        }
    }
    /**
     * 创建一个用于表示特殊状态（如课表为空或加载错误）的列表。
     * @param date 日期字符串
     * @param state 状态类型 (EMPTY 或 ERROR)
     * @param errorMessage 仅在 state 为 ERROR 时使用
     * @return 包含单个特殊 CourseInfo 的 DayCourses 列表
     */
    private fun createSpecialStateList(date: String, state: SpecialState, errorMessage: String? = null): List<CourseData.DayCourses> {
        val courseName: String
        val displayMessage: String

        if (state == SpecialState.EMPTY) {
            courseName = "课表为空"
            val defaultText = getApplication<Application>().getString(R.string.empty_course)
            displayMessage = DataStoreManager.getNoCourseText(defaultText)
        } else {
            courseName = "课表加载错误"
            displayMessage = errorMessage ?: getApplication<Application>().getString(R.string.error_course_message)
        }

        val specialCourse = CourseData.CourseInfo(
            courseName = courseName,
            courseLocation = displayMessage
        )

        return listOf(CourseData.DayCourses(date, listOf(specialCourse)))
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
        _toastMessage.value = Event("已显示今天的课程")
        refreshCourses()
    }

}