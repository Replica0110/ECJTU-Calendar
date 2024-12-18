package com.lonx.ecjtu.hjcalendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val updateManager = UpdateManager()

    // LiveData 现在持有 UpdateCheckResult
    private val _updateResult = MutableLiveData<UpdateCheckResult>()
    val updateResult: LiveData<UpdateCheckResult> = _updateResult

    // 用于在对话框中临时存储信息
    var newVersionInfo: UpdateManager.UpdateInfo? = null

    fun checkForUpdate() {
        viewModelScope.launch {
            val result = updateManager.checkForUpdate()
            _updateResult.postValue(result)
        }
    }

    fun downloadUpdate() {
        newVersionInfo?.let {
            updateManager.downloadUpdate(getApplication(), it)
        }
    }
}