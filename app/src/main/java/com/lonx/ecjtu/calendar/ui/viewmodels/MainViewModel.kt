import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveUserConfigUseCase
import com.lonx.ecjtu.calendar.domain.usecase.cache.CleanUpApksUseCase
import com.lonx.ecjtu.calendar.util.UpdateManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val app: Application,
    private val getUpdateSettingUseCase: GetUpdateSettingUseCase,
    private val saveUserConfigUseCase: SaveUserConfigUseCase,
    private val updateManager: UpdateManager,
    private val cleanUpApksUseCase: CleanUpApksUseCase
) : ViewModel() {


    fun onStartup() {
        viewModelScope.launch {
            cleanUpApksUseCase(app)
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