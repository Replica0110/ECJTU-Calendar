package com.lonx.ecjtu.calendar.data.network

import androidx.compose.runtime.remember
import com.lonx.ecjtu.calendar.data.model.CourseItem
import com.lonx.ecjtu.calendar.data.model.Schedule
import org.jsoup.Jsoup
import java.time.LocalDate


class HtmlParser {

    fun parseSchedulePage(htmlContent: String): Schedule {
        val document = Jsoup.parse(htmlContent)

        val title = document.select("title").first()?.text() ?: "未知标题"
        val dateInfo = document.select("div.center").first()?.text() ?: "未知日期"

        val dateRegex = Regex("(\\d{4}-\\d{2}-\\d{2})\\s+([^（]+)（第(\\d+)周）")

        val (date, weekDay, weekNum) = dateRegex.find(dateInfo)
            ?.let { Triple(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
            ?: Triple(LocalDate.now().toString(), LocalDate.now().dayOfWeek.name, "")

        val dayOfWeek =dateInfo.split(" ")[1]

        val courseList = mutableListOf<CourseItem>()

        val listItems = document.select("ul.rl_info li:not(:has(img))")

        for (item in listItems) {
            try {
                // 3. 获取 p 标签元素
                val pElement = item.select("p").first() ?: continue

                // 4. 使用 HTML 内容进行更精确的解析
                val pHtml = pElement.html()
                val courseName = pHtml.substringAfter("</span>", "")
                    .substringBefore("<br>")
                    .trim()

                // 如果课程名为空，则跳过
                if (courseName.isEmpty()) continue

                // 5. 使用文本内容提取详细信息
                val pText = pElement.text()
                val timeLine = pText.substringAfter("时间：", "")
                    .substringBefore("地点：")
                    .trim()

                val location = pText.substringAfter("地点：", "")
                    .substringBefore("教师：")
                    .trim()

                val teacher = pText.substringAfter("教师：", "")
                    .trim()

                // 6. 分割时间信息
                val parts = timeLine.split(" ")
                val courseTime = parts.getOrNull(1) ?: ""
                val courseWeek = parts.getOrNull(0) ?: ""
                val course = CourseItem(
                    time = courseTime,
                    name = courseName,
                    courseWeek = courseWeek,
                    location = location,
                    teacher = teacher,
                    dayOfWeek = dayOfWeek
                )
                courseList.add(course)
            } catch (e: Exception) {
                // 忽略单个课程解析错误，继续处理其他课程
                continue
            }
        }

        return Schedule(dateInfo = Triple(date, weekDay, weekNum), courses = courseList, title = title)
    }
}
