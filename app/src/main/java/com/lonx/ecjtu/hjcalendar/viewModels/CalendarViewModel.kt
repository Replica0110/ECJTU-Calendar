package com.lonx.ecjtu.hjcalendar.viewModels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.api.CourseInfo
import com.lonx.ecjtu.hjcalendar.api.ECJTUCalendarAPI
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val _courseList = MutableLiveData<List<CourseInfo>>()
    val courseList: LiveData<List<CourseInfo>> = _courseList

    private val api = ECJTUCalendarAPI()

    fun fetchCourseInfo(weiXinID: String) {
        viewModelScope.launch {
            try {
                val html = api.getCourseInfo(weiXinID)
                if (html!= null){
                    Toast.makeText(getApplication(), "正在获取课程信息，请稍后...", Toast.LENGTH_SHORT).show()
                    if (html.isNotBlank()){
                        if (html.contains("<title>教务处微信平台绑定</title>")){
                            Toast.makeText(getApplication(), "课程获取失败，请检查weiXinID是否正确", Toast.LENGTH_SHORT).show()
                            return@launch
                        }else{
                            val parsedData = api.parseHtml(html)
                            val courses = Gson().fromJson(parsedData, Array<CourseInfo>::class.java).toList()
                            _courseList.postValue(courses)
                            Toast.makeText(getApplication(), "课程获取成功", Toast.LENGTH_SHORT).show()
                        }
                        }else {
                            Toast.makeText(getApplication(), "课程获取失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "课程获取失败，请检查网络连接", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

