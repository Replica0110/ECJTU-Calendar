package com.lonx.ecjtu.calendar.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveUserConfigUseCase
import com.lonx.ecjtu.calendar.domain.usecase.cache.CleanUpApksUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetColorModeUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetKeyColorIndexUseCase
import com.lonx.ecjtu.calendar.ui.screen.main.MainUiState
import com.lonx.ecjtu.calendar.util.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val app: Application,
    private val getUpdateSettingUseCase: GetUpdateSettingUseCase,
    private val saveUserConfigUseCase: SaveUserConfigUseCase,
    private val updateManager: UpdateManager,
    private val cleanUpApksUseCase: CleanUpApksUseCase,
    private val getColorModeUseCase: GetColorModeUseCase,
    private val getKeyColorIndexUseCase: GetKeyColorIndexUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    // 公开的、只读的状态流，供 UI 层订阅
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onStartup() {
        viewModelScope.launch {
            cleanUpApksUseCase(app)
        }
        viewModelScope.launch {
            getColorModeUseCase().distinctUntilChanged().collect { color ->
                _uiState.update { it.copy(colorMode = color) }
            }
        }
        viewModelScope.launch {
            getKeyColorIndexUseCase().distinctUntilChanged().collect { index ->
                _uiState.update { it.copy(keyColorIndex = index) }
            }
        }
        // 检查更新的逻辑保持不变
        viewModelScope.launch {
            if (getUpdateSettingUseCase().first()) {
                updateManager.checkForUpdate()
            }
        }
    }

    fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.getQueryParameter("weiXinID")?.let { id ->
                viewModelScope.launch {
                    saveUserConfigUseCase(id)
                    // TODO: 通过 effect 通知 MainActivity 显示 Toast
                }
            }
        }
    }
}