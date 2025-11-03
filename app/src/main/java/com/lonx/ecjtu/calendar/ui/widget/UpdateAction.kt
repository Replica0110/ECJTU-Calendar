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
import kotlinx.coroutines.launch
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

        // 在 IO 线程上执行网络请求
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            // 并发获取今日和明日的课程
            val todayDeferred = async { getCoursesUseCase(today) }
            val tomorrowDeferred = async { getCoursesUseCase(tomorrow) }

            val todayResult = todayDeferred.await()
            val tomorrowResult = tomorrowDeferred.await()

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