package com.lonx.ecjtu.hjcalendar.utils


/**
 * 用于包装 LiveData 中的一次性事件。
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // 只允许外部读取

    /**
     * 返回内容，并防止其被再次使用。
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 返回内容，即使它已经被处理过。
     */
    fun peekContent(): T = content
}