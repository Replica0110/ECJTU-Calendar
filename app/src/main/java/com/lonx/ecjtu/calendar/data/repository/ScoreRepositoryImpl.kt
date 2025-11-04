package com.lonx.ecjtu.calendar.data.repository

import android.util.Log
import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.mapper.toDomain
import com.lonx.ecjtu.calendar.data.network.Constants.SCORE_URL
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.domain.model.ScorePage
import com.lonx.ecjtu.calendar.domain.repository.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScoreRepositoryImpl(
    private val jwxtDataSource: JwxtDataSource,
    private val parser: HtmlParser
) : ScoreRepository {
    /**
     * 获取指定学期的成绩列表和所有可用的学期。
     * @param weiXinID 必需的微信ID参数。
     * @param term 可选的学期字符串，例如 "2023.1"。如果为 null，则获取默认学期的成绩。
     * @return 返回一个 Result 对象，成功时包含 ScorePage 对象，失败时包含异常。
     */
    override suspend fun getScores(weiXinID: String, term: String?): Result<ScorePage> {
        return withContext(Dispatchers.IO) {
            try {
                val params = mutableMapOf<String, Any>("weiXinID" to weiXinID)
                term?.takeIf { it.isNotBlank() }?.let {
                    params["term"] = it
                }

                val htmlResult = jwxtDataSource.fetchHtml(SCORE_URL, params)

                if (htmlResult.isSuccess) {
                    val htmlContent = htmlResult.getOrThrow()
                    val parsedData = parser.parseScorePage(htmlContent)

                    if (parsedData != null) {
                        if (parsedData.error!= null){
                            return@withContext Result.failure(Exception(parsedData.error))
                        }
                        // 将 DTO 映射为领域模型并返回成功结果
                        Result.success(parsedData.toDomain())
                    } else {
                        Result.failure(Exception("无法解析成绩页面HTML"))
                    }
                } else {
                    // 数据源获取失败，直接返回失败结果
                    Result.failure(htmlResult.exceptionOrNull() ?: Exception("从数据源获取成绩HTML失败"))
                }
            } catch (e: Exception) {
                Log.e("ScoreRepositoryImpl", "获取成绩时发生未知错误", e)
                Result.failure(e)
            }
        }
    }

}