package com.lonx.ecjtu.calendar.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.cache.ClearCacheUseCase
import com.lonx.ecjtu.calendar.domain.usecase.cache.GetCacheSizeUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUserConfigUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveUserConfigUseCase
import com.lonx.ecjtu.calendar.ui.screen.settings.ParseResult
import com.lonx.ecjtu.calendar.ui.screen.settings.SettingsEffect
import com.lonx.ecjtu.calendar.ui.screen.settings.SettingsEvent
import com.lonx.ecjtu.calendar.ui.screen.settings.SettingsUiState
import com.lonx.ecjtu.calendar.util.UpdateEffect
import com.lonx.ecjtu.calendar.util.UpdateManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URL

class SettingsViewModel(
    private val app: Application,
    private val saveUserConfigUseCase: SaveUserConfigUseCase,
    private val getUserConfigUseCase: GetUserConfigUseCase,
    private val getUpdateSettingUseCase: GetUpdateSettingUseCase,
    private val saveUpdateSettingUseCase: SaveUpdateSettingUseCase,
    private val updateManager: UpdateManager,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val getCacheSizeUseCase: GetCacheSizeUseCase
) : ViewModel() {

    // 私有的可变状态流，仅在 ViewModel 内部修改
    private val _uiState = MutableStateFlow(SettingsUiState())
    // 公开的、只读的状态流，供 UI 层订阅
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        refreshCacheSize()
        // ViewModel 初始化时，立即开始监听已保存的 URL，并更新 UI
        viewModelScope.launch {
            getUserConfigUseCase().collect { savedUrl ->
                _uiState.update { it.copy(weiXinId = savedUrl) }
            }
        }
        viewModelScope.launch {
            getUpdateSettingUseCase().collect { isEnabled ->
                _uiState.update { it.copy(isAutoUpdateCheckEnabled = isEnabled) }
            }
        }
//        viewModelScope.launch {
//            updateManager.state.collect { updateState ->
//                _uiState.update {
//                    it.copy(
//                        isCheckingForUpdate = updateState.isChecking,
//                        availableUpdateInfo = updateState.updateInfo,
//                        downloadState = updateState.downloadState
//                    )
//                }
//            }
//        }
        viewModelScope.launch {
            updateManager.effect.collect { managerEffect ->
                when (managerEffect) {
                    is UpdateEffect.ShowToast -> {
                        _effect.emit(SettingsEffect.ShowToast(managerEffect.message))
                    }
                }
            }
        }
    }
    private fun refreshCacheSize() {
        viewModelScope.launch {
            val sizeString = getCacheSizeUseCase(app)
            _uiState.update { it.copy(cacheSize = sizeString) }
        }
    }
    private fun parseWeiXinID(input: String): ParseResult {
        val trimmedInput = input.trim()
        val isUrl = trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")

        return if (isUrl) {
            try {
                val url = URL(trimmedInput)
                val queryParams = url.query?.split("&")?.associate {
                    val (key, value) = it.split("=")
                    key to value
                }
                val parsedId = queryParams?.get("weiXinID")
                if (parsedId != null) {
                    ParseResult(
                        originalInput = trimmedInput,
                        parsedId = parsedId,
                        isUrl = true,
                        isParseSuccess = true
                    )
                } else {
                    ParseResult(
                        originalInput = trimmedInput,
                        parsedId = trimmedInput,
                        isUrl = true,
                        isParseSuccess = false
                    )
                }
            } catch (e: Exception) {
                ParseResult(
                    originalInput = trimmedInput,
                    parsedId = trimmedInput,
                    isUrl = true,
                    isParseSuccess = false
                )
            }
        } else {
            ParseResult(
                originalInput = trimmedInput,
                parsedId = trimmedInput,
                isUrl = false,
                isParseSuccess = false
            )
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnIdChange -> {
                val result = parseWeiXinID(event.id)
                _uiState.update {
                    it.copy(
                        weiXinId = result.parsedId,
                        parseResult = result
                    )
                }
            }
            is SettingsEvent.OnAutoUpdateCheckChanged -> {
                viewModelScope.launch {
                    saveUpdateSettingUseCase(event.isEnabled)
                }
            }
            is SettingsEvent.OnSaveClick -> {
                saveUrl()
            }
            is SettingsEvent.OnCheckUpdateNowClick -> {
                updateManager.checkForUpdate()
                refreshCacheSize()
            }
            is SettingsEvent.StartDownload -> {
                updateManager.startDownload(app)
                refreshCacheSize()
            }
            is SettingsEvent.CancelDownload -> {
                updateManager.cancelDownload()
                refreshCacheSize()
            }
            is SettingsEvent.InstallUpdate -> {
                updateManager.installUpdate(app)
                refreshCacheSize()
            }
            is SettingsEvent.DismissUpdateDialog -> {
                updateManager.dismissUpdateDialog()
                refreshCacheSize()
            }
            is SettingsEvent.OnClearCacheClick -> {
                clearCache()
            }
            is SettingsEvent.RequestPinAppWidgetClick -> {
                viewModelScope.launch{
                    _effect.emit(SettingsEffect.RequestPinAppWidgetClick)
                }
            }
        }
    }
    @SuppressLint("DefaultLocale")
    private fun clearCache() {
        viewModelScope.launch {
            val result = clearCacheUseCase(app)

            updateManager.resetUpdateState()

            result.onSuccess { bytesFreed ->
                val sizeInMB = String.format("%.2f", bytesFreed / (1024.0 * 1024.0))
                _effect.emit(SettingsEffect.ShowToast("缓存已清理，释放了 $sizeInMB MB"))

                refreshCacheSize()
            }.onFailure {
                _effect.emit(SettingsEffect.ShowToast("清理缓存失败: ${it.message}"))
            }
        }
    }
    private fun saveUrl() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // 开始加载
            saveUserConfigUseCase(uiState.value.weiXinId)
            _uiState.update { it.copy(isLoading = false) } // 结束加载

            // 发送保存成功的通知，包含解析信息
            val message = uiState.value.parseResult?.let { result ->
                if (result.isUrl) {
                    if (result.isParseSuccess) {
                        "已保存从URL解析的weiXinID"
                    } else {
                        "URL解析失败"
                    }
                } else {
                    "保存成功"
                }
            } ?: "保存成功"

            _effect.emit(SettingsEffect.ShowToast(message))
        }
    }
}