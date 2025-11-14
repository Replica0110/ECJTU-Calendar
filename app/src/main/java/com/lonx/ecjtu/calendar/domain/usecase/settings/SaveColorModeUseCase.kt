package com.lonx.ecjtu.calendar.domain.usecase.settings

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository

class SaveColorModeUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(colorMode: Int) {
        repository.saveColorModeSetting(colorMode)
    }
}