package com.lonx.ecjtu.calendar.domain.usecase.settings

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow

class GetColorModeUseCase(private val repository: CalendarRepository) {
    operator fun invoke(): Flow<Int> = repository.getColorModeSetting()
}