package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.utils.Event
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    private val updateManager = UpdateManager()

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
        newVersionInfo?.let {
            updateManager.downloadUpdate(getApplication(), it)
        }
    }
}