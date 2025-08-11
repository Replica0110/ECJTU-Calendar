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


    /**
     * 执行应用启动时的检查。
     */
    fun runStartupChecks() {
        val checkUpdateOnStart = prefs.getBoolean(getApplication<Application>().getString(R.string.check_update_key), true)
        if (checkUpdateOnStart) {
            viewModelScope.launch {
                _updateResult.postValue(updateManager.checkForUpdate())
            }
        }
    }

    /**
     * 处理传入的 Intent。
     */
    fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                uri.getQueryParameter("weiXinID")?.let { id ->
                    prefs.edit { putString(getApplication<Application>().getString(R.string.weixin_id_key), id) }
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