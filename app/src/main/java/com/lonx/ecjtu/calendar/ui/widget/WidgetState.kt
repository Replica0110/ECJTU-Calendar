package com.lonx.ecjtu.calendar.ui.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString

// 用于表示单个列表（今日/明日）的 UI 状态
@Serializable
sealed interface CourseUiState {
    @Serializable
    object Loading : CourseUiState
    @Serializable
    data class Success(val page: SchedulePage) : CourseUiState
    @Serializable
    data class Error(val message: String) : CourseUiState
}

// 包含今日和明日课程的完整小组件状态
@Serializable
data class CourseWidgetState(
    val today: CourseUiState = CourseUiState.Loading,
    val tomorrow: CourseUiState = CourseUiState.Loading
)

// 定义如何存储和检索小组件的状态
object CourseWidgetStateDefinition : GlanceStateDefinition<CourseWidgetState> {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(CourseUiState::class) {
                subclass(CourseUiState.Loading::class)
                subclass(CourseUiState.Success::class)
                subclass(CourseUiState.Error::class)
            }
        }
    }

    private fun getFile(context: Context, glanceId: String) = File(context.filesDir, "glance-$glanceId.json")

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<CourseWidgetState> {
        return object : DataStore<CourseWidgetState> {
            override val data: Flow<CourseWidgetState>
                get() = flow {
                    emit(readState(context, fileKey))
                }

            override suspend fun updateData(transform: suspend (t: CourseWidgetState) -> CourseWidgetState): CourseWidgetState {
                val currentState = readState(context, fileKey)
                val newState = transform(currentState)
                writeState(context, fileKey, newState)
                return newState
            }
        }
    }

    override fun getLocation(
        context: Context,
        fileKey: String
    ): File {
        return getFile(context, fileKey)
    }

    fun readState(context: Context, fileKey: String): CourseWidgetState {
        return try {
            val json = getFile(context, fileKey).readText()
            this.json.decodeFromString<CourseWidgetState>(json)
        } catch (e: Exception) {
            // 如果文件不存在或解析失败，返回默认的加载状态
            CourseWidgetState()
        }
    }

    fun writeState(context: Context, fileKey: String, state: CourseWidgetState) {
        val json = this.json.encodeToString(state)
        getFile(context, fileKey).writeText(json)
    }
}