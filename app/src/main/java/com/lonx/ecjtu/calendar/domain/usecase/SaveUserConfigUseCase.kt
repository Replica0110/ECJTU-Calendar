package com.lonx.ecjtu.calendar.domain.usecase

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import java.net.URL

class SaveUserConfigUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(weiXinID: String) {
        repository.saveWeiXinID(weiXinID)
    }
}
