package com.lonx.ecjtu.calendar.data.repository

import android.util.Log
import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.mapper.toDomain
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.domain.error.CalendarError
import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import com.lonx.ecjtu.calendar.data.network.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.jsoup.Jsoup
import rxhttp.wrapper.exception.HttpStatusCodeException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarRepositoryImpl(
    private val jwxtDataSource: JwxtDataSource,
    private val localDataSource: LocalDataSource,
    private val htmlParser: HtmlParser
) : CalendarRepository {
    override suspend fun getCourses(date: LocalDate): Result<SchedulePage> {
        return try {
            val weiXinId = localDataSource.getWeiXinID().first()
            if (weiXinId.isBlank()) {
                return Result.failure(CalendarError.NoWeiXinId())
            }

            val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE) // YYYY-MM-DD


            val htmlContent = jwxtDataSource.fetchHtml(
                url = Constants.CALENDAR_URL,
                params = mapOf(
                    "weiXinID" to weiXinId,
                    "date" to formattedDate
                )
            ).fold(
                onSuccess = { it },
                onFailure = { throwable ->
                    val error = when (throwable) {
                        is UnknownHostException,
                        is SocketTimeoutException,
                        is HttpStatusCodeException,
                        is java.io.IOException -> CalendarError.NetworkError(throwable)
                        else -> CalendarError.UnknownError(Exception(throwable.message ?: "未知错误", throwable))
                    }
                    return Result.failure(error)
                }
            )
            val document = Jsoup.parse(htmlContent)

            val title = document.select("title").first()?.text() ?: "未知标题"
            if (title =="教务处微信平台绑定"){
                return Result.failure(CalendarError.WeiXinIdInvalid())
            }

            val scheduleDto = htmlParser.parseSchedulePage(htmlContent)

            // 将 DTO 映射为 Domain Model
            val schedulePage = scheduleDto.toDomain()

            Result.success(schedulePage)
        } catch (e: Exception) {
            Log.e("CourseDataSource", "Error fetching calendar html: $e")
            val error = when (e) {
                is CalendarError -> e
                is UnknownHostException,
                is SocketTimeoutException,
                is HttpStatusCodeException,
                is java.io.IOException -> CalendarError.NetworkError(e)
                else -> CalendarError.UnknownError(e)
            }
            Result.failure(error)
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