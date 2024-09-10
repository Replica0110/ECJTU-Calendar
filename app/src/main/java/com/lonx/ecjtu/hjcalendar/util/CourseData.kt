package com.lonx.ecjtu.hjcalendar.util


class CourseData {
    data class CourseInfo(
        val courseName: String,
        val courseTime: String="N/A",
        val courseWeek: String="N/A",
        val courseLocation: String="N/A",
        val courseTeacher: String="N/A"
    )
    data class DayCourses(
        val date: String,
        val courses: List<CourseInfo>
    )

}

