package com.lonx.ecjtu.calendar.data.repository

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.local.ScoreDao
import com.lonx.ecjtu.calendar.data.datasource.local.toDomain
import com.lonx.ecjtu.calendar.data.datasource.local.toEntity
import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.mapper.toDomain
import com.lonx.ecjtu.calendar.data.network.Constants.SCORE_URL
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.data.repository.utils.requireWeiXinId
import com.lonx.ecjtu.calendar.data.repository.utils.safeApiCall
import com.lonx.ecjtu.calendar.domain.model.ScorePage
import com.lonx.ecjtu.calendar.domain.repository.ScoreRepository
import kotlinx.coroutines.flow.first

class ScoreRepositoryImpl(
    private val jwxtDataSource: JwxtDataSource,
    private val localDataSource: LocalDataSource,
    private val htmlParser: HtmlParser,
    private val scoreDao: ScoreDao
) : ScoreRepository {

    override suspend fun getScores(term: String?): Result<ScorePage> = safeApiCall {
        val weiXinId = localDataSource.requireWeiXinId().getOrThrow()

        val params = mutableMapOf("weiXinID" to weiXinId)

        // 如果学期参数为空，执行完整刷新：首先不带学期参数获取页面以获得所有可选学期，
        // 然后遍历这些学期分别获取成绩并保存到数据库。
        if (term.isNullOrBlank()) {
            // 初始获取以获得可选学期列表
            val htmlContent = jwxtDataSource.fetchHtml(SCORE_URL, params)
                .getOrElse { throw it }

            val parsedDto = htmlParser.parseScorePage(htmlContent)
                ?: throw Exception("无法解析成绩页面HTML")

            parsedDto.error?.let { throw Exception(it) }

            val domainPage = parsedDto.toDomain()

            // 遍历所有可选学期并分别获取/保存
            domainPage.availableTerms.forEach { t ->
                val p = params.toMutableMap()
                p["term"] = t
                val htmlForTerm = jwxtDataSource.fetchHtml(SCORE_URL, p)
                    .getOrElse { null }

                if (htmlForTerm == null) {
                    // 获取此学期时网络错误，跳过
                    return@forEach
                }

                val parsedForTerm = htmlParser.parseScorePage(htmlForTerm)
                if (parsedForTerm == null || parsedForTerm.error != null) {
                    // 解析错误或服务器端错误：删除该学期数据
                    scoreDao.deleteScoresByTerm(t)
                    return@forEach
                }

                val scores = parsedForTerm.toDomain().scores

                if (scores.isEmpty()) {
                    scoreDao.deleteScoresByTerm(t)
                    // 删除该学期的存储时间戳
                    localDataSource.removeScoreLastRefresh(t)
                } else {
                    // 删除该学期的旧条目然后插入新条目
                    scoreDao.deleteScoresByTerm(t)
                    val entities = scores.map { s -> s.toEntity(t) }
                    scoreDao.insertScores(entities)
                    // 保存该学期的最后刷新时间
                    localDataSource.saveScoreLastRefresh(t, System.currentTimeMillis())
                }
            }

            // 尝试获取并保存所有学期后，从数据库读取实际学期
            val dbTerms = scoreDao.getAllTerms().first()

            // 确定有效的当前学期：优先保留之前的UI选择（如果仍存在），否则优先使用解析出的当前学期，再否则使用数据库第一个学期
            val effectiveCurrent = listOfNotNull(
                // 如果调用者传递了学期，尝试保持它
                null
            ).firstOrNull()

            val selectedTerm = when {
                // 如果解析出的domainPage.currentTerm存在于数据库中，则保持它
                domainPage.currentTerm.isNotBlank() && dbTerms.contains(domainPage.currentTerm) -> domainPage.currentTerm
                // 否则从数据库中选取第一个可选学期
                dbTerms.isNotEmpty() -> dbTerms.first()
                else -> ""
            }

            val scores =
                if (selectedTerm.isBlank()) emptyList() else scoreDao.getScoresByTerm(selectedTerm)
                    .first().map { it.toDomain() }

            ScorePage(
                scores = scores,
                availableTerms = dbTerms,
                currentTerm = selectedTerm
            )
        } else {
            // 获取单个学期并保存
            params["term"] = term

            val htmlContent = jwxtDataSource.fetchHtml(SCORE_URL, params)
                .getOrElse { throw it }

            val parsedDto = htmlParser.parseScorePage(htmlContent)
                ?: throw Exception("无法解析成绩页面HTML")

            parsedDto.error?.let { throw Exception(it) }

            val domainPage = parsedDto.toDomain()

            val scores = domainPage.scores

            if (scores.isEmpty()) {
                scoreDao.deleteScoresByTerm(term)
                localDataSource.removeScoreLastRefresh(term)
            } else {
                scoreDao.deleteScoresByTerm(term)
                val entities = scores.map { s -> s.toEntity(term) }
                scoreDao.insertScores(entities)
                localDataSource.saveScoreLastRefresh(term, System.currentTimeMillis())
            }

            // 更新该学期的数据库后，基于数据库状态返回页面以保持UI一致性
            val dbTermsSingle = scoreDao.getAllTerms().first()
            val current =
                if (dbTermsSingle.contains(term)) term else dbTermsSingle.firstOrNull()
                    .orEmpty()
            val scoresFromDb = if (current.isNotBlank()) scoreDao.getScoresByTerm(current).first()
                .map { it.toDomain() } else emptyList()

            ScorePage(
                scores = scoresFromDb,
                availableTerms = dbTermsSingle,
                currentTerm = current
            )
        }
    }

    override suspend fun getScoresFromLocal(term: String?): Result<ScorePage> = safeApiCall {
        // 从数据库加载可选学期
        val terms = scoreDao.getAllTerms().first()

        val currentTerm = term?.takeIf { it.isNotBlank() } ?: terms.firstOrNull().orEmpty()

        val scores = if (currentTerm.isBlank()) {
            emptyList()
        } else {
            scoreDao.getScoresByTerm(currentTerm).first().map { it.toDomain() }
        }

        ScorePage(
            scores = scores,
            availableTerms = terms,
            currentTerm = currentTerm
        )
    }
}