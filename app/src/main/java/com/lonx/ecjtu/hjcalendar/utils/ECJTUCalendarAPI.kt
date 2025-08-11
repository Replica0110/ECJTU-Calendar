package com.lonx.ecjtu.hjcalendar.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ECJTUCalendarAPI {

    suspend fun getCourseHtml(weiXinID: String, date: String): String? = withContext(Dispatchers.IO) {
        try {
            val url="https://jwxt.ecjtu.edu.cn/weixin/CalendarServlet?weiXinID=$weiXinID&date=$date"
//            Log.e("getCourseInfo", "URL: $url")
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val doc: Document = Jsoup.connect(url)
                .sslSocketFactory(sslContext.socketFactory)
                .get()
            return@withContext doc.html()
        } catch (e: Exception) {
            Log.e("getCourseInfo", "Error fetching course info: ${e.message}")
            return@withContext ""
        }

    }
    fun parseCourseHtml(html: String): CourseData.DayCourses {
        val doc: Document = Jsoup.parse(html)
        val courseElements = doc.select("ul.rl_info li")
        val courseList = mutableListOf<CourseData.CourseInfo>()
//        val gson = GsonBuilder().setPrettyPrinting().create()

        // 获取日期信息
        val dateElement = doc.select("div.center").text()

        // 如果没有课程信息或者只有图片，返回“今日无课”
        if (courseElements.isEmpty() || courseElements.all { it.select("img").isNotEmpty() }) {
            return CourseData.DayCourses(
                dateElement,
                emptyList()
            )
        }

        // 解析课程信息
        courseElements.forEach { element ->
            try {
                val classTime = element.toString().substringAfter("时间：").substringBefore("<br>").split(" ")[1].trim()
                val courseName = element.toString().substringAfter("</span>").substringBefore("<br>").trim()
                val classWeek = element.toString().substringAfter("时间：").substringBefore("<br>").split(" ")[0].trim()
                val location = element.toString().substringAfter("地点：").substringBefore("<br>").trim()
                val teacher = element.toString().substringAfter("教师：").substringBefore("<br>").trim()

                courseList.add(
                    CourseData.CourseInfo(
                        courseName,
                        "节次：$classTime",
                        "上课周：$classWeek",
                        "地点：$location",
                        "教师：$teacher"
                    )
                )
            } catch (e: Exception) {
                Log.e("parseHtml", "Error parsing course element: ${e.message}")
            }
        }

        return CourseData.DayCourses(dateElement, courseList.distinct())
    }

    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )
}

