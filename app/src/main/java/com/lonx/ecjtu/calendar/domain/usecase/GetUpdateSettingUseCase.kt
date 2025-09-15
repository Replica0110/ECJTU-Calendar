package com.lonx.ecjtu.calendar.domain.usecase


import com.lonx.ecjtu.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow

class GetUpdateSettingUseCase(private val repository: CalendarRepository) {
    operator fun invoke(): Flow<Boolean> = repository.getAutoUpdateCheckSetting()
}