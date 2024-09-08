package com.lonx.ecjtu.hjcalendar.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.utils.CourseInfo
import com.lonx.ecjtu.hjcalendar.utils.CourseResponse
import com.lonx.ecjtu.hjcalendar.utils.ECJTUCalendarAPI
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val _courseList = MutableLiveData<List<CourseResponse>>()
    val courseList: LiveData<List<CourseResponse>> = _courseList

    private val api = ECJTUCalendarAPI()

    // 添加回调参数
    fun fetchCourseInfo(weiXinID: String, callback: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val html = api.getCourseInfo(weiXinID)
                if (html != null) {
                    if (html.isNotBlank()) {
                        if (html.contains("<title>教务处微信平台绑定</title>")) {
                            // 使用 ToastUtil 显示 Toast
                            ToastUtil.showToast(getApplication(), "课程获取失败，请检查weiXinID是否正确")
                            callback?.invoke()
                            return@launch
                        } else {
                            ToastUtil.showToast(getApplication(), "课程获取成功")
                            val courseResponse = api.parseHtml(html)
                            // TODO 解析数据得到courseResponse
                            val courses = courseResponse.courses
                            val date = courseResponse.date
                            _courseList.postValue(listOf(courseResponse))
//                            Log.e("fetchCourseInfo", "Date: $date")
                            callback?.invoke()
                        }
                    } else {
                        ToastUtil.showToast(getApplication(), "课程获取失败，请检查网络连接")
                        callback?.invoke()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToast(getApplication(), "课程获取失败，请检查网络连接")
                callback?.invoke()
            }
        }
    }

}
