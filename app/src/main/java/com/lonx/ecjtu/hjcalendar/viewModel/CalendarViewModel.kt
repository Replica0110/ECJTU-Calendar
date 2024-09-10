package com.lonx.ecjtu.hjcalendar.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.hjcalendar.util.CourseData
import com.lonx.ecjtu.hjcalendar.util.ECJTUCalendarAPI
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val _courseList = MutableLiveData<List<CourseData.DayCourses>>()
    val courseList: LiveData<List<CourseData.DayCourses>> = _courseList
    private val ecjtuAPI = ECJTUCalendarAPI()
    // 添加回调参数
    fun fetchCourseInfo(
        weiXinID: String,
        date:String ,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val errorDayCourses=listOf(CourseData.DayCourses(date,
                    listOf(CourseData.CourseInfo(
                        courseName = "课表加载错误"))
                ))
                val html = ecjtuAPI.getCourseInfo(weiXinID,date)
//                Log.e("fetchCourseInfo", "HTML: $html")
                if (html != null) {
                    if (html.isNotBlank()) {
                        if (html.contains("<title>教务处微信平台绑定</title>")) {
                            Log.e("fetchCourseInfo", "Invalid weiXinID")
                            _courseList.postValue(errorDayCourses)
                            onFailure?.invoke("无效的weiXinID")
                        } else {
                            val dayCourse = ecjtuAPI.parseHtml(html)
                            _courseList.postValue(listOf(dayCourse))
                            onSuccess?.invoke("日历已更新")
                        }
                    } else {
                        _courseList.postValue(errorDayCourses)
                        onFailure?.invoke("空响应")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure?.invoke(e.message ?: "未知错误")
            }
        }
    }

}
