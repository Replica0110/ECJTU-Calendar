package com.lonx.ecjtu.calendar.di

import MainViewModel
import com.lonx.ecjtu.calendar.ui.viewmodels.CalendarViewModel
import com.lonx.ecjtu.calendar.ui.viewmodels.SettingsViewModel
import com.lonx.ecjtu.calendar.ui.util.AppUpdateInstaller
import com.lonx.ecjtu.calendar.ui.util.AppUpdateInstallerImpl
import com.lonx.ecjtu.calendar.util.UpdateManager
import com.lonx.ecjtu.calendar.util.UpdateManagerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel {
        SettingsViewModel(
            app = androidApplication(),
            saveUserConfigUseCase = get(),
            getUserConfigUseCase = get(),
            getUpdateSettingUseCase = get(),
            saveUpdateSettingUseCase = get(),
            updateManager = get(),
            clearCacheUseCase = get(),
            getCacheSizeUseCase = get()
        )
    }
    viewModel {
        MainViewModel(
            app = androidApplication(),
            getUpdateSettingUseCase = get(),
            saveUserConfigUseCase = get(),
            updateManager = get(),
            cleanUpApksUseCase = get()
        )
    }
    // 提供 CalendarViewModel
    viewModel {
        CalendarViewModel(
            getCoursesUseCase = get()
        )
    }
    single<AppUpdateInstaller> {
        AppUpdateInstallerImpl()
    }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UpdateManager> {
        UpdateManagerImpl(
            appScope = get(),
            updateRepository = get(),
            apkInstallUseCase = get()
        )
    }
}