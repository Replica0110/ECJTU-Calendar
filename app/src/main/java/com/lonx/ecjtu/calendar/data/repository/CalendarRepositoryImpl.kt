package com.lonx.ecjtu.calendar.data.repository

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.CourseDataSource
import com.lonx.ecjtu.calendar.data.model.CourseItem
import com.lonx.ecjtu.calendar.data.model.Schedule
import com.lonx.ecjtu.calendar.data.network.HtmlParser
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarRepositoryImpl(
    private val courseDataSource: CourseDataSource,
    private val localDataSource: LocalDataSource,
    private val htmlParser: HtmlParser
) : CalendarRepository {
    private val calendarUrl = "https://jwxt.ecjtu.edu.cn/weixin/CalendarServlet"
    override suspend fun getCourses(date: LocalDate): Result<SchedulePage> {
        return try {
            val weiXinId = localDataSource.getWeiXinID().first()
            if (weiXinId.isBlank()) {
                return Result.failure(Exception("请先在设置中配置微信ID"))
            }

            val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE) // YYYY-MM-DD


            val htmlContent = courseDataSource.fetchCalendarHtml(
                url = calendarUrl,
                params = mapOf(
                    "weiXinID" to weiXinId,
                    "date" to formattedDate
                )
            )

            val schedulePageDTO = htmlParser.parseSchedulePage(htmlContent)

            // 将 DTO 映射为 Domain Model
            val schedulePage = schedulePageDTO.toDomainModel()

            Result.success(schedulePage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveWeiXinID(weiXinId: String) {
        localDataSource.saveWeiXinID(weiXinId)
    }

    override fun getWeiXinID(): Flow<String> {
        return localDataSource.getWeiXinID()
    }
    override fun getAutoUpdateCheckSetting(): Flow<Boolean> {
        return localDataSource.getAutoUpdateCheckEnabled()
    }

    override suspend fun saveAutoUpdateCheckSetting(enabled: Boolean) {
        localDataSource.setAutoUpdateCheckEnabled(enabled)
    }
}

private fun Schedule.toDomainModel(): SchedulePage {
    return SchedulePage(
        dateInfo = this.dateInfo,
        courses = this.courses.map { it.toDomainModel() }
    )
}

private fun CourseItem.toDomainModel(): Course {
    return Course(
        time = this.time,
        name = this.name,
        location = this.location,
        teacher = this.teacher,
        duration = this.courseWeek,
        dayOfWeek = this.dayOfWeek
    )
}