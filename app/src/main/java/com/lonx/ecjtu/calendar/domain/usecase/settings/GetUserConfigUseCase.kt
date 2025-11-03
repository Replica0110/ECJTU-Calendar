package com.lonx.ecjtu.calendar.domain.usecase.settings

import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow

class GetUserConfigUseCase(private val repository: CalendarRepository) {
    operator fun invoke(): Flow<String> {
        return repository.getWeiXinID()
    }
}