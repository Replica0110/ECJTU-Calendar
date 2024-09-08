package com.lonx.ecjtu.hjcalendar.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.hjcalendar.utils.CourseResponse
import com.lonx.ecjtu.hjcalendar.utils.ECJTUCalendarAPI
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val _courseList = MutableLiveData<List<CourseResponse>>()
    val courseList: LiveData<List<CourseResponse>> = _courseList

    private val api = ECJTUCalendarAPI()

    // 添加回调参数
    fun fetchCourseInfo(
        weiXinID: String,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val html = api.getCourseInfo(weiXinID)
                Log.e("fetchCourseInfo", "HTML: $html")
                if (html != null) {
                    if (html.isNotBlank()) {
                        if (html.contains("<title>教务处微信平台绑定</title>")) {
                            Log.e("fetchCourseInfo", "Invalid weiXinID")
                            onFailure?.invoke("无效的weiXinID")
                        } else {
                            val courseResponse = api.parseHtml(html)
                            _courseList.postValue(listOf(courseResponse))
                            onSuccess?.invoke("日历已更新")
                        }
                    } else {
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
