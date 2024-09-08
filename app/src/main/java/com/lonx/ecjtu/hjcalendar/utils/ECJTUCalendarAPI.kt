package com.lonx.ecjtu.hjcalendar.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.InetAddress
import java.net.Socket
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ECJTUCalendarAPI {

    suspend fun getCourseInfo(weiXinID: String, date: String): String? = withContext(Dispatchers.IO) {
        try {
            val url="https://jwxt.ecjtu.edu.cn/weixin/CalendarServlet?weiXinID=$weiXinID&date=$date"
//            Log.e("getCourseInfo", "URL: $url")
            val doc: Document = Jsoup.connect(url)
                .sslSocketFactory(SSLSocketFactoryCompat())
                .get()
            return@withContext doc.html()
        } catch (e: Exception){
            Log.e("getCourseInfo", "Error fetching course info: ${e.message}")
            return@withContext ""
        }

    }
    fun parseHtml(html: String): DayCourses {
        val doc: Document = Jsoup.parse(html)
        val courseElements = doc.select("ul.rl_info li")
        val courseList = mutableListOf<CourseInfo>()
//        val gson = GsonBuilder().setPrettyPrinting().create()

        // 获取日期信息
        val dateElement = doc.select("div.center").text() ?: "N/A"

        // 如果没有课程信息或者只有图片，返回“今日无课”
        if (courseElements.isEmpty() || courseElements.all { it.select("img").isNotEmpty() }) {
            return DayCourses(dateElement, listOf(CourseInfo("今日无课", "N/A", "N/A", "N/A", "N/A")))
        }

        // 解析课程信息
        courseElements.forEach { element ->
            try {
                val classTime = element.toString().substringAfter("时间：").substringBefore("<br>").split(" ")[1].trim()
                val courseName = element.toString().substringAfter("</span>").substringBefore("<br>").trim()
                val classWeek = element.toString().substringAfter("时间：").substringBefore("<br>").split(" ")[0].trim()
                val location = element.toString().substringAfter("地点：").substringBefore("<br>").trim()
                val teacher = element.toString().substringAfter("教师：").substringBefore("<br>").trim()

                courseList.add(CourseInfo(courseName, "节次：$classTime", "上课周：$classWeek", "地点：$location", "教师：$teacher"))
            } catch (e: Exception) {
                Log.e("parseHtml", "Error parsing course element: ${e.message}")
            }
        }

        return DayCourses(dateElement, courseList.distinct())
    }

    class SSLSocketFactoryCompat : SSLSocketFactory() {
        private val trustManager = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d("SSLSocketFactoryCompat", "checkClientTrusted")
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d("SSLSocketFactoryCompat", "checkServerTrusted")
            }
        }

        private val sslContext: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), java.security.SecureRandom())
        }

        private val delegate: SSLSocketFactory = sslContext.socketFactory

        override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

        override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
            delegate.createSocket(s, host, port, autoClose)

        override fun createSocket(host: String, port: Int): Socket = delegate.createSocket(host, port)

        override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
            delegate.createSocket(host, port, localHost, localPort)

        override fun createSocket(host: InetAddress, port: Int): Socket = delegate.createSocket(host, port)

        override fun createSocket(host: InetAddress, port: Int, localHost: InetAddress, localPort: Int): Socket =
            delegate.createSocket(host, port, localHost, localPort)
    }

}
data class CourseInfo(
    val courseName: String,
    val classTime: String,
    val classWeek: String,
    val location: String,
    val teacher: String
)
data class DayCourses(
    val date: String,
    val courses: List<CourseInfo>
)
