package com.lonx.ecjtu.hjcalendar.logic

import com.tencent.mmkv.MMKV

object DataStoreManager {

    private const val KEY_WEIXIN_ID = "key_weixin_id"
    private const val KEY_CHECK_UPDATE_ON_START = "key_check_update_on_start"

    private const val KEY_NO_COURSE_TEXT = "key_no_course_text"

    private val mmkv = MMKV.defaultMMKV()

    fun saveWeiXinId(id: String) {
        mmkv.encode(KEY_WEIXIN_ID, id)
    }

    fun getWeiXinId(): String {
        return mmkv.decodeString(KEY_WEIXIN_ID, "") ?: ""
    }

    fun setUpdateCheckOnStart(enabled: Boolean) {
        mmkv.encode(KEY_CHECK_UPDATE_ON_START, enabled)
    }

    fun isUpdateCheckOnStartEnabled(): Boolean {
        return mmkv.decodeBool(KEY_CHECK_UPDATE_ON_START, true)
    }

    fun saveNoCourseText(text: String) {
        mmkv.encode(KEY_NO_COURSE_TEXT, text)
    }

    fun getNoCourseText(defaultValue: String): String {
        return mmkv.decodeString(KEY_NO_COURSE_TEXT, defaultValue) ?: defaultValue
    }
}