package com.lonx.ecjtu.calendar.di

import com.lonx.ecjtu.calendar.domain.usecase.cache.CleanUpApksUseCase
import com.lonx.ecjtu.calendar.domain.usecase.cache.ClearCacheUseCase
import com.lonx.ecjtu.calendar.domain.usecase.cache.GetCacheSizeUseCase
import com.lonx.ecjtu.calendar.domain.usecase.calendar.GetAcademicCalendarUseCase
import com.lonx.ecjtu.calendar.domain.usecase.course.GetCoursesUseCase
import com.lonx.ecjtu.calendar.domain.usecase.course.GetSelectedCoursesUseCase
import com.lonx.ecjtu.calendar.domain.usecase.score.GetScoreUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetColorModeUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetKeyColorIndexUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.GetUserConfigUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveColorModeUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveKeyColorIndexUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveUpdateSettingUseCase
import com.lonx.ecjtu.calendar.domain.usecase.settings.SaveUserConfigUseCase
import com.lonx.ecjtu.calendar.domain.usecase.update.ApkInstallUseCase
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
    factory { CleanUpApksUseCase() }

    factory { GetColorModeUseCase(repository = get()) }
    factory { SaveColorModeUseCase(repository = get()) }

    factory { GetKeyColorIndexUseCase(repository = get()) }
    factory { SaveKeyColorIndexUseCase(repository = get()) }

    // 提供 GetAcademicCalendarUseCase 的实例工厂
    factory {
        GetAcademicCalendarUseCase(repository = get(), parser = get())
    }
    factory {
        GetScoreUseCase(repository = get())
    }
    factory {
        GetSelectedCoursesUseCase(repository = get())
    }
}