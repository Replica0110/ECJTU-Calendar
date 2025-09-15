package com.lonx.ecjtu.calendar.domain.model

sealed class CalendarError(message: String = "") : Exception(message) {
    class NoWeiXinId : CalendarError("请先在设置中配置微信ID")
    class NetworkError(errorMessage: Exception) : CalendarError("网络错误: $errorMessage")
    class UnknownError(errorMessage: Exception) : CalendarError("未知错误: $errorMessage")
    class WeiXinIdInvalid : CalendarError("微信ID错误，请检查")
}
