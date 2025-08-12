package com.lonx.ecjtu.hjcalendar.data.parser

import com.lonx.ecjtu.hjcalendar.utils.CourseData
import org.jsoup.Jsoup

class CalendarHtmlParser {

    fun parse(html: String): CourseData.DayCourses {
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

                // 4. 使用更健壮的方法从纯文本中提取字段
                val timeLine = pText.substringAfter("时间：", "").substringBefore("地点：").trim()
                val location = pText.substringAfter("地点：", "").substringBefore("教师：").trim()
                val teacher = pText.substringAfter("教师：", "").trim()

                val courseTime = timeLine.split(" ").getOrNull(1) ?: ""
                val courseWeek = timeLine.split(" ").getOrNull(0) ?: ""

                CourseData.CourseInfo(
                    courseName,
                    "节次：$courseTime",
                    "上课周：$courseWeek",
                    "地点：$location",
                    "教师：$teacher"
                )
            } catch (e: Exception) {
                null
            }
        }

        return CourseData.DayCourses(dateElement, courseList.distinct())
    }
}

class InvalidWeiXinIdException : Exception("无效的weiXinID")