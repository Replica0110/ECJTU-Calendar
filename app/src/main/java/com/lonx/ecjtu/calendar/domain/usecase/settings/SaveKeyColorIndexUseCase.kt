package com.lonx.ecjtu.calendar.domain.usecase.settings

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository

class SaveKeyColorIndexUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(index: Int) {
        repository.saveKeyColorIndex(index)
    }
}
