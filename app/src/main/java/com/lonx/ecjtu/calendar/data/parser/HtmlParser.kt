package com.lonx.ecjtu.calendar.data.parser

import com.lonx.ecjtu.calendar.data.dto.CourseItemDTO
import com.lonx.ecjtu.calendar.data.dto.ScheduleDTO
import com.lonx.ecjtu.calendar.data.dto.ScoreDTO
import com.lonx.ecjtu.calendar.data.dto.ScorePageData
import com.lonx.ecjtu.calendar.data.dto.SelectedCourseDTO
import com.lonx.ecjtu.calendar.data.dto.SelectedCoursePageData
import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
import org.jsoup.Jsoup
import java.time.LocalDate

class HtmlParser {
    fun parseSchedulePage(htmlContent: String): ScheduleDTO {
        Logger.logParseStart(Tags.PARSER, "parseSchedulePage")
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
        Logger.logParseSuccess(Tags.PARSER, courseList.size, "门课程")
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
        Logger.logParseStart(Tags.PARSER, "parseAcademicCalendarImageUrl")
        return try {
            val document = Jsoup.parse(htmlContent)
            val imgElement = document.select("img").first()
            val src = imgElement?.attr("src")
            Logger.d(Tags.PARSER, "从网页中解析到图片链接: $src")
            src
        } catch (e: Exception) {
            Logger.e(Tags.PARSER, "解析校历图片链接失败", e)
            null
        }
    }

    fun parseScorePage(htmlContent: String): ScorePageData? {
        Logger.logParseStart(Tags.PARSER, "parseScorePage")
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

                    Logger.d(Tags.PARSER, "课程代码: $courseCode, 课程名称: $courseName, 学分: $credit")
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
                    Logger.logSkipInvalid(Tags.PARSER, "单条成绩解析失败: ${e.message}")
                    continue
                }
            }

            Logger.logParseSuccess(Tags.PARSER, scoreList.size, "门课程成绩")
            ScorePageData(
                scores = scoreList,
                availableTerms = availableTerms,
                currentTerm = currentTerm
            )
        } catch (e: Exception) {
            Logger.e(Tags.PARSER, "解析成绩页面失败", e)
            null
        }
    }

    fun parseSelectedCoursePage(htmlContent: String): SelectedCoursePageData? {
        Logger.logParseStart(Tags.PARSER, "parseSelectedCoursePage")
        return try {
            val document = Jsoup.parse(htmlContent)

            // 检查是否无已选课程
            val noSelectedCourseElement =
                document.select("a.btn-info:contains(该学期你暂无已选课程！)")
            if (noSelectedCourseElement.isNotEmpty()) {
                return SelectedCoursePageData(
                    courses = emptyList(),
                    availableTerms = emptyList(),
                    currentTerm = "",
                    error = "该学期你暂无已选课程！"
                )
            }

            // 学期下拉框
            val termElements = document.select("ul.dropdown-menu li a")
            val availableTerms = termElements.map { it.text().trim() }

            // 当前学期
            val currentTerm = document.select("div.right span").last()?.text()?.trim() ?: ""

            // 解析课程列表
            val courseList = mutableListOf<SelectedCourseDTO>()
            val courseRows = document.select("div.row")

            for (row in courseRows) {
                try {
                    val textBlock = row.select("div.text").first() ?: continue

                    // 课程名称和其余参数存在 a 标签的 href 中
                    val courseLink = textBlock.select("a").first()
                    val courseName = courseLink?.text()?.trim() ?: ""

                    // href 参数
                    val href = courseLink?.attr("href") ?: ""

                    val requireRegex = Regex("courseRequire=([^&]+)")
                    val periodRegex = Regex("period=([\\d.]+)")
                    val creditRegex = Regex("creditHour=([\\d.]+)")
                    val selectedTypeRegex = Regex("courseSelectType=([^&]+)")
                    val selectTypeShowRegex = Regex("selectTypeShow=([^&]+)")
                    val checkTypeRegex = Regex("checkType=([^&]+)")
                    val yiXuanRegex = Regex("yiXuan=([^&]+)")

                    val courseRequire = requireRegex.find(href)?.groupValues?.get(1) ?: ""
                    val period =
                        periodRegex.find(href)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    val credit =
                        creditRegex.find(href)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    val selectedType = selectedTypeRegex.find(href)?.groupValues?.get(1) ?: ""
                    val courseType = selectTypeShowRegex.find(href)?.groupValues?.get(1) ?: ""
                    val checkType = checkTypeRegex.find(href)?.groupValues?.get(1) ?: ""
                    val isSelected = yiXuanRegex.find(href)?.groupValues?.get(1) ?: ""

                    // 解析教师、时间、教学班
                    val teacher =
                        textBlock.select("strong:contains(任课教师:) + span").text().trim()
                    val classTime =
                        textBlock.select("strong:contains(上课时间:) + span").text().trim()
                    val className =
                        textBlock.select("strong:contains(教学班名称:) + span").text().trim()

                    val dto = SelectedCourseDTO(
                        courseName = courseName,
                        courseRequire = courseRequire,
                        period = period,
                        credit = credit,
                        selectedType = selectedType,
                        courseType = courseType,
                        checkType = checkType,
                        courseTeacher = teacher,
                        isSelected = isSelected,
                        className = className,
                        classTime = classTime
                    )

                    courseList.add(dto)
                } catch (e: Exception) {
                    Logger.logSkipInvalid(Tags.PARSER, "单条选课解析失败: ${e.message}")
                    continue
                }
            }

            Logger.logParseSuccess(Tags.PARSER, courseList.size, "门已选课程")
            SelectedCoursePageData(
                courses = courseList,
                availableTerms = availableTerms,
                currentTerm = currentTerm
            )

        } catch (e: Exception) {
            Logger.e(Tags.PARSER, "解析已选课程页面失败", e)
            null
        }
    }

}