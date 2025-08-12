package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager
import com.lonx.ecjtu.hjcalendar.logic.DownloadState
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.utils.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    val updateManager = UpdateManager()
    private val _downloadState = MutableLiveData<DownloadState>(DownloadState.Idle)
    val downloadState: LiveData<DownloadState> = _downloadState

    // 新增：用于持有下载任务的 Job
    private var downloadJob: Job? = null
    private val _updateResult = MutableLiveData<UpdateCheckResult>()
    val updateResult: LiveData<UpdateCheckResult> = _updateResult

    // 用于显示一次性消息，如 Toast
    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    var newVersionInfo: UpdateManager.UpdateInfo? = null


    fun runStartupChecks() {
        if (DataStoreManager.isUpdateCheckOnStartEnabled()) {
            viewModelScope.launch {
                _updateResult.postValue(updateManager.checkForUpdate())
            }
        }
    }

    fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                uri.getQueryParameter("weiXinID")?.let { id ->
                    DataStoreManager.saveWeiXinId(id)
                    _toastMessage.value = Event("weiXinID 已获取，请下拉刷新日历")
                }
            }
        }
    }

    /**
     * 下载更新
     */
    fun downloadUpdate() {
        if (downloadJob?.isActive == true) return

        newVersionInfo?.let { info ->
            downloadJob = viewModelScope.launch {
                try {
                    updateManager.downloadUpdate(getApplication(), info)
                        .collect { state ->
                            _downloadState.postValue(state)
                        }
                } catch (e: Exception) {
                    if (e is java.util.concurrent.CancellationException) {
                        Log.i("ViewModel", "Download was cancelled by the user. This is normal.")
                    } else {
                        Log.e("ViewModel", "An unexpected error occurred during download.", e)
                        _downloadState.postValue(DownloadState.Error(e))
                    }
                }
            }
        }
    }
    /**
     * 取消下载。
     */
    fun cancelDownload() {
        downloadJob?.cancel()
        _downloadState.postValue(DownloadState.Idle) // 重置状态
    }

    /**
     * 在对话框关闭或下载完成后重置状态。
     */
    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }
}