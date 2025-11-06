package com.lonx.ecjtu.calendar.ui.screen.setting

// 定义 UI 状态
data class ParseResult(
    val originalInput: String,
    val parsedId: String,
    val isUrl: Boolean,
    val isParseSuccess: Boolean
)

data class SettingsUiState(
    val weiXinId: String = "",
    val noCourseText: String = "今天没有课啦~",
    val isLoading: Boolean = false,
    val isAutoUpdateCheckEnabled: Boolean = true,
    val parseResult: ParseResult? = null,
    val cacheSize: String = "计算中..."
//    val isCheckingForUpdate: Boolean = false,
//    val downloadState: DownloadState = DownloadState.Idle,
//    val availableUpdateInfo: UpdateInfo? = null
)

// 定义用户可以触发的事件
sealed interface SettingsEvent {
    data class OnIdChange(val id: String) : SettingsEvent // weixinid输入框内容改变
    data object OnSaveClick : SettingsEvent

    data class OnAutoUpdateCheckChanged(val isEnabled: Boolean) : SettingsEvent

    data object RequestPinAppWidgetClick : SettingsEvent
    data object OnCheckUpdateNowClick : SettingsEvent

    data object StartDownload : SettingsEvent
    data object CancelDownload : SettingsEvent

    data object InstallUpdate : SettingsEvent

    data object DismissUpdateDialog : SettingsEvent
    data object OnClearCacheClick : SettingsEvent
}

// 定义 UI 效果
sealed interface SettingsEffect {
    data class ShowToast(val message: String) : SettingsEffect
    data object RequestPinAppWidgetClick: SettingsEffect

}