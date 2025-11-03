package com.lonx.ecjtu.calendar.data.datasource.remote

interface AcademicCalendarDataSource {
    suspend fun getAcademicCalendar(): Result<String>
}