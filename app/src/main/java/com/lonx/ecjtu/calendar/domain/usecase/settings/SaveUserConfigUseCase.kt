package com.lonx.ecjtu.calendar.domain.usecase.settings

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository

class SaveUserConfigUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(weiXinID: String) {
        repository.saveWeiXinID(weiXinID)
    }
}