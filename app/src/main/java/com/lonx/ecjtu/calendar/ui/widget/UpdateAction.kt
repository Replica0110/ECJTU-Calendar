package com.lonx.ecjtu.calendar.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.lonx.ecjtu.calendar.domain.usecase.course.GetCoursesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class UpdateAction : ActionCallback, KoinComponent {
    private val getCoursesUseCase: GetCoursesUseCase by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 立即将状态更新为“加载中”
        updateAppWidgetState(context, CourseWidgetStateDefinition, glanceId) { currentState ->
            currentState.copy(
                today = CourseUiState.Loading,
                tomorrow = CourseUiState.Loading
            )
        }
        CourseGlanceWidget().update(context, glanceId)

        // 在 onAction 的协程上下文中执行网络请求（可被挂起/取消），并对 DNS 解析失败做重试与回退
        withContext(Dispatchers.IO) {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            suspend fun fetchWithRetry(date: LocalDate, maxAttempts: Int = 3): Result<com.lonx.ecjtu.calendar.domain.model.SchedulePage> {
                var attempt = 0
                var lastFailure: Result<com.lonx.ecjtu.calendar.domain.model.SchedulePage>? = null
                while (attempt < maxAttempts) {
                    attempt++
                    val res = getCoursesUseCase(date)
                    if (res.isSuccess) return res
                    lastFailure = res
                    // 如果是网络解析错误，则稍后重试；否则直接返回
                    val ex = res.exceptionOrNull()
                    if (ex is java.net.UnknownHostException) {
                        // 指数退避
                        delay(1000L * attempt)
                        continue
                    } else {
                        return res
                    }
                }
                return lastFailure ?: Result.failure(Exception("Unknown network error"))
            }

            // 并发获取今日和明日的课程（每个带重试）
            val todayDeferred = async { fetchWithRetry(today) }
            val tomorrowDeferred = async { fetchWithRetry(tomorrow) }

            val todayResult = todayDeferred.await()
            val tomorrowResult = tomorrowDeferred.await()

            // 如果两个请求都因为网络（DNS）失败，则回退到本地缓存，以避免空白/错误信息
            if (todayResult.isFailure && tomorrowResult.isFailure) {
                val cached = CourseWidgetStateDefinition.readState(context, glanceId.toString())
                updateAppWidgetState(context, CourseWidgetStateDefinition, glanceId) { _ ->
                    cached
                }
                CourseGlanceWidget().update(context, glanceId)
                return@withContext
            }

            // 将 Result 转换为 UI State
            val todayState = todayResult.fold(
                onSuccess = { CourseUiState.Success(it) },
                onFailure = { CourseUiState.Error(it.message ?: "未知错误") }
            )
            val tomorrowState = tomorrowResult.fold(
                onSuccess = { CourseUiState.Success(it) },
                onFailure = { CourseUiState.Error(it.message ?: "未知错误") }
            )

            // 更新最终状态
            updateAppWidgetState(context, CourseWidgetStateDefinition, glanceId) { currentState ->
                currentState.copy(
                    today = todayState,
                    tomorrow = tomorrowState
                )
            }
            CourseGlanceWidget().update(context, glanceId)
        }
    }
}