package com.lonx.ecjtu.calendar.data.repository

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.local.SelectedCourseDao
import com.lonx.ecjtu.calendar.data.datasource.local.toDomain
import com.lonx.ecjtu.calendar.data.datasource.local.toEntity
import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.mapper.toDomain
import com.lonx.ecjtu.calendar.data.network.Constants.SELECTED_COURSE_URL
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.data.repository.utils.requireWeiXinId
import com.lonx.ecjtu.calendar.data.repository.utils.safeApiCall
import com.lonx.ecjtu.calendar.domain.model.SelectedCoursePage
import com.lonx.ecjtu.calendar.domain.repository.SelectedCourseRepository
import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class SelectedCourseRepositoryImpl(
    private val jwxtDataSource: JwxtDataSource,
    private val localDataSource: LocalDataSource,
    private val htmlParser: HtmlParser,
    private val selectedCourseDao: SelectedCourseDao
) : SelectedCourseRepository {

    override suspend fun getSelectedCourses(term: String?): Result<SelectedCoursePage> =
        safeApiCall {

            val weiXinId = localDataSource.requireWeiXinId().getOrThrow()
            Logger.d(Tags.SELECTED_COURSE, "获取已选课程: term=${term ?: "null"} (完整刷新)")

            val params = mutableMapOf("weiXinID" to weiXinId)
            // 如果学期参数为空，执行完整刷新：首先不带学期参数获取页面以获得所有可选学期，
            // 然后遍历这些学期分别获取成绩并保存到数据库。
            if (term.isNullOrBlank()) {
                // 获取所有可选学期
                val htmlContent = jwxtDataSource.fetchHtml(SELECTED_COURSE_URL, params)
                    .getOrElse { throw it }
                val parsedDto = htmlParser.parseSelectedCoursePage(htmlContent)
                    ?: throw Exception("无法解析选课页面HTML")
                parsedDto.error?.let { throw Exception(it) }
                val domainPage = parsedDto.toDomain()

                coroutineScope {
                    val termsCount = domainPage.availableTerms.size
                    Logger.d(Tags.SELECTED_COURSE, "并行获取 $termsCount 个学期数据")

                    domainPage.availableTerms.mapIndexed { index, t ->
                        async {
                            Logger.d(Tags.SELECTED_COURSE, "获取学期 [$${index + 1}/$termsCount]: $t")
                            val p = params.toMutableMap()
                            p["term"] = t
                            val htmlForTerm =
                                jwxtDataSource.fetchHtml(SELECTED_COURSE_URL, p).getOrElse { null }
                            if (htmlForTerm == null) {
                                Logger.w(Tags.SELECTED_COURSE, "获取学期 $t 时网络错误，跳过")
                                return@async
                            }
                            val parsedDtoForTerm = htmlParser.parseSelectedCoursePage(htmlForTerm)
                            if (parsedDtoForTerm == null || parsedDtoForTerm.error != null) {
                                Logger.w(Tags.SELECTED_COURSE, "学期 $t 解析失败，删除数据")
                                selectedCourseDao.deleteSelectedCoursesByTerm(t)
                                return@async
                            }
                            val selectedCourses = parsedDtoForTerm.toDomain().courses
                            Logger.d(Tags.SELECTED_COURSE, "学期 $t 获取到 ${selectedCourses.size} 门课程")
                            if (selectedCourses.isEmpty()) {
                                selectedCourseDao.deleteSelectedCoursesByTerm(t)
                                localDataSource.removeSelectedCourseLastRefresh(t)
                            } else {
                                selectedCourseDao.deleteSelectedCoursesByTerm(t)
                                val entities = selectedCourses.map { it.toEntity(t) }
                                selectedCourseDao.insertSelectedCourses(entities)
                                localDataSource.saveSelectedCourseLastRefresh(t, System.currentTimeMillis())
                            }
                        }
                    }.awaitAll()
                }

                // 尝试获取并保存所有学期后，从数据库读取实际学期
                val dbTerms = selectedCourseDao.getAllTerms().first()

                val selectedTerm = when {
                    domainPage.currentTerm.isNotBlank() && dbTerms.contains(domainPage.currentTerm) -> domainPage.currentTerm
                    dbTerms.isNotEmpty() -> dbTerms.first()
                    else -> ""
                }

                Logger.d(Tags.SELECTED_COURSE, "完整刷新完成，选择学期: $selectedTerm，可用学期: ${dbTerms.size} 个")

                val selectedCourses =
                    if (selectedTerm.isBlank()) emptyList() else selectedCourseDao.getSelectedCoursesByTerm(
                        selectedTerm
                    ).first().map { it.toDomain() }

                SelectedCoursePage(
                    courses = selectedCourses,
                    availableTerms = dbTerms,
                    currentTerm = selectedTerm
                )

            } else {
                // 获取单个学期并保存
                Logger.d(Tags.SELECTED_COURSE, "获取单个学期: $term")
                params["term"] = term

                val htmlContent = jwxtDataSource.fetchHtml(SELECTED_COURSE_URL, params)
                    .getOrElse { throw it }

                val parsedDto = htmlParser.parseSelectedCoursePage(htmlContent)
                    ?: throw Exception("无法解析选课页面HTML")

                parsedDto.error?.let { throw Exception(it) }

                val domainPage = parsedDto.toDomain()

                val courses = domainPage.courses

                if (courses.isEmpty()) {
                    Logger.d(Tags.SELECTED_COURSE, "学期 $term 无已选课程，删除数据")
                    selectedCourseDao.deleteSelectedCoursesByTerm(term)
                    localDataSource.removeSelectedCourseLastRefresh(term)
                } else {
                    Logger.logDbOperation(Tags.SELECTED_COURSE, "保存", courses.size)
                    selectedCourseDao.deleteSelectedCoursesByTerm(term)
                    val entities = courses.map { it.toEntity(term) }
                    selectedCourseDao.insertSelectedCourses(entities)
                    localDataSource.saveSelectedCourseLastRefresh(term, System.currentTimeMillis())
                }

                // 更新该学期的数据库后，基于数据库状态返回页面以保持UI一致性
                val dbTermsSingle = selectedCourseDao.getAllTerms().first()
                val current =
                    if (term.isNotBlank() && dbTermsSingle.contains(term)) term else dbTermsSingle.firstOrNull()
                        .orEmpty()
                val coursesFromDb =
                    if (current.isNotBlank()) selectedCourseDao.getSelectedCoursesByTerm(current)
                        .first()
                        .map { it.toDomain() } else emptyList()

                Logger.d(Tags.SELECTED_COURSE, "学期 $term 获取完成")

                SelectedCoursePage(
                    courses = coursesFromDb,
                    availableTerms = dbTermsSingle,
                    currentTerm = current
                )
            }
        }


    override suspend fun getSelectedCoursesFromLocal(term: String?): Result<SelectedCoursePage> =
        safeApiCall {
            Logger.d(Tags.SELECTED_COURSE, "从本地加载已选课程: term=${term ?: "null"}")

            val terms = selectedCourseDao.getAllTerms().first()

            val currentTerm = term?.takeIf { it.isNotBlank() } ?: terms.firstOrNull().orEmpty()

            val courses = if (currentTerm.isBlank()) {
                emptyList()
            } else {
                selectedCourseDao.getSelectedCoursesByTerm(currentTerm).first()
                    .map { it.toDomain() }
            }

            Logger.d(Tags.SELECTED_COURSE, "本地加载完成: ${courses.size} 门课程")

            SelectedCoursePage(
                courses = courses,
                availableTerms = terms,
                currentTerm = currentTerm
            )
        }
}