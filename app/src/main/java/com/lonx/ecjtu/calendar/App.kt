package com.lonx.ecjtu.calendar

import android.app.Application
import com.lonx.ecjtu.calendar.di.appModule
import com.lonx.ecjtu.calendar.di.dataModule
import com.lonx.ecjtu.calendar.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // 使用 Android Logger，级别为 ERROR，避免在 Logcat 中产生过多噪音
            androidLogger(Level.ERROR)
            // 注入 Android Context
            androidContext(this@App)
            // 加载我们定义的所有模块
            modules(dataModule, domainModule, appModule)
        }
    }
}