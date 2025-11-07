package com.lonx.ecjtu.calendar.data.repository

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.mapper.toDomain
import com.lonx.ecjtu.calendar.data.network.Constants.SCORE_URL
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.data.repository.utils.requireWeiXinId
import com.lonx.ecjtu.calendar.data.repository.utils.safeApiCall
import com.lonx.ecjtu.calendar.domain.model.ScorePage
import com.lonx.ecjtu.calendar.domain.repository.ScoreRepository

class ScoreRepositoryImpl(
    private val jwxtDataSource: JwxtDataSource,
    private val localDataSource: LocalDataSource,
    private val htmlParser: HtmlParser
) : ScoreRepository {

    override suspend fun getScores(term: String?): Result<ScorePage> = safeApiCall {
        val weiXinId = localDataSource.requireWeiXinId().getOrThrow()

        val params = mutableMapOf("weiXinID" to weiXinId)
        term?.takeIf { it.isNotBlank() }?.let { params["term"] = it }

        val htmlContent = jwxtDataSource.fetchHtml(SCORE_URL, params)
            .getOrElse { throw it }

        val parsedDto = htmlParser.parseScorePage(htmlContent)
            ?: throw Exception("无法解析成绩页面HTML")

        parsedDto.error?.let { throw Exception(it) }

        parsedDto.toDomain()
    }
}