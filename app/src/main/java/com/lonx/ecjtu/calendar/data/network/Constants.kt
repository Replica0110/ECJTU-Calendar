package com.lonx.ecjtu.calendar.data.network

object Constants {

    const val BASE_URL = "https://jwxt.ecjtu.edu.cn" // 教务系统基础Url
    const val CALENDAR_URL = "${BASE_URL}/weixin/CalendarServlet" // 日历查询接口
    const val ACADEMIC_CALENDAR_URL = "${BASE_URL}/weixin/xiaoli.html"  // 校历页面

    const val SCORE_URL = "${BASE_URL}/weixin/ScoreQuery" // 成绩查询接口

    const val SELECTED_COURSE_URL = "${BASE_URL}/weixin/ElectiveCourseCl"
}