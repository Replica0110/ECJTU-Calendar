package com.lonx.ecjtu.calendar.data.repository

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.mapper.toDomain
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import com.lonx.ecjtu.calendar.data.network.Constants
import com.lonx.ecjtu.calendar.data.repository.utils.requireWeiXinId
import com.lonx.ecjtu.calendar.data.repository.utils.safeApiCall
import org.jsoup.Jsoup
import java.time.LocalDate

class CalendarRepositoryImpl(
    private val jwxtDataSource: JwxtDataSource,
    private val localDataSource: LocalDataSource,
    private val htmlParser: HtmlParser
) : CalendarRepository {

    override suspend fun getCourses(date: LocalDate): Result<SchedulePage> = safeApiCall {
        val weiXinId = localDataSource.requireWeiXinId().getOrThrow()

        val htmlContent = jwxtDataSource.fetchHtml(
            url = Constants.CALENDAR_URL,
            params = mapOf(
                "weiXinID" to weiXinId,
                "date" to date.toString()
            )
        ).getOrElse { throw it }

        val document = Jsoup.parse(htmlContent)
        val title = document.select("title").first()?.text() ?: "未知标题"
        if (title == "教务处微信平台绑定") throw Exception("WeiXinID无效或未绑定")

        htmlParser.parseSchedulePage(htmlContent).toDomain()
    }

    override suspend fun saveWeiXinID(weiXinId: String) {
        localDataSource.saveWeiXinID(weiXinId)
    }

    override fun getWeiXinID() = localDataSource.getWeiXinID()

    override fun getAutoUpdateCheckSetting() = localDataSource.getAutoUpdateCheckEnabled()

    override suspend fun saveAutoUpdateCheckSetting(enabled: Boolean) =
        localDataSource.setAutoUpdateCheckEnabled(enabled)

    override suspend fun saveColorModeSetting( mode: Int) =
        localDataSource.saveColorModeSetting(mode)

    override fun getColorModeSetting() = localDataSource.getColorModeSetting()

    override suspend fun saveKeyColorIndex(index: Int) = localDataSource.saveKeyColorIndex(index)
    override fun getKeyColorIndex() = localDataSource.getKeyColorIndex()
}
