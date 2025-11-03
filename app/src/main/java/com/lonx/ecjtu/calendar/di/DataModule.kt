package com.lonx.ecjtu.calendar.di

import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSource
import com.lonx.ecjtu.calendar.data.datasource.local.LocalDataSourceImpl
import com.lonx.ecjtu.calendar.data.datasource.remote.AcademicCalendarDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.AcademicCalendarDataSourceImpl
import com.lonx.ecjtu.calendar.data.datasource.remote.CourseDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.CourseDataSourceImpl
import com.lonx.ecjtu.calendar.data.datasource.remote.UpdateDataSource
import com.lonx.ecjtu.calendar.data.datasource.remote.UpdateDataSourceImpl
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import com.lonx.ecjtu.calendar.data.repository.CalendarRepositoryImpl
import com.lonx.ecjtu.calendar.data.repository.UpdateRepositoryImpl
import com.lonx.ecjtu.calendar.domain.repository.AcademicCalendar
import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import com.lonx.ecjtu.calendar.domain.repository.UpdateRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    // 提供 CourseDataSource 的单例
    single<CourseDataSource> {
        CourseDataSourceImpl()
    }

    // 提供 AcademicCalendarDataSource 的单例
    single<AcademicCalendarDataSource> {
        AcademicCalendarDataSourceImpl(htmlParser = get())
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
    single<CalendarRepository> {
        CalendarRepositoryImpl(
            courseDataSource = get(),
            localDataSource = get(),
            htmlParser = get()
        )
    }

    single<UpdateRepository> {
        UpdateRepositoryImpl(updateDataSource = get())
    }

    single<UpdateDataSource> {
        UpdateDataSourceImpl()
    }
}