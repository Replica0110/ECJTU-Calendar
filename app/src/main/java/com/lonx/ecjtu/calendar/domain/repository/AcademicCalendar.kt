package com.lonx.ecjtu.calendar.domain.repository

/**
 * 校历仓库接口
 */
interface AcademicCalendar {
    /**
     * 获取学术校历图片信息
     * @return Result包装的图片URL字符串
     */
    suspend fun getAcademicCalendar(): Result<String>
}