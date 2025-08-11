package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.lonx.ecjtu.hjcalendar.BuildConfig
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.appWidget.CourseWidgetProvider
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.utils.Event
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    private val updateManager = UpdateManager()


    // 更新检查结果
    private val _updateResult = MutableLiveData<UpdateCheckResult>()
    val updateResult: LiveData<UpdateCheckResult> = _updateResult

    private val _pinWidgetResult = MutableLiveData<Event<String>>()
    val pinWidgetResult: LiveData<Event<String>> = _pinWidgetResult

    var newVersionInfo: UpdateManager.UpdateInfo? = null

    fun getWeiXinId(): String {
        return DataStoreManager.getWeiXinId()
    }

    fun isUpdateCheckEnabled(): Boolean {
        return DataStoreManager.isUpdateCheckOnStartEnabled()
    }

    fun setUpdateCheck(enabled: Boolean) {
        DataStoreManager.setUpdateCheckOnStart(enabled)
    }

    /** 获取格式化的版本信息 */
    fun getVersionSummary(): String {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val buildTime = BuildConfig.BUILD_TIME
        return "版本: $versionName ($versionCode)\n最后编译时间: $buildTime"
    }

    /** 检查更新 */
    fun checkForUpdate() {
        viewModelScope.launch {
            _updateResult.postValue(updateManager.checkForUpdate())
        }
    }

    /** 下载更新 */
    fun downloadUpdate() {
        newVersionInfo?.let {
            updateManager.downloadUpdate(getApplication(), it)
        }
    }
    /** 保存输入框中的字符串设置 */
    fun getNoCourseText(): String {
        val defaultText = getApplication<Application>().getString(R.string.empty_course)
        return DataStoreManager.getNoCourseText(defaultText)
    }

    /** 将课程小组件固定到桌面 */
    fun pinWidget() {
        val context = getApplication<Application>()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, CourseWidgetProvider::class.java)

        val message = try {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
                "已发送添加小组件到桌面的请求"
            } else {
                "您的设备不支持固定桌面小组件"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "添加失败了~"
        }
        _pinWidgetResult.value = Event(message)
    }

    /** 保存输入框中的字符串设置 */
    fun saveStringPreference(key: String, value: String) {
        val app = getApplication<Application>()
        var processedValue = value
        val message: String

        when (key) {
            app.getString(R.string.weixin_id_key) -> {
                if (value.startsWith("https://jwxt.ecjtu.edu.cn/weixin")) {
                    processedValue = value.substringAfter("=", "")
                }
                DataStoreManager.saveWeiXinId(processedValue)
                message = "weiXinID已保存，在日历页面下拉刷新课表"
            }
            app.getString(R.string.no_course_key) -> {
                DataStoreManager.saveNoCourseText(processedValue)
                message = "自定义文本已保存"
            }
            else -> {
                return
            }
        }
        _pinWidgetResult.value = Event(message)
    }
}