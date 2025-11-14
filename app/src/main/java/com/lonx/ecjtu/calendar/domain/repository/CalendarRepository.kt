package com.lonx.ecjtu.calendar.domain.repository

import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CalendarRepository {

    suspend fun getCourses(date: LocalDate): Result<SchedulePage>

    suspend fun saveWeiXinID(weiXinId: String)

    fun getWeiXinID(): Flow<String>

    fun getAutoUpdateCheckSetting(): Flow<Boolean>

    suspend fun saveAutoUpdateCheckSetting(enabled: Boolean)

    suspend fun saveColorModeSetting( mode: Int)

    fun getColorModeSetting(): Flow<Int>
}