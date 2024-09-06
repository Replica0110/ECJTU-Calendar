package com.lonx.ecjtu.calendar.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lonx.ecjtu.calendar.utils.CourseInfo
import com.lonx.ecjtu.calendar.utils.ECJTUCalendarAPI
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    val courseList: List<CourseInfo>
        get() {
            val sharedPreferences = getApplication<Application>().getSharedPreferences("CourseData", Context.MODE_PRIVATE)
            val courseData = sharedPreferences.getString("courseInfo", "")
            Log.d("SettingsViewModel", "Course data: $courseData")
            return if (courseData.isNullOrBlank()) {
                emptyList()
            } else {
                Gson().fromJson(courseData, Array<CourseInfo>::class.java).toList()
            }
        }

    fun updateCourseInfo(newId: String) {
        getCourseData(newId)
    }

    // 获取课程信息并保存
    private fun getCourseData(weixinID: String) {
        viewModelScope.launch {
            try {
                // 获取当天的课程信息
                val api = ECJTUCalendarAPI()
                val html=api.getCourseInfo(weixinID)
                Log.d("SettingsViewModel", "HTML: $html")
                if (html != null) {
                    Toast.makeText(getApplication(), "正在获取课表，请稍后...", Toast.LENGTH_SHORT).show()
                    if (html.isNotBlank()){
                        if (html.contains("<title>教务处微信平台绑定</title>")) {
                            Toast.makeText(getApplication(), "课表获取失败，请检查weXinID是否正确", Toast.LENGTH_LONG).show()
                            return@launch
                        }
                        val courseInfoJson = api.parseHtml(html)
                        saveCourseData(courseInfoJson)
                        Toast.makeText(getApplication(), "课表获取成功", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(getApplication(), "课表获取失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error fetching course data: $e")
            }
        }
    }

    private fun saveCourseData(courseData: String) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("CourseData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("courseInfo", courseData)
        editor.apply()

        val courses = Gson().fromJson(courseData, Array<CourseInfo>::class.java).toList()
        logCourseList(courses)
    }

    private fun logCourseList(courseList: List<CourseInfo>?) {
        courseList?.let {
            val courseListString = it.joinToString { course -> course.toString() }
        } ?: Log.e("SettingsViewModel", "Course data is null")
    }
}
