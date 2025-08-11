package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.CourseData
import com.lonx.ecjtu.hjcalendar.utils.ECJTUCalendarAPI
import com.lonx.ecjtu.hjcalendar.utils.Event
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    // --- LiveData for UI State ---

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
    private val _currentDate = MutableLiveData<Date>(Date()) // 初始化为今天
    val currentDate: LiveData<Date> = _currentDate

    // --- Public Functions for Fragment to Call ---

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
        // 使用 LiveData 中的日期
        val date = _currentDate.value ?: Date()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        // 从 SharedPreferences 获取 weiXinID
        val weiXinID = getWeixinID()
        if (weiXinID.isBlank()) {
            _toastMessage.value = Event("请先在设置中配置 weiXinID")
            // 创建一个表示错误的列表项
            _courseList.value = createErrorCourseList(formattedDate, "未配置weiXinID")
            return
        }

        // 使用 viewModelScope 执行网络请求
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val html = ECJTUCalendarAPI.getCourseHtml(weiXinID, formattedDate)
                if (html != null && html.isNotBlank()) {
                    if (html.contains("<title>教务处微信平台绑定</title>")) {
                        _courseList.value = createErrorCourseList(formattedDate, "无效的weiXinID")
                        _toastMessage.value = Event("获取课程失败: 无效的weiXinID")
                    } else {
                        val dayCourse = ECJTUCalendarAPI.parseCourseHtml(html)
                        _courseList.value = listOf(dayCourse)
                        _toastMessage.value = Event("日历已更新")
                    }
                } else {
                    _courseList.value = createErrorCourseList(formattedDate, "空响应")
                    _toastMessage.value = Event("获取课程失败: 服务器响应为空")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _courseList.value = createErrorCourseList(formattedDate, e.message ?: "未知错误")
                _toastMessage.value = Event("获取课程失败: ${e.message ?: "未知错误"}")
            } finally {
                _isLoading.value = false // 确保刷新动画总是会停止
            }
        }
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

    // --- Private Helper Functions ---

    private fun getWeixinID(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val key = getApplication<Application>().getString(R.string.weixin_id_key)
        return prefs.getString(key, "") ?: ""
    }

    private fun createErrorCourseList(date: String, errorMsg: String): List<CourseData.DayCourses> {
        return listOf(CourseData.DayCourses(date,
            listOf(CourseData.CourseInfo(
                courseName = "课表加载错误: $errorMsg"))
        ))
    }
}