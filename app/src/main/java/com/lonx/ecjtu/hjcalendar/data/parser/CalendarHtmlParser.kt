package com.lonx.ecjtu.hjcalendar.data.parser


import com.lonx.ecjtu.hjcalendar.data.model.Course
import com.lonx.ecjtu.hjcalendar.data.model.DailySchedule
import org.jsoup.Jsoup
import com.lonx.ecjtu.hjcalendar.data.model.ScheduleResult

class CalendarHtmlParser {
    fun parse(html: String): ScheduleResult {
        try {
            val doc = Jsoup.parse(html)

            if (doc.title().contains("教务处微信平台绑定")) {
                throw InvalidWeiXinIdException()
            }

            val dateElement = doc.selectFirst("div.center")?.text() ?: ""
            val courseElements = doc.select("ul.rl_info li:not(:has(img))")

            val courseList = courseElements.mapNotNull { element ->
                try {
                    val pElement = element.selectFirst("p") ?: return@mapNotNull null
                    val pHtml = pElement.html()
                    val courseName = pHtml.substringAfter("</span>", "").substringBefore("<br>").trim()

                    if (courseName.isEmpty()) return@mapNotNull null

                    val pText = pElement.text()
                    val timeLine = pText.substringAfter("时间：", "").substringBefore("地点：").trim()
                    val location = pText.substringAfter("地点：", "").substringBefore("教师：").trim()
                    val teacher = pText.substringAfter("教师：", "").trim()

                    val parts = timeLine.split(" ")
                    val courseTime = parts.getOrNull(1) ?: ""
                    val courseWeek = parts.getOrNull(0) ?: ""

                    Course(
                        name = courseName,
                        time = courseTime,
                        week = courseWeek,
                        location = location,
                        teacher = teacher
                    )
                } catch (e: Exception) {
                    null
                }
            }.distinct()

            val schedule = DailySchedule(dateElement, courseList)
            return if (courseList.isEmpty()) {
                ScheduleResult.Empty(dateElement)
            } else {
                ScheduleResult.Success(schedule)
            }
        } catch (e: InvalidWeiXinIdException) {
            return ScheduleResult.Error("", "无效的微信ID")
        } catch (e: Exception) {
            return ScheduleResult.Error("", "解析课程数据时出错：${e.message}")
        }
    }
}

class InvalidWeiXinIdException : Exception("无效的weiXinID")