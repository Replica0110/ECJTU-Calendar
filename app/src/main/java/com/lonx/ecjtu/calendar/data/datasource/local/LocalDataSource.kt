package com.lonx.ecjtu.calendar.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getWeiXinID(): Flow<String>
    suspend fun saveWeiXinID(weiXinId: String)

    fun getAutoUpdateCheckEnabled(): Flow<Boolean>

    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean)

    suspend fun saveColorModeSetting(mode: Int)
    fun getColorModeSetting(): Flow<Int>

    // Last network refresh timestamp for scores (per term). Returns 0L if not present.
    fun getScoreLastRefresh(term: String): Flow<Long>

    suspend fun saveScoreLastRefresh(term: String, timestampMillis: Long)

    suspend fun removeScoreLastRefresh(term: String)

    fun getSelectedCourseLastRefresh(term: String): Flow<Long>

    suspend fun saveSelectedCourseLastRefresh(term: String, timestampMillis: Long)

    suspend fun removeSelectedCourseLastRefresh(term: String)
}