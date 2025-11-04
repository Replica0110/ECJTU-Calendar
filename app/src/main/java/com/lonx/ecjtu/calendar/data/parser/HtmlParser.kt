package com.lonx.ecjtu.calendar.data.parser

import android.util.Log
import com.lonx.ecjtu.calendar.data.dto.CourseItemDTO
import com.lonx.ecjtu.calendar.data.dto.ScheduleDTO
import com.lonx.ecjtu.calendar.data.dto.ScoreDTO
import com.lonx.ecjtu.calendar.data.dto.ScorePageData
import org.jsoup.Jsoup
import java.time.LocalDate

class HtmlParser {
    fun parseSchedulePage(htmlContent: String): ScheduleDTO {
        val document = Jsoup.parse(htmlContent)

        val title = document.select("title").first()?.text() ?: "未知标题"
        val dateInfo = document.select("div.center").first()?.text() ?: "未知日期"

        val dateRegex = Regex("(\\d{4}-\\d{2}-\\d{2})\\s+([^（]+)（第(\\d+)周）")

        val (date, weekDay, weekNum) = dateRegex.find(dateInfo)
            ?.let { Triple(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
            ?: Triple(LocalDate.now().toString(), LocalDate.now().dayOfWeek.name, "")

        val dayOfWeek =dateInfo.split(" ")[1]

        val courseList = mutableListOf<CourseItemDTO>()

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
                val course = CourseItemDTO(
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
        return ScheduleDTO(
            title = title,
            dateInfo = Triple(date, weekDay, weekNum),
            courses = courseList
        )
    }

    /**
     * 解析校历网页，获取图片链接
     */
    fun parseAcademicCalendarImageUrl(htmlContent: String): String? {
        return try {
            val document = Jsoup.parse(htmlContent)
            val imgElement = document.select("img").first()
            val src = imgElement?.attr("src")
            Log.d("HtmlParser", "从网页中解析到图片链接: $src")
            src
        } catch (e: Exception) {
            Log.e("HtmlParser", "解析校历图片链接失败", e)
            null
        }
    }

    fun parseScorePage(htmlContent: String): ScorePageData? {
        return try {
            val document = Jsoup.parse(htmlContent)

            val noScoreElement = document.select("a.btn-info:contains(对不起,该学期你暂无成绩！)")
            if (noScoreElement.isNotEmpty()) {
                return ScorePageData(
                    scores = emptyList(),
                    availableTerms = emptyList(),
                    currentTerm = "",
                    error = "对不起,该学期你暂无成绩！"
                )
            }

            val termElements = document.select("ul.dropdown-menu li a")
            val availableTerms = termElements.map { it.text().trim() }

            val currentTerm = document.select("div.right span").last()?.text()?.trim() ?: ""

            // 3. Parse the list of scores
            val scoreList = mutableListOf<ScoreDTO>()
            val scoreRows = document.select("div.row")

            for (row in scoreRows) {
                try {
                    val courseFullName = row.select("span.course").first()?.text() ?: continue
                    val gradeDiv = row.select("div.grade").first()
                    val scores = gradeDiv?.select("span.score")?.map { it.text() } ?: listOf("", "", "")
                    val finalScore = scores.getOrElse(0) { "" }
                    val retakeScore = scores.getOrElse(1) { "" }
                    val relearnScore = scores.getOrElse(2) { "" }
                    val courseType = row.select("span.require mark").first()?.text() ?: ""
                    val courseCodeRegex = Regex("【(\\d+)】")
                    val courseNameRegex = Regex("】[^】]*】(.+?)(?:（|\\(|$)")
                    val creditRegex = Regex("学分:([\\d.]+)")
                    val courseCode = courseCodeRegex.find(courseFullName)?.groupValues?.get(1) ?: ""
                    val courseName = courseNameRegex.find(courseFullName)?.groupValues?.get(1)?.trim() ?: ""
                    val credit = creditRegex.find(courseFullName)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                    Log.d("HtmlParser", "Course Code: $courseCode")
                    Log.d("HtmlParser", "Course Name: $courseName")
                    Log.d("HtmlParser", "Credit: $credit")
                    scoreList.add(
                        ScoreDTO(
                            courseName = courseName,
                            courseCode = courseCode,
                            credit = credit,
                            finalScore = finalScore,
                            retakeScore = retakeScore,
                            relearnScore = relearnScore,
                            courseType = courseType
                        )
                    )
                } catch (e: Exception) {
                    Log.w("HtmlParser", "Failed to parse a single score item.", e)
                    continue
                }
            }

            ScorePageData(
                scores = scoreList,
                availableTerms = availableTerms,
                currentTerm = currentTerm
            )
        } catch (e: Exception) {
            Log.e("HtmlParser", "Failed to parse score page HTML.", e)
            null
        }
    }
}