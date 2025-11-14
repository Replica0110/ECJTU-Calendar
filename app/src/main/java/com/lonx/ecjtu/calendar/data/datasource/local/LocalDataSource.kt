package com.lonx.ecjtu.calendar.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWeiXinID(): Flow<String>
    suspend fun saveWeiXinID(weiXinId: String)

    fun getAutoUpdateCheckEnabled(): Flow<Boolean>

    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean)

    suspend fun saveColorModeSetting(mode: Int)
    fun getColorModeSetting(): Flow<Int>
}