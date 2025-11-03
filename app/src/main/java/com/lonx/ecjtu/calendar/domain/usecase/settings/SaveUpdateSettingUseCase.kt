package com.lonx.ecjtu.calendar.domain.usecase.settings

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository

class SaveUpdateSettingUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveAutoUpdateCheckSetting(enabled)
}