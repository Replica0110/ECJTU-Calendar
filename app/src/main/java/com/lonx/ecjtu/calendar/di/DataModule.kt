package com.lonx.ecjtu.calendar.di

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSourceImpl
import com.lonx.ecjtu.calendar.data.datasource.remote.CourseDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.CourseDataSourceImpl
import com.lonx.ecjtu.calendar.data.datasource.remote.UpdateDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.UpdateDataSourceImpl
import com.lonx.ecjtu.calendar.data.network.HtmlParser
import com.lonx.ecjtu.calendar.data.repository.CalendarRepositoryImpl
import com.lonx.ecjtu.calendar.data.repository.UpdateRepositoryImpl
import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import com.lonx.ecjtu.calendar.domain.repository.UpdateRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    // 提供 CourseDataSource 的单例
    single<CourseDataSource> {
        CourseDataSourceImpl()
    }

    // 提供 LocalDataSource 的单例，它需要 Android Context
    single<LocalDataSource> {
        LocalDataSourceImpl(androidContext())
    }

    // 提供 HtmlParser 的单例
    single {
        HtmlParser()
    }

    // 将 CalendarRepository 接口与其实现 CalendarRepositoryImpl 绑定为单例。
    // Koin 的 get() 函数会自动查找并注入上面定义的依赖。
    single<CalendarRepository> {
        CalendarRepositoryImpl(
            courseDataSource = get(),
            localDataSource = get(),
            htmlParser = get()
        )
    }
    
    // 提供 UpdateDataSource 的单例
    single<UpdateDataSource> {
        UpdateDataSourceImpl()
    }
    
    // 将 UpdateRepository 接口与其实现 UpdateRepositoryImpl 绑定为单例
    single<UpdateRepository> {
        UpdateRepositoryImpl(
            updateDataSource = get()
        )
    }

}