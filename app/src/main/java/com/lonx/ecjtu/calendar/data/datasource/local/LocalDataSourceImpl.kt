package com.lonx.ecjtu.calendar.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 在文件顶层声明 DataStore 实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LocalDataSourceImpl(private val context: Context) : LocalDataSource {

    private object PreferencesKeys {
        val WEIXIN_ID = stringPreferencesKey("weixin_id")

        val AUTO_UPDATE_CHECK = booleanPreferencesKey("auto_update_check")

        val COLOR_MODE = intPreferencesKey("color_mode")

        val KEY_COLOR_INDEX = intPreferencesKey("key_color_index")
    }

    private fun lastScoreRefreshKey(term: String) = longPreferencesKey("score_last_refresh_$term")
    private fun lastSelectedCourseRefreshKey(term: String) =
        longPreferencesKey("selected_course_last_refresh_$term")

    override fun getWeiXinID(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WEIXIN_ID] ?: ""
        }
    }

    override suspend fun saveWeiXinID(weiXinId: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.WEIXIN_ID] = weiXinId }
    }

    override fun getAutoUpdateCheckEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_CHECK] ?: true
        }
    }

    override suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_CHECK] = enabled
        }
    }

    override fun getColorModeSetting(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.COLOR_MODE] ?: 0
        }
    }

    override suspend fun saveColorModeSetting(mode: Int) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.COLOR_MODE] = mode }
    }


    override fun getScoreLastRefresh(term: String): Flow<Long> {
        val key = lastScoreRefreshKey(term)
        return context.dataStore.data.map { prefs -> prefs[key] ?: 0L }
    }

    override suspend fun saveScoreLastRefresh(term: String, timestampMillis: Long) {
        val key = lastScoreRefreshKey(term)
        context.dataStore.edit { preferences -> preferences[key] = timestampMillis }
    }

    override suspend fun removeScoreLastRefresh(term: String) {
        val key = lastScoreRefreshKey(term)
        context.dataStore.edit { preferences -> preferences.remove(key) }
    }


    override fun getSelectedCourseLastRefresh(term: String): Flow<Long> {
        val key = lastSelectedCourseRefreshKey(term)
        return context.dataStore.data.map { prefs -> prefs[key] ?: 0L }
    }

    override suspend fun saveSelectedCourseLastRefresh(term: String, timestampMillis: Long) {
        val key = lastSelectedCourseRefreshKey(term)
        context.dataStore.edit { preferences -> preferences[key] = timestampMillis }
    }

    override suspend fun removeSelectedCourseLastRefresh(term: String) {
        val key = lastSelectedCourseRefreshKey(term)
        context.dataStore.edit { preferences -> preferences.remove(key) }
    }

    override suspend fun saveKeyColorIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_COLOR_INDEX] = index
        }
    }

    override fun getKeyColorIndex(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_COLOR_INDEX] ?: 0
        }
    }
}
