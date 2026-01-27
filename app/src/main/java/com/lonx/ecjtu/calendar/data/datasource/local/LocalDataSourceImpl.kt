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
import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
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
            val id = preferences[PreferencesKeys.WEIXIN_ID] ?: ""
            Logger.d(Tags.LOCAL_DATA, "读取 WeiXinID: ${if (id.isNotEmpty()) Logger.mask(id) else "空"}")
            id
        }
    }

    override suspend fun saveWeiXinID(weiXinId: String) {
        Logger.d(Tags.LOCAL_DATA, "保存 WeiXinID: ${Logger.mask(weiXinId)}")
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.WEIXIN_ID] = weiXinId }
    }

    override fun getAutoUpdateCheckEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            val enabled = preferences[PreferencesKeys.AUTO_UPDATE_CHECK] ?: true
            Logger.d(Tags.LOCAL_DATA, "读取自动更新设置: $enabled")
            enabled
        }
    }

    override suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        Logger.d(Tags.LOCAL_DATA, "保存自动更新设置: $enabled")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_CHECK] = enabled
        }
    }

    override fun getColorModeSetting(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            val mode = preferences[PreferencesKeys.COLOR_MODE] ?: 0
            Logger.d(Tags.LOCAL_DATA, "读取颜色模式: $mode")
            mode
        }
    }

    override suspend fun saveColorModeSetting(mode: Int) {
        Logger.d(Tags.LOCAL_DATA, "保存颜色模式: $mode")
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.COLOR_MODE] = mode }
    }


    override fun getScoreLastRefresh(term: String): Flow<Long> {
        val key = lastScoreRefreshKey(term)
        return context.dataStore.data.map { prefs ->
            val timestamp = prefs[key] ?: 0L
            Logger.d(Tags.LOCAL_DATA, "读取成绩刷新时间 [$term]: ${if (timestamp > 0) timestamp else "未设置"}")
            timestamp
        }
    }

    override suspend fun saveScoreLastRefresh(term: String, timestampMillis: Long) {
        Logger.d(Tags.LOCAL_DATA, "保存成绩刷新时间 [$term]: $timestampMillis")
        val key = lastScoreRefreshKey(term)
        context.dataStore.edit { preferences -> preferences[key] = timestampMillis }
    }

    override suspend fun removeScoreLastRefresh(term: String) {
        Logger.d(Tags.LOCAL_DATA, "删除成绩刷新时间 [$term]")
        val key = lastScoreRefreshKey(term)
        context.dataStore.edit { preferences -> preferences.remove(key) }
    }


    override fun getSelectedCourseLastRefresh(term: String): Flow<Long> {
        val key = lastSelectedCourseRefreshKey(term)
        return context.dataStore.data.map { prefs ->
            val timestamp = prefs[key] ?: 0L
            Logger.d(Tags.LOCAL_DATA, "读取选课刷新时间 [$term]: ${if (timestamp > 0) timestamp else "未设置"}")
            timestamp
        }
    }

    override suspend fun saveSelectedCourseLastRefresh(term: String, timestampMillis: Long) {
        Logger.d(Tags.LOCAL_DATA, "保存选课刷新时间 [$term]: $timestampMillis")
        val key = lastSelectedCourseRefreshKey(term)
        context.dataStore.edit { preferences -> preferences[key] = timestampMillis }
    }

    override suspend fun removeSelectedCourseLastRefresh(term: String) {
        Logger.d(Tags.LOCAL_DATA, "删除选课刷新时间 [$term]")
        val key = lastSelectedCourseRefreshKey(term)
        context.dataStore.edit { preferences -> preferences.remove(key) }
    }

    override suspend fun saveKeyColorIndex(index: Int) {
        Logger.d(Tags.LOCAL_DATA, "保存主题色索引: $index")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_COLOR_INDEX] = index
        }
    }

    override fun getKeyColorIndex(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            val index = preferences[PreferencesKeys.KEY_COLOR_INDEX] ?: 0
            Logger.d(Tags.LOCAL_DATA, "读取主题色索引: $index")
            index
        }
    }
}
