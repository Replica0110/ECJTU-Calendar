package com.lonx.ecjtu.calendar.util

import android.content.Context
import com.lonx.ecjtu.calendar.data.dto.UpdateDTO
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.data.model.UpdateCheckResult
import com.lonx.ecjtu.calendar.domain.repository.UpdateRepository
import com.lonx.ecjtu.calendar.domain.usecase.update.ApkInstallUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateState(
    val isChecking: Boolean = false,
    val updateDTO: UpdateDTO? = null,
    val downloadState: DownloadState = DownloadState.Idle,
    val info: String? = null,
)
sealed interface UpdateEffect {
    data class ShowToast(val message: String) : UpdateEffect
}
// 2. 定义接口
interface UpdateManager {
    val state: StateFlow<UpdateState>
    val effect: Flow<UpdateEffect>
    fun checkForUpdate()

    fun startDownload(context: Context)
    fun cancelDownload()
    fun installUpdate(context: Context)
    fun dismissUpdateDialog()
    fun resetUpdateState()
}


class UpdateManagerImpl(
    private val appScope: CoroutineScope, // 使用应用级别的协程作用域
    private val updateRepository: UpdateRepository,
    private val apkInstallUseCase: ApkInstallUseCase
): UpdateManager {
    private val _state = MutableStateFlow(UpdateState())
    override val state: StateFlow<UpdateState> = _state.asStateFlow()
    private val _effect = MutableSharedFlow<UpdateEffect>()
    override val effect: Flow<UpdateEffect> = _effect.asSharedFlow()
    private var lastValidUpdateResult: UpdateCheckResult.NewVersion? = null
    private var downloadJob: Job? = null

    override fun checkForUpdate() {

        if (_state.value.isChecking) return

        appScope.launch {
            _state.update { it.copy(isChecking = true) }
            when (val result = updateRepository.checkForUpdate()) {
                is UpdateCheckResult.NewVersion -> {
                    lastValidUpdateResult = result
                    _state.update { it.copy(updateDTO = result.info) }
                }
                is UpdateCheckResult.NoUpdateAvailable -> {
                    lastValidUpdateResult = null
                    _state.update { it.copy(info = "已经是最新版本") }
                    _effect.emit(UpdateEffect.ShowToast("已经是最新版本"))
                }
                is UpdateCheckResult.ApiError -> {
                    lastValidUpdateResult = null
                    _state.update { it.copy(info = "API错误: ${result.code}") }
                    _effect.emit(UpdateEffect.ShowToast("API错误: ${result.code}"))
                }
                is UpdateCheckResult.NetworkError -> {
                    lastValidUpdateResult = null
                    _state.update { it.copy(info = "网络错误，请检查连接") }
                    _effect.emit(UpdateEffect.ShowToast("网络错误，请检查连接"))
                }
                UpdateCheckResult.ParsingError -> {
                    lastValidUpdateResult = null
                    _state.update { it.copy(info = "解析更新信息失败") }
                    _effect.emit(UpdateEffect.ShowToast("解析更新信息失败"))
                }
                UpdateCheckResult.TimeoutError -> {
                    lastValidUpdateResult = null
                    _state.update { it.copy(info = "检查更新超时") }
                    _effect.emit(UpdateEffect.ShowToast("检查更新超时"))
                }
            }
            _state.update { it.copy(isChecking = false) }
        }
    }

    override fun startDownload(context: Context) {
        if (downloadJob?.isActive == true || state.value.updateDTO == null) return

        downloadJob = appScope.launch {
            updateRepository.downloadUpdate(context, state.value.updateDTO!!)
                .onStart { _state.update { it.copy(downloadState = DownloadState.InProgress(0)) } }
                .catch { e -> _state.update { it.copy(downloadState = DownloadState.Error(e)) } }
                .collect { downloadState ->
                    _state.update { it.copy(downloadState = downloadState, info = "下载中...") }
                }
        }
    }

    override fun cancelDownload() {
        downloadJob?.cancel()
        _state.update { it.copy(downloadState = DownloadState.Idle) }
    }

    override fun installUpdate(context: Context) {
        val currentState = _state.value
        if (currentState.downloadState is DownloadState.Success) {
            apkInstallUseCase(context, currentState.downloadState.file)
        }
    }

    override fun dismissUpdateDialog() {
        _state.update { it.copy(
            updateDTO = null,
//            downloadState = DownloadState.Idle
        ) }
//        cancelDownload()
    }
    override fun resetUpdateState() {
        cancelDownload() // 如果正在下载，先取消
        lastValidUpdateResult = null
        _state.update {
            it.copy(
                isChecking = false,
                updateDTO = null,
                downloadState = DownloadState.Idle
            )
        }
    }

}