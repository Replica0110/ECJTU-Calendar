package com.lonx.ecjtu.calendar.data.datasource.local


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
    }

    override fun getWeiXinID(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WEIXIN_ID] ?: ""
        }
    }

    override suspend fun saveWeiXinID(weiXinId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIXIN_ID] = weiXinId
        }
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
}