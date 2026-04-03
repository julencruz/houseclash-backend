package com.houseclash.backend.infrastructure.scheduler

import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.usecase.ApplyMarketInflationUsecase
import com.houseclash.backend.domain.usecase.AutoApproveExpiredTasksUsecase
import com.houseclash.backend.domain.usecase.RecurringTaskSchedulerUsecase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TaskSchedulerService(
    private val recurringTaskSchedulerUsecase: RecurringTaskSchedulerUsecase,
    private val autoApproveExpiredTasksUsecase: AutoApproveExpiredTasksUsecase,
    private val applyMarketInflationUsecase: ApplyMarketInflationUsecase,
    private val houseRepository: HouseRepository
) {

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 1)
    fun scheduleRecurringTasks() {
        recurringTaskSchedulerUsecase.execute()
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 1)
    fun autoApproveExpiredTasks() {
        houseRepository.findAll().forEach { house ->
            autoApproveExpiredTasksUsecase.execute(house.id!!)
        }
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 6)
    fun applyMarketInflation() {
        houseRepository.findAll().forEach { house ->
            applyMarketInflationUsecase.execute(house.id!!)
        }
    }
}
