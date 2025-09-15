package com.lonx.ecjtu.calendar.di

import com.lonx.ecjtu.calendar.domain.usecase.ApkInstallUseCase
import com.lonx.ecjtu.calendar.domain.usecase.ClearCacheUseCase
import com.lonx.ecjtu.calendar.domain.usecase.GetCacheSizeUseCase
import com.lonx.ecjtu.calendar.domain.usecase.GetCoursesUseCase
import com.lonx.ecjtu.calendar.domain.usecase.GetUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.GetUserConfigUseCase
import com.lonx.ecjtu.calendar.domain.usecase.SaveUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.SaveUserConfigUseCase
import org.koin.dsl.module

val domainModule = module {

    // 提供 GetCoursesUseCase 的实例工厂
    factory {
        GetCoursesUseCase(repository = get())
    }

    // 提供 GetUserConfigUseCase 的实例工厂
    factory {
        GetUserConfigUseCase(repository = get())
    }

    // 提供 SaveUserConfigUseCase 的实例工厂
    factory {
        SaveUserConfigUseCase(repository = get())
    }

    factory { GetUpdateSettingUseCase(repository = get()) }

    factory { SaveUpdateSettingUseCase(repository = get()) }
    factory { ClearCacheUseCase() }
    factory { GetCacheSizeUseCase() }

    factory { ApkInstallUseCase(appUpdateInstaller = get()) }
}